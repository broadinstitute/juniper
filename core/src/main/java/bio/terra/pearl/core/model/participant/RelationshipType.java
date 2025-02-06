package bio.terra.pearl.core.model.participant;

public enum RelationshipType{
    PROXY, FAMILY;
    public static boolean isProxy(RelationshipType type) {
        return PROXY.equals(type);
    }
}
