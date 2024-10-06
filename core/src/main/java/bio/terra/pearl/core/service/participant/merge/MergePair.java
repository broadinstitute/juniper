package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MergePair<T extends BaseEntity> {
    private T source;
    private T target;

    public MergePair(T source, T target) {
        this.source = source;
        this.target = target;
    }
}
