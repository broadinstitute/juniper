package bio.terra.pearl.populate.dto.fileupload;

import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantFilePopDto extends ParticipantFile implements FilePopulatable {
    String populateFileName;

    String fileContent;

    List<DownloadRecordPopDto> downloadRecordPopDtos = new ArrayList<>();
}
