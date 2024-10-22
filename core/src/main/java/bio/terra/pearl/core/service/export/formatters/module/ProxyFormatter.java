package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyFormatter extends BeanListModuleFormatter<ParticipantUser> {
    public static final List<String> INCLUDED_PROPERTIES = List.of("username");

    public ProxyFormatter(ExportOptions options) {
        super(options, "proxy", "Proxies");
    }

    @Override
    public List<ParticipantUser> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getProxyUsers();
    }

    @Override
    public Comparator<ParticipantUser> getComparator() {
        return Comparator.comparing(ParticipantUser::getUsername);
    }


    @Override
    protected List<PropertyItemFormatter<ParticipantUser>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<ParticipantUser>(propName, ParticipantUser.class))
                .collect(Collectors.toList());
    }
}
