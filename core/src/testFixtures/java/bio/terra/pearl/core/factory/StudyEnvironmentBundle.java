package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StudyEnvironmentBundle {
    private Study study;
    private StudyEnvironment studyEnv;
    private Portal portal;
    private PortalEnvironment portalEnv;
}
