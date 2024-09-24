package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
