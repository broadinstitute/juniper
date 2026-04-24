package bio.terra.pearl.core.model.file;

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
public class DownloadRecord extends BaseEntity {
    private UUID participantFileId;
    private UUID participantUserId;
    private UUID adminUserId;
    private UUID enrolleeId;
}
