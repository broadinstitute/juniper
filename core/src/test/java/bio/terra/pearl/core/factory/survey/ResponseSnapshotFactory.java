package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class ResponseSnapshotFactory {
    public ResponseSnapshot.ResponseSnapshotBuilder builder(String testName) {
        int randAnswer = RandomUtils.nextInt(0, 100);
        return ResponseSnapshot.builder()
                .fullData("{ questionStableId: \"" + testName + "\", value: " + randAnswer + " }")
                .resumeData("{ questionStableId: \"" + testName + "\", value: " + randAnswer + " }");
    }
}
