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
public class CoreLanguageText extends BaseEntity {
    private String messageKey;
    private String text;
    private String language;
}
