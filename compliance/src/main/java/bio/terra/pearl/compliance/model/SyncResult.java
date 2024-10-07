package bio.terra.pearl.compliance.model;

public class SyncResult {

    private boolean hasChanged;

    private final StringBuilder textSummary = new StringBuilder();

    public void appendToSummary(String message) {
        textSummary.append(message);
    }

    public boolean hasVantaDataChanged() {
        return hasChanged;
    }

    public void setHasVantaDataChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public String getTextSummary() {
        return textSummary.toString();
    }
}
