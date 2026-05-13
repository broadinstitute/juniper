package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileUploadFormatter extends BeanListModuleFormatter<FileUploadFormatter.FileUploadExportDto> {

    private static final List<String> EXPORT_PROPERTIES =
            List.of("fileName", "fileType", "uploadedAt", "downloadCount", "firstParticipantDownloadAt");

    public FileUploadFormatter(ExportOptions options) {
        super(options, "file_upload", "File uploads");
    }

    @Override
    protected List<PropertyItemFormatter<FileUploadExportDto>> generateItemFormatters(ExportOptions options) {
        return EXPORT_PROPERTIES.stream()
                .map(prop -> new PropertyItemFormatter<>(prop, FileUploadExportDto.class, options.getZoneId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FileUploadExportDto> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getParticipantFiles().stream()
                .map(FileUploadExportDto::from)
                .sorted(Comparator.comparing(FileUploadExportDto::getUploadedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public Comparator<FileUploadExportDto> getComparator() {
        return Comparator.comparing(FileUploadExportDto::getUploadedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileUploadExportDto {
        private String fileName;
        private String fileType;
        private Instant uploadedAt;
        private int downloadCount;
        private Instant firstParticipantDownloadAt;

        public static FileUploadExportDto from(ParticipantFile file) {
            Instant firstParticipantDownload = file.getDownloads().stream()
                    .filter(r -> r.getParticipantUserId() != null)
                    .map(DownloadRecord::getCreatedAt)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            return FileUploadExportDto.builder()
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .uploadedAt(file.getCreatedAt())
                    .downloadCount(file.getDownloads().size())
                    .firstParticipantDownloadAt(firstParticipantDownload)
                    .build();
        }
    }
}
