package rickiewars.guishop.api.minecraft;

import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

public record ResourceId(String namespace, String path) {
    private static final String DEFAULT_NAMESPACE = "minecraft";
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9_./-]+");

    public ResourceId {
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Non [a-z0-9_.-] character in namespace of ResourceId: " + namespace + ":" + path);
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Non [a-z0-9_./-] character in path of ResourceId: " + namespace + ":" + path);
        }
    }

    public static ResourceId of(String namespace, String path) {
        return new ResourceId(namespace, path);
    }

    public static ResourceId ofVanilla(String path) {
        return new ResourceId(DEFAULT_NAMESPACE, path);
    }

    /** Parses "namespace:path", or bare "path" (assumed "minecraft" namespace). Throws on invalid or empty input. */
    public static ResourceId parse(String raw) {
        int colon = raw.indexOf(':');
        if (colon < 0) return new ResourceId(DEFAULT_NAMESPACE, raw);
        return new ResourceId(raw.substring(0, colon), raw.substring(colon + 1));
    }

    /** Like {@link #parse(String)}, but returns null instead of throwing on invalid or empty input. */
    public static ResourceId tryParse(String raw) {
        try {
            return parse(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    /** Converts to the real Minecraft identifier type. */
    public Identifier toIdentifier() {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
