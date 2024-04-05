package bio.terra.pearl.core.model;

import java.util.UUID;

/**
 * Versioned is used for customer-editable documents.  All documents must have a stableId and version, and the stableID-version
 * pair must be unique.  stableId-publishedVersion pairs must also be unique, but not every document is required to have a publishedVersion
 *
 * PublishedVersion is only intended as a cosmetic feature for users to "tag" documents.  All application logic and data fetching should be based on
 * version.  this ensures that the application will operate the same in the sandbox environment as it does in live environments.
 */
public interface Versioned {
    public String getStableId();

    public UUID getPortalId();
    public int getVersion();
    public void setVersion(int version);
    public Integer getPublishedVersion();
    public void setPublishedVersion(Integer publishedVersion);
}
