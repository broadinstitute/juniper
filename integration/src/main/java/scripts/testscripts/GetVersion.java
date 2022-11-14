package scripts.testscripts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.javatemplate.api.PublicApi;
import bio.terra.testrunner.runner.TestScript;
import bio.terra.testrunner.runner.config.TestUserSpecification;
import com.google.api.client.http.HttpStatusCodes;
import scripts.client.JavatemplateClient;

public class GetVersion extends TestScript {
  @Override
  public void userJourney(TestUserSpecification testUser) throws Exception {
    JavatemplateClient client = new JavatemplateClient(server);
    var publicApi = new PublicApi(client);

    var versionProperties = publicApi.getVersion();

    // check the response code
    assertThat(client.getStatusCode(), is(HttpStatusCodes.STATUS_CODE_OK));

    // check the response body
    assertThat(versionProperties.getGitHash(), notNullValue());
    assertThat(versionProperties.getGitTag(), notNullValue());
    assertThat(versionProperties.getGithub(), notNullValue());
    assertThat(versionProperties.getBuild(), notNullValue());
  }
}
