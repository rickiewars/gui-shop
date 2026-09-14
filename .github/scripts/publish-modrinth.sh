#!/usr/bin/env bash
# Publishes each Minecraft version's jar to Modrinth, in the exact order given by VERSIONS_JSON.
#
# Required env vars:
#   GH_TOKEN, GH_REPO       - for `gh release download`
#   MODRINTH_TOKEN          - Modrinth API token
#   MODRINTH_PROJECT        - Modrinth project ID/slug to publish to
#   TAG                     - the tagged release to publish on Modrinth
#   RELEASE_NAME            - base name for each created Modrinth version
#   RELEASE_BODY            - changelog text
#   VERSIONS_JSON           - JSON array of {minecraft_version, java_version}, oldest first
#
set -euo pipefail

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT

echo "$VERSIONS_JSON" | jq -c '.[]' | while read -r entry; do
  mc_version=$(echo "$entry" | jq -r '.minecraft_version')
  jar_name="gui-shop-${TAG}+${mc_version}.jar"
  jar_path="$work_dir/$jar_name"
  data_path="$work_dir/data.json"

  echo "== Publishing $mc_version =="
  gh release download "$TAG" --pattern "$jar_name" --output "$jar_path"

  jq -n \
    --arg name "$RELEASE_NAME ($mc_version)" \
    --arg version_number "${TAG}+${mc_version}" \
    --arg changelog "$RELEASE_BODY" \
    --arg project_id "$MODRINTH_PROJECT" \
    --arg mc_version "$mc_version" \
    '{
      name: $name,
      version_number: $version_number,
      changelog: $changelog,
      dependencies: [],
      game_versions: [$mc_version],
      loaders: ["fabric"],
      featured: false,
      status: "listed",
      requested_status: "listed",
      version_type: "release",
      project_id: $project_id,
      file_parts: ["file"],
      primary_file: "file"
    }' > "$data_path"

  curl -sf -X POST "https://api.modrinth.com/v2/version" \
    -H "Authorization: $MODRINTH_TOKEN" \
    -F "data=@${data_path};type=application/json" \
    -F "file=@${jar_path};type=application/java-archive"

  echo
done
