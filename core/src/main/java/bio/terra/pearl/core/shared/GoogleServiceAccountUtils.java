package bio.terra.pearl.core.shared;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableSet;

import java.io.FileInputStream;
import java.io.IOException;

public class GoogleServiceAccountUtils {

    private static final ImmutableSet<String> SERVICE_ACCOUNT_SCOPES = ImmutableSet.of("profile", "email", "openid");

    public static AccessToken getServiceAccountToken(GoogleCredentials serviceAccountCredentials) {
        //Google service account tokens do not include the necessary scopes by default. We need to explicitly
        //scope the credential to include the profile information so Terra's API can recognize it as a user.
        GoogleCredentials scopedCredentials = serviceAccountCredentials.createScoped(SERVICE_ACCOUNT_SCOPES);

        AccessToken refreshedAccessToken;
        try {
            refreshedAccessToken = scopedCredentials.refreshAccessToken();
        } catch (IOException e) {
            throw new RuntimeException("Error generating service account access token: " + e.getMessage());
        }

        return refreshedAccessToken;
    }

    public static GoogleCredentials parseCredentials(String pathToCredentials) {
        GoogleCredentials serviceAccountCredentials;

        try {
            serviceAccountCredentials = GoogleCredentials.fromStream(new FileInputStream(pathToCredentials));
        } catch (IOException e) {
            throw new RuntimeException("Error loading TDR service account credentials: " + e.getMessage());
        }

        return serviceAccountCredentials;
    }

}
