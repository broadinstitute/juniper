package bio.terra.pearl.populate.util;

import bio.terra.pearl.populate.service.EnrolleePopulateType;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class PopulateUtils {

    public static Integer randomInteger(int min, int max) {
        return new Random().nextInt(min, max);
    }

    //Returns a boolean that's biased to be true 'trueWeight'% of the time
    public static boolean randomBoolean(int trueWeight) {
        return new Random().nextInt(100) < trueWeight;
    }

    public static String randomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static <T> T randomItem(List<T> items) {
        return items.get(randomInteger(0, items.size()));
    }

    public static String randomShortcode(String prefix) {
        return prefix + randomString(10).toUpperCase();
    }

    public static String generateEmail() {
        return randomString(16).toLowerCase() + ".seed@test.com";
    }

    public static String generateEmail(EnrolleePopulateType enrolleePopulateType) {
        return enrolleePopulateType.name() + "-" + randomString(8).toLowerCase() + "@test.com";
    }


    public static LocalDate generateRandomDate() {
        long minDay = LocalDate.of(1920, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        long randomDay = new Random().nextLong(minDay, maxDay);

        return LocalDate.ofEpochDay(randomDay);
    }

}
