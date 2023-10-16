package bio.terra.pearl.populate.util;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SurveyValidationTest {

    @Test
    public void testValidationRegx() {
        // weight
        Pattern pattern = Pattern.compile("^([1-9][0-9]?|[1-4]\\d\\d|500)$");
        Assertions.assertFalse(pattern.matcher("0").matches());
        Assertions.assertFalse(pattern.matcher("01").matches());
        Assertions.assertTrue(pattern.matcher("7").matches());
        Assertions.assertTrue(pattern.matcher("83").matches());
        Assertions.assertFalse(pattern.matcher("401.2").matches());
        Assertions.assertTrue(pattern.matcher("499").matches());
        Assertions.assertTrue(pattern.matcher("500").matches());
        Assertions.assertFalse(pattern.matcher("501").matches());
        Assertions.assertFalse(pattern.matcher("1115").matches());

        //pattern = Pattern.compile("^[1-9]|[1-9]\\d|[1-9]\\d\\d|(1000)$");
        pattern = Pattern.compile("^([1-9][0-9]{0,2}|1000)$");
        Assertions.assertFalse(pattern.matcher("0").matches());
        Assertions.assertFalse(pattern.matcher("01").matches());
        Assertions.assertTrue(pattern.matcher("7").matches());
        Assertions.assertTrue(pattern.matcher("83").matches());
        Assertions.assertTrue(pattern.matcher("999").matches());
        Assertions.assertTrue(pattern.matcher("1000").matches());
        Assertions.assertFalse(pattern.matcher("1001").matches());
        Assertions.assertFalse(pattern.matcher("1115").matches());

        // inches
        pattern = Pattern.compile("^([1-9]|1[0-1])?(\\.5)?$");
        Assertions.assertFalse(pattern.matcher("0").matches());
        Assertions.assertFalse(pattern.matcher("01").matches());
        Assertions.assertTrue(pattern.matcher(".5").matches());
        Assertions.assertTrue(pattern.matcher("7").matches());
        Assertions.assertTrue(pattern.matcher("7.5").matches());
        Assertions.assertTrue(pattern.matcher("10").matches());
        Assertions.assertTrue(pattern.matcher("11.5").matches());
        Assertions.assertFalse(pattern.matcher("11.6").matches());
        Assertions.assertFalse(pattern.matcher("12").matches());
        Assertions.assertFalse(pattern.matcher("115").matches());
    }
}
