package bio.terra.pearl.core.model.i18n;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LanguageText extends BaseEntity {
    private String keyName;
    private String text;
    private String language;
}
