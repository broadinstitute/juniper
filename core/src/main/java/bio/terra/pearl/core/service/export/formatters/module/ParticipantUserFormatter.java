package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipantUserFormatter extends BeanModuleFormatter<ParticipantUser> {
    public static final String PARTICIPANT_USER_MODULE_NAME = "account";
    public static final List<String> INCLUDED_PROPERTIES = List.of("username", "createdAt");

    public ParticipantUserFormatter(ExportOptions exportOptions) {
        itemFormatters = INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<ParticipantUser>(propName, ParticipantUser.class))
                .collect(Collectors.toList());
        moduleName = PARTICIPANT_USER_MODULE_NAME;
        displayName = "Account";
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
