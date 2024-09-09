package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class VantaResults<T extends VantaObject> {

    VantaPageInfo pageInfo;

    List<T> data;
}