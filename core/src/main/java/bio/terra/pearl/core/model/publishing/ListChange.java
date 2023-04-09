package bio.terra.pearl.core.model.publishing;

import java.util.List;

public record ListChange<T, CT>(List<T> addedItems, List<T> removedItems, List<CT> changedItems) { }
