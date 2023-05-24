package bio.terra.pearl.core.service.exception.datarepo;

import bio.terra.pearl.core.service.exception.NotFoundException;
import lombok.Getter;

import java.util.UUID;

public class DatasetNotFoundException extends NotFoundException {

    public DatasetNotFoundException(UUID id) {
        super("Dataset not found: %s".formatted(id));
    }

    public DatasetNotFoundException(String datasetName) {
        super("Dataset not found: %s".formatted(datasetName));
    }

}
