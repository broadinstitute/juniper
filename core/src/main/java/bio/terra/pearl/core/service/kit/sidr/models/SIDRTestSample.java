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

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
/** the status of one sample for one test on an order. Only returned by SIDR, never sent. */
public class SIDRTestSample {
    private String sampleId;
    private String sidrSampleId;
    private String clientBarcode;
    private String testSampleStatus;
    private List<SIDRStatus> statusHistory;
    private String cancellationReason;
    private String lastModified;
}
