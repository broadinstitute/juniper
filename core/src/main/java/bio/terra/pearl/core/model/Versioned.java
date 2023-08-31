package bio.terra.pearl.core.model;

public interface Versioned {
    public String getStableId();
    public int getVersion();
    public void setVersion(int version);
    public Integer getPublishedVersion();
    public void setPublishedVersion(Integer publishedVersion);
}
