package bio.terra.pearl.core.service.address;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AddressValidationConfig {

    private String addressValidationClass;

    public AddressValidationConfig(Environment environment) {
        this.addressValidationClass = environment.getProperty("env.addrValidation.addrValidationClientClass");
    }
}
