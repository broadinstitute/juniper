package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipantUserFormatter extends BeanModuleFormatter<ParticipantUser> {
    public static final List<String> INCLUDED_PROPERTIES = List.of("shortcode", "username", "createdAt");

    public ParticipantUserFormatter(ExportOptions exportOptions) {
        super(exportOptions, "account", "Account");
    }

    @Override
    protected List<PropertyItemFormatter<ParticipantUser>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<>(propName, ParticipantUser.class))
                .collect(Collectors.toList());
    }

    @Override
    public ParticipantUser getBean(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getParticipantUser();
    }

    @Override
    public ParticipantUser newBean() {
        return new ParticipantUser();
    }
}
