package rickiewars.guishop.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void slugifyLowercasesAndReplacesNonAlphanumerics() {
        assertEquals("my_cool_shop", StringUtils.slugify("My Cool Shop!", Set.of()));
    }

    @Test
    void slugifyStripsLeadingAndTrailingUnderscores() {
        assertEquals("shop", StringUtils.slugify("--Shop--", Set.of()));
    }

    @Test
    void slugifyFallsBackToShopWhenResultIsEmpty() {
        assertEquals("shop", StringUtils.slugify("!!!", Set.of()));
    }

    @Test
    void slugifyAppendsNumericSuffixOnCollision() {
        assertEquals("shop_2", StringUtils.slugify("Shop", Set.of("shop")));
    }

    @Test
    void slugifyIncrementsSuffixUntilUnique() {
        assertEquals("shop_3", StringUtils.slugify("Shop", Set.of("shop", "shop_2")));
    }

    @Test
    void padLeftPadsWhenShorterThanLength() {
        assertEquals("00042", StringUtils.padLeft("42", 5, '0'));
    }

    @Test
    void padLeftReturnsUnchangedWhenAlreadyExactLength() {
        assertEquals("42", StringUtils.padLeft("42", 2, '0'));
    }

    @Test
    void padLeftReturnsUnchangedWhenLongerThanLength() {
        assertEquals("12345", StringUtils.padLeft("12345", 2, '0'));
    }

    @Test
    void insertInsertsAtPositiveIndex() {
        assertEquals("hexllo", StringUtils.insert("hello", 2, 'x'));
    }

    @Test
    void insertInsertsAtNegativeIndexFromEnd() {
        assertEquals("helxlo", StringUtils.insert("hello", -2, 'x'));
    }
}
