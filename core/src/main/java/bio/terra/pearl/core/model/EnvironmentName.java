package bio.terra.pearl.core.model;
/** enum is lowercase to make it easier to incorporate in urls */
public enum EnvironmentName {
    sandbox,
    irb,
    live;
    public static EnvironmentName valueOfCaseInsensitive(String value) {
        return EnvironmentName.valueOf(value.toLowerCase());
    }
}
