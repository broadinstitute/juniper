package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShortcodeServiceTest {

    @Test
    void testGenerateShortcodeWithPrefix() {
        ShortcodeService shortcodeService = new ShortcodeService(new RandomUtilService());
        String shortcode = shortcodeService.generateShortcode("F", (s) -> Optional.empty());
        assertNotNull(shortcode);
        assertTrue(shortcode.startsWith("F_"));
        assertEquals(8, shortcode.length());
        assertTrue(shortcode.substring(2).matches("[A-Z]{6}"));
    }

    @Test
    void testGenerateShortcodeNoPrefix() {
        ShortcodeService shortcodeService = new ShortcodeService(new RandomUtilService());
        String shortcode = shortcodeService.generateShortcode(null, (s) -> Optional.empty());
        assertNotNull(shortcode);
        assertEquals(6, shortcode.length());
        assertTrue(shortcode.matches("[A-Z]{6}"));
    }

    @Test
    void testFailsToGenerateShortcode() {
        ShortcodeService shortcodeService = new ShortcodeService(new RandomUtilService());

        assertThrows(InternalServerException.class,
                () -> shortcodeService.generateShortcode(null, (s) -> Optional.of(true)));

    }

    @Test
    void testIsShortcodeBanned() {
        RandomUtilService randomUtilService = Mockito.mock(RandomUtilService.class);
        ShortcodeService shortcodeService = Mockito.spy(new ShortcodeService(randomUtilService));

        assertTrue(shortcodeService.isShortcodeBanned("BANNED"));
    }

    @Test
    void testIsShortcodeAllowed() {
        RandomUtilService randomUtilService = Mockito.mock(RandomUtilService.class);
        ShortcodeService shortcodeService = Mockito.spy(new ShortcodeService(randomUtilService));

        assertFalse(shortcodeService.isShortcodeBanned("ALLOWED"));
    }

    @Test
    void testFailsToGenerateProfanityShortcode() {
        RandomUtilService randomUtilService = Mockito.mock(RandomUtilService.class);
        Mockito.when(
                        randomUtilService.generateSecureRandomString(
                                ShortcodeService.SHORTCODE_LENGTH,
                                ShortcodeService.ALLOWED_CHARS))
                .thenReturn("BANNED");
        ShortcodeService shortcodeService = Mockito.spy(new ShortcodeService(randomUtilService));

        assertThrows(InternalServerException.class,
                () -> shortcodeService.generateShortcode(null, (s) -> Optional.of(true)));
    }

    @Test
    void testRetriesAfterProfanityShortcode() {
        RandomUtilService randomUtilService = Mockito.mock(RandomUtilService.class);
        Mockito.when(
                        randomUtilService.generateSecureRandomString(
                                ShortcodeService.SHORTCODE_LENGTH,
                                ShortcodeService.ALLOWED_CHARS))
                .thenReturn("BANNED")
                .thenReturn("ALLOWED");
        ShortcodeService shortcodeService = Mockito.spy(new ShortcodeService(randomUtilService));

        String shortcode = shortcodeService.generateShortcode(null, (s) -> Optional.empty());
        assertNotNull(shortcode);
        assertEquals("ALLOWED", shortcode);
    }
}
