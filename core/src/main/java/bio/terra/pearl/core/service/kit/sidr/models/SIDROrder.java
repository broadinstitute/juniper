package bio.terra.pearl.core.service.kit.sidr.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SIDROrder {
    private String externalOrderId;
    private Boolean clinicalOrderAttestation;
    private String projectKey;
    private List<SIDRMetadata> metadata;
    private List<SIDRTest> tests;
    private List<SIDRSample> samples;
    private SIDRClinician clinician;
    private Integer numberOfSamples;
    private String quote;
    private Boolean sampleKitRequested;
    private Boolean bulkUploadRequested;
    private String orderId;
    private String projectId;
    private SIDRLinks links;
    private SIDRMessages messages;

}
