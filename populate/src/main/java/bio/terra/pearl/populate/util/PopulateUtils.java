package bio.terra.pearl.populate.util;

import org.apache.commons.lang3.RandomStringUtils;

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

    public static String randomShortcode(String prefix) {
        return prefix + randomString(10).toUpperCase();
    }

    public static String generateEmail() {
        return randomString(16).toLowerCase() + ".seed@test.com";
    }

}
