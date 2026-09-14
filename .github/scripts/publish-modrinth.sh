#!/usr/bin/env bash
# Publishes each Minecraft version's jar to Modrinth, in the exact order given by the version manifest.
#
# Required env vars:
#   MODRINTH_TOKEN          - Modrinth API token
#   MODRINTH_PROJECT        - Modrinth project ID/slug to publish to
#   TAG                     - the tagged release to publish on Modrinth
#   RELEASE_NAME            - base name for each created Modrinth version
#   RELEASE_BODY            - changelog text
#   ASSETS_DIR              - directory already containing the release's downloaded jars and
#                             manifest.json (JSON array of {minecraft_version, java_version,
#                             advertised_versions})
#
# Required scope: MODRINTH_TOKEN needs "Create versions" permission on MODRINTH_PROJECT.
set -euo pipefail

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT

mapfile -t entries < <(jq -c '.[]' "$ASSETS_DIR/manifest.json")

echo "== Validating expected assets =="
missing_jars=()
for entry in "${entries[@]}"; do
  mc_version=$(jq -r '.minecraft_version' <<< "$entry")
  jar_name="gui-shop-${TAG}+${mc_version}.jar"
  if [ ! -f "$ASSETS_DIR/$jar_name" ]; then
    missing_jars+=("$jar_name")
  fi
done

if [ ${#missing_jars[@]} -gt 0 ]; then
  echo "Missing expected assets in $ASSETS_DIR:" >&2
  printf '  - %s\n' "${missing_jars[@]}" >&2
  exit 1
fi
echo "All expected assets present."
echo

failed_versions=()
for entry in "${entries[@]}"; do
  mc_version=$(jq -r '.minecraft_version' <<< "$entry")
  jar_name="gui-shop-${TAG}+${mc_version}.jar"
  jar_path="$ASSETS_DIR/$jar_name"
  data_path="$work_dir/data-${mc_version}.json"

  # advertised_versions is the version itself plus any additional compatible versions
  game_versions=$(jq -c '.advertised_versions' <<< "$entry")

  echo "== Publishing $mc_version (Minecraft $(jq -r 'join(", ")' <<< "$game_versions")) =="

  jq -n \
    --arg name "$RELEASE_NAME ($mc_version)" \
    --arg version_number "${TAG}+${mc_version}" \
    --arg changelog "$RELEASE_BODY" \
    --arg project_id "$MODRINTH_PROJECT" \
    --argjson game_versions "$game_versions" \
    '{
      name: $name,
      version_number: $version_number,
      changelog: $changelog,
      dependencies: [],
      game_versions: $game_versions,
      loaders: ["fabric"],
      featured: false,
      status: "listed",
      requested_status: "listed",
      version_type: "release",
      project_id: $project_id,
      file_parts: ["file"],
      primary_file: "file"
    }' > "$data_path"

  response_path="$work_dir/response-${mc_version}.json"
  http_code=$(curl -s -o "$response_path" -w '%{http_code}' -X POST "https://api.modrinth.com/v2/version" \
    -H "Authorization: $MODRINTH_TOKEN" \
    -F "data=@${data_path};type=application/json" \
    -F "file=@${jar_path};type=application/java-archive")

  if [[ "$http_code" == 2* ]]; then
    echo "OK: $mc_version"
  else
    echo "FAILED: $mc_version (HTTP $http_code)" >&2
    cat "$response_path" >&2
    failed_versions+=("$mc_version")
  fi
  echo
done

if [ ${#failed_versions[@]} -gt 0 ]; then
  echo "Failed to publish: ${failed_versions[*]}" >&2
  exit 1
fi
