package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.survey.Survey;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class VersionedEntityChangeTests {
    @Test
    public void testSourceDestConstructor() {
        Survey source = Survey.builder().stableId("blah").version(2).build();
        Survey dest = Survey.builder().stableId("blah").version(1).build();
        VersionedEntityChange changeRecord = new VersionedEntityChange(source, dest);
        assertThat(changeRecord, equalTo(new VersionedEntityChange(
                "blah", 1, "blah", 2
        )));
        // double check the version just in case the args to the above constructor get flipped around
        assertThat(changeRecord.newVersion(), equalTo(2));
    }
    @Test
    public void testSourceDestConstructorHandlesNullDest() {
        Survey source = Survey.builder().stableId("blah").version(1).build();
        Survey dest = null;
        VersionedEntityChange changeRecord = new VersionedEntityChange(source, dest);
        assertThat(changeRecord, equalTo(new VersionedEntityChange(
                null, null, "blah", 1
        )));
    }
    @Test
    public void testSourceDestConstructorHandlesNullSource() {
        Survey source = null;
        Survey dest = Survey.builder().stableId("blah").version(1).build();
        VersionedEntityChange changeRecord = new VersionedEntityChange(source, dest);
        assertThat(changeRecord, equalTo(new VersionedEntityChange(
                "blah", 1, null, null
        )));
    }
}
