package bio.terra.pearl.core.model.publishing;

import java.util.List;

/** record of a diff of lists of items.  T is the item type, CT is a representation of a diff for changed items */
public record ListChange<T, CT>(List<T> addedItems, List<T> removedItems, List<CT> changedItems) { }
