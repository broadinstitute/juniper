package bio.terra.javatemplate.iam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import bio.terra.javatemplate.config.SamConfiguration;
import org.broadinstitute.dsde.workbench.client.sam.ApiClient;
import org.broadinstitute.dsde.workbench.client.sam.auth.OAuth;
import org.junit.jupiter.api.Test;

class SamClientTest {
  private static final String BASE_PATH = "basepath";
  private static final String TOKEN = "token";
  private static final String AUTH_NAME = "b2coauth";

  private final SamClient client = new SamClient(new SamConfiguration(BASE_PATH));

  @Test
  void testApis() {
    validateClient(client.statusApi().getApiClient(), null);
    validateClient(client.usersApi(TOKEN).getApiClient(), TOKEN);
    validateClient(client.resourcesApi(TOKEN).getApiClient(), TOKEN);
  }

  private static void validateClient(ApiClient client, String token) {
    assertThat(client.getBasePath(), is(BASE_PATH));
    OAuth oauth = (OAuth) client.getAuthentication(AUTH_NAME);
    assertThat(oauth.getAccessToken(), is(token));
  }
}
