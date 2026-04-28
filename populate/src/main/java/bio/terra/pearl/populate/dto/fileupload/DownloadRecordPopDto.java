package bio.terra.pearl.populate.dto.fileupload;

import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DownloadRecordPopDto implements TimeShiftable {
    /** username of the participant user who downloaded; if null, defaults to the enrollee's own participant user */
    private String linkedUsername;
    private Integer submittedHoursAgo;
}
