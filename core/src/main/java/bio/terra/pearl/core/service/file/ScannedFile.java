package bio.terra.pearl.core.service.file;

import java.io.InputStream;

public record ScannedFile(InputStream file, VirusScanResult scanResult) {
}
