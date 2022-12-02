package bio.terra.pearl.api.admin.controller;

import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.common.iam.SamUser;
import bio.terra.common.iam.SamUserFactory;
import bio.terra.pearl.api.admin.api.ExampleApi;
import bio.terra.pearl.api.admin.config.SamConfiguration;
import bio.terra.pearl.api.admin.model.Example;
import bio.terra.pearl.api.admin.service.ExampleService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ExampleController implements ExampleApi {
  private final ExampleService exampleService;
  private final BearerTokenFactory bearerTokenFactory;
  private final SamUserFactory samUserFactory;
  private final SamConfiguration samConfiguration;
  private final HttpServletRequest request;

  public ExampleController(
      ExampleService exampleService,
      BearerTokenFactory bearerTokenFactory,
      SamUserFactory samUserFactory,
      SamConfiguration samConfiguration,
      HttpServletRequest request) {
    this.exampleService = exampleService;
    this.bearerTokenFactory = bearerTokenFactory;
    this.samUserFactory = samUserFactory;
    this.samConfiguration = samConfiguration;
    this.request = request;
  }

  private SamUser getUser() {
    // this automatically checks if the user is enabled
    return this.samUserFactory.from(request, samConfiguration.basePath());
  }

  /** Example of getting user information from sam. */
  @Override
  public ResponseEntity<String> getMessage() {
    var user = getUser();
    return ResponseEntity.of(
        this.exampleService.getExampleForUser(user.getSubjectId()).map(Example::message));
  }

  @Override
  public ResponseEntity<Void> setMessage(String body) {
    var user = getUser();
    this.exampleService.saveExample(new Example(user.getSubjectId(), body));
    return ResponseEntity.noContent().build();
  }

  /** Example of getting the bearer token and using it to make a Sam (or other service) api call */
  @Override
  public ResponseEntity<Boolean> getAction(String resourceType, String resourceId, String action) {
    var bearerToken = bearerTokenFactory.from(request);
    return ResponseEntity.ok(true);
  }
}
