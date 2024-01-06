package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.populate.dto.PortalPopDto;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    /** write a file to the zip file, but only if it hasn't already been written */
    public void writeFileForEntity(String fileName, String fileContent, UUID entityId) {
        writeFileForEntity(fileName, fileContent.getBytes(), entityId);
    }

    /** write a file to the zip file, but only if it hasn't already been written */
    public void writeFileForEntity(String fileName, byte[] bytes, UUID entityId) {
        if (writtenEntities.containsKey(entityId)) {
            return;
        }
        try {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(bytes);
            zipOut.closeEntry();
            writtenEntities.put(entityId, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error writing file for entity: " + entityId, e);
        }
    }

    /** write multiple files to the zip file for a single entity (e.g. an email template) */
    public void writeFilesForEntity(List<String> fileNames, List<String> fileContents, UUID entityId) {
        if (writtenEntities.containsKey(entityId)) {
            return;
        }
        if (fileNames.size() != fileContents.size()) {
            throw new IllegalArgumentException("Error writing files for entity: " + entityId + " - fileNames and fileContents must be the same size");
        }
        try {
            for (int i = 0; i < fileNames.size(); i++) {
                zipOut.putNextEntry(new ZipEntry(fileNames.get(i)));
                zipOut.write(fileContents.get(i).getBytes());
                zipOut.closeEntry();
            }
            writtenEntities.put(entityId, fileNames.get(0));
        } catch (IOException e) {
            throw new RuntimeException("Error writing file for entity: " + entityId, e);
        }
    }

    /** get the file name for an entity that has already been written */
    public String getFileNameForEntity(UUID entityId) {
        return writtenEntities.get(entityId);
    }
}
