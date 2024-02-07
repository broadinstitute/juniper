package bio.terra.pearl.core.service.participant;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RandomUtilService {

    private final SecureRandom secureRandom;

    public RandomUtilService() {
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String generateSecureRandomString(int length, String allowedChars) {
        if (length < 0)
            throw new IllegalArgumentException("length of the random String should be more than 0");
        String finalString = secureRandom
                .ints(length, 0, allowedChars.length())
                .mapToObj(i -> allowedChars.charAt(i))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        return finalString;
    }
}

