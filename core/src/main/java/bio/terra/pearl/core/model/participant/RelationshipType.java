package bio.terra.pearl.core.model.participant;

public enum RelationshipType{
    PROXY;
    public boolean isProxy(RelationshipType type) {
        return PROXY.equals(type);
    }
}
