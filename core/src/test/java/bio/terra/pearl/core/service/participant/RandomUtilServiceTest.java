package bio.terra.pearl.core.service.participant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilServiceTest {

    private RandomUtilService randomUtilService = new RandomUtilService();

    @Test
    void generateSecureRandomString_Success() {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 10;

        String result = randomUtilService.generateSecureRandomString(length, allowedChars);
        assertNotNull(result);
        assertEquals(10, result.length());
        for(char c: allowedChars.toCharArray()){
            assertNotEquals(allowedChars.indexOf(c), -1);
        }
    }

    @Test
    void generateSecureRandomString_invalidInput() {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = -1;

        assertThrows(IllegalArgumentException.class, () -> randomUtilService.generateSecureRandomString(length, allowedChars));
    }

}
