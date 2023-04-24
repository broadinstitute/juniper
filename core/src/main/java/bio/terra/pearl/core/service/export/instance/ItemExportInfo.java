package bio.terra.pearl.core.service.export.instance;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** An 'item' corresponds to either a survey question or a specific field of a data model */
@Getter @Builder
public class ItemExportInfo {
    private boolean splitOptionsIntoColumns = false;
    private boolean stableIdsForOptions = true;
    private boolean allowMultiple = false;
    private boolean hasOtherDescription = false;
    // for now, we support bean property accesses, or question stableId answer access
    private String propertyAccessor;
    private String questionStableId;
    private String baseColumnKey;
    private List<QuestionChoice> choices = null;
}
