package bio.terra.pearl.core.service.participant.search;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantUtilService {

    private SecureRandom secureRandom;

    public ParticipantUtilService(SecureRandom secureRandom){
        this.secureRandom = secureRandom;
    }


    @Transactional
    public String generateSecureRandomString(int length, String allowedChars, Function<String, Optional> func) {
        int MAX_TRIES = 10;
        String finalString = null;
        for (int tryNum = 0; tryNum < MAX_TRIES; tryNum++) {
            String possibleRandomString = secureRandom
                    .ints(length, 0, allowedChars.length())
                    .mapToObj(i -> allowedChars.charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

            if (func.apply(possibleRandomString).isEmpty()) {
                finalString = possibleRandomString;
                break;
            }
        }
        if (finalString == null) {
            throw new InternalServerException("Unable to generate unique shortcode");
        }
        return finalString;
    }

    @Transactional
    public String generateSecureRandomString(int length, String allowedChars, BiFunction<String, EnvironmentName, Optional> func, EnvironmentName environmentName, String prefix) {
        int MAX_TRIES = 10;
        String finalString = null;
        for (int tryNum = 0; tryNum < MAX_TRIES; tryNum++) {
            String possibleRandomString = secureRandom
                    .ints(length, 0, allowedChars.length())
                    .mapToObj(i -> allowedChars.charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

            if (func.apply(prefix + possibleRandomString, environmentName).isEmpty()) {
                finalString = prefix + possibleRandomString;
                break;
            }
        }
        if (finalString == null) {
            throw new InternalServerException("Unable to generate unique shortcode");
        }
        return finalString;
    }
}
