package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LocalizedEmailTemplate extends BaseEntity {
    private UUID emailTemplateId;
    private String language;
    private String body;
    private String subject;
}
