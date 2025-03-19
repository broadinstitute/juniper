package bio.terra.pearl.core.model.file;

import bio.terra.pearl.core.service.file.VirusScanResult;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class ScannedParticipantFileDto extends ParticipantFile {
    private VirusScanResult virusScanResult;

    public ScannedParticipantFileDto(ParticipantFile participantFile, VirusScanResult virusScanResult) {
        BeanUtils.copyProperties(participantFile, this);

        this.virusScanResult = virusScanResult;
    }
}
