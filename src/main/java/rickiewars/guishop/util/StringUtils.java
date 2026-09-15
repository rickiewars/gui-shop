package rickiewars.guishop.util;

public interface StringUtils {

    /**
     * Turns a display name into a unique, filesystem/id-safe slug.
     * @param existingIds Slugs already in use
     * @return A unique, lowercase, id-safe slug
     */
    static String slugify(String name, java.util.Set<String> existingIds) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "shop";

        String candidate = base;
        int suffix = 2;
        while (existingIds.contains(candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    /**
     * Pad a string on the left until it reaches a minimum length
     * @param str The string to pad
     * @param length The minimum length the result should have
     * @param padChar The character to pad with
     * @return The padded string, or the original string if it already met the minimum length
     */
    static String padLeft(String str, int length, char padChar) {
        if (str.length() >= length) return str;
        return String.valueOf(padChar).repeat(length - str.length()) + str;
    }

    /**
     * Insert a character into a string at a specified index
     * @param str The string to insert the character into
     * @param index The index to insert the character at. If negative, it will be counted from the end of the string
     * @param insertChar The character to insert
     * @return The new string with the character inserted
     */
    static String insert(String str, int index, char insertChar) {
        if (index < 0) index = str.length() + index;
        return str.substring(0, index) + insertChar + str.substring(index);
    }
}
