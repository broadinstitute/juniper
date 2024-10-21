package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class StudyFormatter extends BeanModuleFormatter<Study> {
    public static final List<String> INCLUDED_PROPERTIES = List.of(
            "shortcode"
    );

    public StudyFormatter(ExportOptions exportOptions) {
        super(exportOptions, "study", "Study");
    }

    @Override
    protected List<PropertyItemFormatter<Study>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<>(propName, Study.class))
                .collect(Collectors.toList());
    }

    @Override
    public Study getBean(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getStudy();
    }

    @Override
    public Study newBean() {
        return new Study();
    }
}
