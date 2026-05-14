package bio.terra.pearl.core.model.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents a single file within a FILE_UPLOAD answer's object_value JSON array.
 * Stores only the UUID of the corresponding ParticipantFile.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnswer {
    private UUID participantFileId;
}
