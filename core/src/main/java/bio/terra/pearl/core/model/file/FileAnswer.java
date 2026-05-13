package bio.terra.pearl.core.model.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single file within a FILE_UPLOAD answer's object_value JSON array.
 * Downloads are populated at read time from the DownloadRecord table and not stored in the answer JSON.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnswer {
    private String fileName;
    private Instant uploadedAt;
    private UUID uploadingAdminUserId;
    private UUID uploadingParticipantUserId;

    @Builder.Default
    private List<DownloadRecord> downloads = new ArrayList<>();
}
