package bio.terra.pearl.core.service;

import java.util.HashMap;
import java.util.Map;

public class CascadeTree {

    private Map<CascadeProperty, CascadeTree> childMap = new HashMap<>();
    public CascadeTree getChild(CascadeProperty cascadeProp) {
        return childMap.get(cascadeProp);
    }
    public boolean hasProperty(CascadeProperty cascadeProperty) {
        return childMap.containsKey(cascadeProperty);
    }

    public CascadeTree() {
    }

    public CascadeTree(CascadeProperty property) {
        childMap.put(property, new CascadeTree());
    }

    public CascadeTree(CascadeProperty property, CascadeTree subtree) {
        childMap.put(property, subtree);
    }
}
