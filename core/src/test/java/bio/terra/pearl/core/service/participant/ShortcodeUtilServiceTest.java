package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShortcodeUtilServiceTest {

    @Test
    void testGenerateShortcodeWithPrefix() {
        ShortcodeUtilService shortcodeUtilService = new ShortcodeUtilService(new RandomUtilService());
        String shortcode = shortcodeUtilService.generateShortcode("F", (s) -> Optional.empty());
        assertNotNull(shortcode);
        assertTrue(shortcode.startsWith("F_"));
        assertEquals(8, shortcode.length());
        assertTrue(shortcode.substring(2).matches("[A-Z]{6}"));
    }

    @Test
    void testGenerateShortcodeNoPrefix() {
        ShortcodeUtilService shortcodeUtilService = new ShortcodeUtilService(new RandomUtilService());
        String shortcode = shortcodeUtilService.generateShortcode(null, (s) -> Optional.empty());
        assertNotNull(shortcode);
        assertEquals(6, shortcode.length());
        assertTrue(shortcode.matches("[A-Z]{6}"));
    }

    @Test
    void testFailsToGenerateShortcode() {
        ShortcodeUtilService shortcodeUtilService = new ShortcodeUtilService(new RandomUtilService());

        assertThrows(InternalServerException.class,
                () -> shortcodeUtilService.generateShortcode(null, (s) -> Optional.of(true)));

    }
}
