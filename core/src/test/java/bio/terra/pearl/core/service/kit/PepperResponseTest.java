package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PepperResponseTest {

    @Test
    public void testCheckIsError() throws Exception {
        var json = """
                {
                  "isError": true,
                  "otherData": { "answer": 42 }
                }""";

        assertThat(PepperResponse.checkIsError(json, new ObjectMapper()), is(true));
    }

    @Test
    public void testCheckIsErrorEmpty() throws Exception {
        var json = """
                {
                  "otherData": { "answer": 42 }
                }""";

        assertThat(PepperResponse.checkIsError(json, new ObjectMapper()), is(false));
    }
}
