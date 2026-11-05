package bio.terra.pearl.core.service.kit.sidr.models;

public enum SIDREnvironment {
    PRODUCTION,
    STAGING,
    SANDBOX1,
    SANDBOX2,
    SANDBOX3,
    SANDBOX4,
    SANDBOX5;

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
