package bio.terra.pearl.core.model.survey;

import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;

public class AnswerTests {
    @Test
    public void testStringValuesEqual() {
        Answer ans1 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue("blah").build();
        Answer ans2 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue("blah").build();
        assertThat(ans1.valuesEqual(ans2), equalTo(true));
    }

    @Test
    public void testStringValuesNotEqual() {
        Answer ans1 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue("blah").build();
        Answer ans2 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue("blo").build();
        assertThat(ans1.valuesEqual(ans2), equalTo(false));
    }

    @Test
    public void testStringValuesNotEqualNull() {
        Answer ans1 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue("blah").build();
        Answer ans2 = Answer.builder().enrolleeId(UUID.randomUUID()).stringValue(null).build();
        assertThat(ans1.valuesEqual(ans2), equalTo(false));
    }

    @Test
    public void testObjectValuesEqual() {
        Answer ans1 = Answer.builder().questionStableId("q1").objectValue("[1,3]").build();
        Answer ans2 = Answer.builder().enrolleeId(UUID.randomUUID()).objectValue("[1,3]").build();
        assertThat(ans1.valuesEqual(ans2), equalTo(true));
    }

    @Test
    public void testObjectValuesNotEqual() {
        Answer ans1 = Answer.builder().questionStableId("q1").objectValue("[1,3]").build();
        Answer ans2 = Answer.builder().enrolleeId(UUID.randomUUID()).objectValue("[1,4]").build();
        assertThat(ans1.valuesEqual(ans2), equalTo(false));
    }
}
