package bio.terra.pearl.core.model.migration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class B2cValidationError {

    // Format from:
    // https://learn.microsoft.com/en-us/azure/active-directory-b2c/restful-technical-profile#returning-validation-error-message
    private String version = "1.0.0";
    // required to be 409... for some reason...
    private Integer status = 409;
    private String userMessage;

    public B2cValidationError(String userMessage) {
        this.userMessage = userMessage;
    }
}
