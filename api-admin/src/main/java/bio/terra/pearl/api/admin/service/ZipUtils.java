package bio.terra.pearl.api.admin.service;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.web.multipart.MultipartFile;

/** adapted from https://www.baeldung.com/java-compress-and-uncompress */
public class ZipUtils {
  public static void unzipFile(File destDir, MultipartFile file) throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(file.getBytes()));
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      File newFile = newFile(destDir, zipEntry);
      if (zipEntry.isDirectory()) {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
          throw new IOException("Failed to create directory " + newFile);
        }
      } else {
        // fix for Windows-created archives
        File parent = newFile.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
          throw new IOException("Failed to create directory " + parent);
        }

        // write file content
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
      }
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
  }

  public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }
}
