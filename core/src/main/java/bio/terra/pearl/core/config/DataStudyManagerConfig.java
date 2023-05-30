package bio.terra.pearl.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
public class DataStudyManagerConfig {
    private String basePath;
    private String secret;

    public DataStudyManagerConfig(Environment environment) {
        this.basePath = environment.getProperty("env.dsm.basePath");
        this.secret = environment.getProperty("env.dsm.secret");
    }
}
