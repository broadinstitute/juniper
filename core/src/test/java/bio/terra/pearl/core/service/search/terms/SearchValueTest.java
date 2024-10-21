package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.BaseSpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchValueTest extends BaseSpringBootTest {

    @Test
    public void testEquals(TestInfo info) {
        assertTrue(new SearchValue("Test").equals(new SearchValue("Test")));
        assertFalse(new SearchValue("Test").equals(new SearchValue("Test2")));
        assertFalse(new SearchValue("Test").equals(new SearchValue(10.0)));
        assertFalse(new SearchValue("10.0").equals(new SearchValue(10.0)));
        assertTrue(new SearchValue(10.0).equals(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).equals(new SearchValue(11.0)));

        assertTrue(new SearchValue(Instant.ofEpochMilli(100)).equals(new SearchValue(Instant.ofEpochMilli(100))));
        assertFalse(new SearchValue(Instant.ofEpochMilli(90)).equals(new SearchValue(Instant.ofEpochMilli(100))));

    }

    @Test
    public void testGreaterThan(TestInfo info) {
        assertFalse(new SearchValue("Test2").greaterThan(new SearchValue("Test")));
        assertFalse(new SearchValue("Test").equals(new SearchValue("Test2")));
        assertFalse(new SearchValue("Test").equals(new SearchValue(10.0)));
        assertFalse(new SearchValue("10.0").equals(new SearchValue(10.0)));
    }

    @Test
    public void testNullResilience(TestInfo info) {
        String value = null;
        assertFalse(new SearchValue(value).equals(new SearchValue("Test")));
        assertFalse(new SearchValue("test").equals(new SearchValue(value)));

        Double numberValue = null;
        assertFalse(new SearchValue(numberValue).equals(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).equals(new SearchValue(numberValue)));

        assertFalse(new SearchValue(numberValue).greaterThan(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).greaterThan(new SearchValue(numberValue)));

        assertFalse(new SearchValue(numberValue).greaterThanOrEqualTo(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).greaterThanOrEqualTo(new SearchValue(numberValue)));

        List<SearchValue> arrayVal = null;

        assertFalse(new SearchValue(arrayVal).equals(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).equals(new SearchValue(arrayVal)));

        List<SearchValue> arrayWithNull = List.of(new SearchValue(1.0), new SearchValue(numberValue));

        assertFalse(new SearchValue(arrayWithNull).equals(new SearchValue(10.0)));
        assertFalse(new SearchValue(10.0).equals(new SearchValue(arrayWithNull)));
        assertTrue(new SearchValue(arrayWithNull).equals(new SearchValue(1.0)));
    }

    @Test
    public void arrayEquals(TestInfo info) {
        SearchValue arrayVal = new SearchValue(List.of(new SearchValue(1.0), new SearchValue(2.0), new SearchValue(3.0)));

        // works like SQL; equals means check if value is in array
        assertTrue(arrayVal.equals(new SearchValue(2.0)));
        assertFalse(arrayVal.equals(new SearchValue(4.0)));
        assertFalse(arrayVal.equals(new SearchValue("asdf")));

        // works reversed
        assertTrue(new SearchValue(2.0).equals(arrayVal));
        assertFalse(new SearchValue(4.0).equals(arrayVal));

        // compares two arrays element-wise
        SearchValue diffArrayVal = new SearchValue(List.of(new SearchValue(65.0), new SearchValue(2.0), new SearchValue(3.0)));
        SearchValue arrayCopy = new SearchValue(List.of(new SearchValue(1.0), new SearchValue(2.0), new SearchValue(3.0)));
        SearchValue wrongOrder = new SearchValue(List.of(new SearchValue(2.0), new SearchValue(1.0), new SearchValue(3.0)));
        assertTrue(arrayVal.equals(arrayCopy));
        assertFalse(arrayVal.equals(diffArrayVal));
        assertFalse(arrayVal.equals(wrongOrder));
    }

}
