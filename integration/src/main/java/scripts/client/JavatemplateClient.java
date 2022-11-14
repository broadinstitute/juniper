package scripts.client;

import bio.terra.javatemplate.client.ApiClient;
import bio.terra.testrunner.common.utils.AuthenticationUtils;
import bio.terra.testrunner.runner.config.ServerSpecification;
import bio.terra.testrunner.runner.config.TestUserSpecification;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.Objects;

public class JavatemplateClient extends ApiClient {

  /**
   * Build a no-auth API client object for the service. No access token is needed for this API
   * client.
   *
   * @param server the server we are testing against
   */
  public JavatemplateClient(ServerSpecification server) throws IOException {
    this(server, null);
  }

  /**
   * Build an API client object for the given test user for the service. The test user's token is
   * always refreshed. If a test user isn't configured (e.g. when running locally), return an
   * un-authenticated client.
   *
   * @param server the server we are testing against
   * @param testUser the test user whose credentials are supplied to the API client object
   */
  public JavatemplateClient(ServerSpecification server, TestUserSpecification testUser)
      throws IOException {
    // note that this uses server.catalogUri. Typically a uri for a new service needs to be added to
    // https://github.com/DataBiosphere/terra-test-runner/blob/main/src/main/java/bio/terra/testrunner/runner/config/ServerSpecification.java
    // but for this template we will stick with catalog
    setBasePath(Objects.requireNonNull(server.catalogUri, "Catalog URI required"));

    if (testUser != null) {
      GoogleCredentials userCredential =
          AuthenticationUtils.getDelegatedUserCredential(
              testUser, AuthenticationUtils.userLoginScopes);
      var accessToken = AuthenticationUtils.getAccessToken(userCredential);
      if (accessToken != null) {
        setAccessToken(accessToken.getTokenValue());
      }
    }
  }
}
