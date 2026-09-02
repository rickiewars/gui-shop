package rickiewars.guishop.api.minecraft;

public record ResourceId(String namespace, String path) {
    private static final String DEFAULT_NAMESPACE = "minecraft";

    public ResourceId {
        if (namespace.isEmpty() || path.isEmpty()) {
            throw new IllegalArgumentException("ResourceId namespace and path must not be empty: " + namespace + ":" + path);
        }
    }

    public static ResourceId of(String namespace, String path) {
        return new ResourceId(namespace, path);
    }

    public static ResourceId ofVanilla(String path) {
        return new ResourceId(DEFAULT_NAMESPACE, path);
    }

    /** Parses "namespace:path", or bare "path" (assumed "minecraft" namespace). */
    public static ResourceId parse(String raw) {
        int colon = raw.indexOf(':');
        if (colon < 0) return new ResourceId(DEFAULT_NAMESPACE, raw);
        return new ResourceId(raw.substring(0, colon), raw.substring(colon + 1));
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
