package bio.terra.pearl.pepper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
class SurveyImportContext {

    Map<String, Map<String, Object>> allLangMap;

    String formStableId;
    Set<String> contentBlockStableIds = new HashSet<>();
}
