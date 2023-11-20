package bio.terra.pearl.populate.service.export;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExportContext {
    private Map<UUID, String> exportedEntities = new HashMap<>();

    public void markEntityAsExported(UUID entityId, String fileName) {
        exportedEntities.put(entityId, fileName);
    }
}
