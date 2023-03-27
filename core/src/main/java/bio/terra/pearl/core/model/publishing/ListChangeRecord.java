package bio.terra.pearl.core.model.publishing;

import java.util.List;

public record ListChangeRecord<T, CT>(List<T> addedItems, List<T> removedItems, List<CT> changedItems) { }
