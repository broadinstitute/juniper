package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WithdrawnEnrolleeExtService {
  private final WithdrawnEnrolleeService withdrawnEnrolleeService;

  public WithdrawnEnrolleeExtService(WithdrawnEnrolleeService withdrawnEnrolleeService) {
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<WithdrawnEnrollee> getAll(PortalStudyEnvAuthContext authContext) {
    return withdrawnEnrolleeService.findByStudyEnvironmentIdNoData(
        authContext.getStudyEnvironment().getId());
  }
}
