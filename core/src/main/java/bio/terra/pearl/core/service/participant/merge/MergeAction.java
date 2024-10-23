package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class MergeAction<T extends BaseEntity, MP> {
    private MergePair<T> pair;
    private Action action;
    private MP mergePlan; // if the action is MERGE, this is the merge plan

    public MergeAction(MergePair<T> pair, Action action) {
        this.pair = pair;
        this.action = action;
    }

    public MergeAction(MergePair<T> pair, Action action, MP mergePlan) {
        if (action ==Action.MOVE_SOURCE && pair.getSource() == null) {
            throw new IllegalArgumentException("source cannot be null for MOVE_SOURCE action");
        }
        if (action ==Action.DELETE_SOURCE && pair.getSource() == null) {
            throw new IllegalArgumentException("source cannot be null for DELETE_SOURCE action");
        }
        if (action == Action.MERGE && (pair.getSource() == null || pair.getTarget() == null)) {
            throw new IllegalArgumentException("MERGE requires a source, target and plan");
        }
        this.pair = pair;
        this.action = action;
        this.mergePlan = mergePlan;
    }

    @JsonIgnore
    public T getSource() {
        return pair.getSource();
    }

    @JsonIgnore
    public T getTarget() {
        return pair.getTarget();
    }

    public enum Action {
        MOVE_SOURCE, // no change to target, reassign source to target (not a delete/recreate, just a reassign)
        NO_ACTION, // nothing
        MERGE, // do some logic to reconcile source and target
        DELETE_SOURCE, // delete the source, likely because it is empty or a pure dupe
        MOVE_SOURCE_DELETE_TARGET // move source to target and delete target
    }
}
