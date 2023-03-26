package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.MailingListApi;
import bio.terra.pearl.api.admin.service.MailingListExtService;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class MailingListController implements MailingListApi {

  private RequestUtilService requestUtilService;
  private HttpServletRequest request;
  private MailingListExtService mailingListExtService;

  public MailingListController(
      RequestUtilService requestUtilService,
      HttpServletRequest request,
      MailingListExtService mailingListExtService) {
    this.requestUtilService = requestUtilService;
    this.request = request;
    this.mailingListExtService = mailingListExtService;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = requestUtilService.getFromRequest(request);
    List<MailingListContact> contacts =
        mailingListExtService.getAll(portalShortcode, environmentName, user);
    return ResponseEntity.ok(contacts);
  }
}
