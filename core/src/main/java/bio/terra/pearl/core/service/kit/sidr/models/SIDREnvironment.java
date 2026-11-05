package bio.terra.pearl.core.service.kit.sidr.models;

public enum SIDREnvironment {
    PRODUCTION,
    STAGING,
    SANDBOX1,
    SANDBOX2,
    SANDBOX3,
    SANDBOX4,
    SANDBOX5;

    /**
     * parses the value stored in StudyEnvironmentConfig.sidrEnvironment (e.g. "production", "sandbox1")
     * @throws IllegalArgumentException if the value isn't a known SIDR environment
     */
    public static SIDREnvironment fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("No SIDR environment configured");
        }
        try {
            return SIDREnvironment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown SIDR environment: " + value, e);
        }
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

     public String getAPIURl() {
         if (this == SIDREnvironment.PRODUCTION) {
             return "https://gpo.broadinstitute.org";
         }
         return "https://gpo-" + this + ".broadinstitute.org";
    }

    public String getAPIAudience() {
        return "broad-gpo-" + this;
    }
}
