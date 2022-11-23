package bio.terra.pearl.core.service;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class CascadeTree {
    @Setter @Getter
    private Map<CascadeProperty, CascadeTree> childMap = new HashMap<>();
    public CascadeTree getChild(CascadeProperty cascadeProp) {
        return childMap.get(cascadeProp);
    }
    public boolean hasProperty(CascadeProperty cascadeProperty) {
        return childMap.containsKey(cascadeProperty);
    }
}
