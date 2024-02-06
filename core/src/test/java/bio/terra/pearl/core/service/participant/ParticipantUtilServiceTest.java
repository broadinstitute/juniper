package bio.terra.pearl.core.service.participant;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class ParticipantUtilServiceTest {

    private ParticipantUtilService participantUtilService = new ParticipantUtilService();

    @Test
    void generateSecureRandomString_Success() {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 10;

        String result = participantUtilService.generateSecureRandomString(length, allowedChars);
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

        assertThrows(IllegalArgumentException.class, () -> participantUtilService.generateSecureRandomString(length, allowedChars));
    }

}
