package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.populate.dto.PortalPopDto;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExtractPopulateContext {
    @Getter
    private final ZipOutputStream zipOut;
    private Map<UUID, String> writtenEntities = new HashMap<>();
    @Getter
    private final PortalPopDto portalPopDto;

    public ExtractPopulateContext(Portal portal, ZipOutputStream zipOut) {
        this.zipOut = zipOut;
        portalPopDto = new PortalPopDto();
        portalPopDto.setShortcode(portal.getShortcode());
        portalPopDto.setName(portal.getName());
    }

    public void writeFileForEntity(String fileName, String fileContent, UUID entityId) {
        if (writtenEntities.containsKey(entityId)) {
            return;
        }
        try {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(fileContent.getBytes());
            zipOut.closeEntry();
            writtenEntities.put(entityId, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error writing file for entity: " + entityId, e);
        }
    }

    public String getFileNameForEntity(UUID entityId) {
        return writtenEntities.get(entityId);
    }
}
