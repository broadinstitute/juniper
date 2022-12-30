package bio.terra.pearl.core.model.survey;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseData {
    private List<ResponseDataItem> items;
}


