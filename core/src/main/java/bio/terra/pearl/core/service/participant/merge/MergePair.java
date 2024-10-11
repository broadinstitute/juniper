package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
@Setter
public class MergePair<T extends BaseEntity> {
    private T source;
    private T target;

    public MergePair(T source, T target) {
        this.source = source;
        this.target = target;
    }

    public static <D extends BaseEntity> List<MergePair<D>> pairLists(List<D> sourceList, List<D> targetList, BiFunction<D, D, Boolean> comparator) {
        List<MergePair<D>> pairs = new ArrayList<>();
        for (D source : sourceList) {
            D target = targetList.stream().filter(t -> comparator.apply(source, t)).findFirst().orElse(null);
            pairs.add(new MergePair<D>(source, target));
        }
        for (D target : targetList) {
            D source = sourceList.stream().filter(t -> comparator.apply(target, t)).findFirst().orElse(null);
            // we only need to add unmatched targets -- matched will have been handled in the loop above
            if (source == null) {
                pairs.add(new MergePair<D>(source, target));
            }
        }
        return pairs;
    }

    public PairType getPairType() {
        if (source == null && target != null) {
            return PairType.TARGET_ONLY;
        } else if (source != null && target == null) {
            return PairType.SOURCE_ONLY;
        } else {
            return PairType.BOTH;
        }
    }

    public enum PairType {
        SOURCE_ONLY,
        TARGET_ONLY,
        BOTH
    }
}
