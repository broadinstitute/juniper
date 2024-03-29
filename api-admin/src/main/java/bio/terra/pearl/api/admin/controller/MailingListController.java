package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.MailingListApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.MailingListExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class MailingListController implements MailingListApi {

  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final MailingListExtService mailingListExtService;
  private final ObjectMapper objectMapper;

  public MailingListController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      MailingListExtService mailingListExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.mailingListExtService = mailingListExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    List<MailingListContact> contacts =
        mailingListExtService.getAll(portalShortcode, environmentName, user);
    return ResponseEntity.ok(contacts);
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, String envName, Object body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    List<MailingListContact> contacts =
        objectMapper.convertValue(body, new TypeReference<List<MailingListContact>>() {});

    List<MailingListContact> createdContacts =
        mailingListExtService.create(portalShortcode, environmentName, contacts, user);
    return ResponseEntity.ok(createdContacts);
  }

  @Override
  public ResponseEntity<Void> delete(String portalShortcode, String envName, UUID contactId) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    mailingListExtService.delete(portalShortcode, environmentName, contactId, user);
    return ResponseEntity.noContent().build();
  }
}
