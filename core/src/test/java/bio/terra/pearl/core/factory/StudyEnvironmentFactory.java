package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentFactory {
    @Autowired
    private EnvironmentFactory environmentFactory;
    public StudyEnvironment.StudyEnvironmentBuilder builder(String testName) {
        EnvironmentName envName = EnvironmentName.values()[RandomUtils.nextInt(0, 3)];
        return StudyEnvironment.builder()
                .environmentName(envName);
    }

    public StudyEnvironment.StudyEnvironmentBuilder builderWithDependencies(String testName) {
        return builder(testName)
                .environmentName(environmentFactory.buildPersisted(testName).getName());
    }
}
