package bio.terra.pearl.core.service;

import java.util.HashSet;
import java.util.Set;

/** Marker interface for denoting allowable cascade properties */
public interface CascadeProperty {
    public static Set<CascadeProperty> EMPTY_SET = new HashSet<>();
}

