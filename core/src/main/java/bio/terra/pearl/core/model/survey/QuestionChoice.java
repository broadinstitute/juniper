package bio.terra.pearl.core.model.survey;

import lombok.Builder;

public record QuestionChoice(String stableId, String text) {
    @Builder public QuestionChoice {}
}
