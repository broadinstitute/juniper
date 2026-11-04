package bio.terra.pearl.pepper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans up a converted SurveyJS definition so it follows Juniper conventions rather than Pepper's:
 * camelCase element names (including references in expressions) and no stray template whitespace.
 * Also removes questions that Pepper no longer uses.
 */
public class SurveyNormalizer {
    private static final Pattern REF_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final List<String> EXPRESSION_KEYS = List.of("visibleIf", "enableIf", "requiredIf");
    private static final List<String> CHILD_KEYS = List.of("elements", "templateElements");

    private final ObjectMapper objectMapper;
    /** old name -> new name, for every renamed element. Dynamic panel children are keyed "panel/child" */
    private final Map<String, String> renames = new LinkedHashMap<>();

    public SurveyNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> getRenames() {
        return renames;
    }

    /** removes the named questions, and any element whose visibility depends on one of them */
    public void removeQuestions(JsonNode surveyJson, Set<String> names) {
        for (JsonNode page : surveyJson.get("pages")) {
            removeFrom((ObjectNode) page, names);
        }
    }

    private void removeFrom(ObjectNode parent, Set<String> names) {
        for (String key : CHILD_KEYS.stream().filter(parent::has).toList()) {
            ArrayNode children = (ArrayNode) parent.get(key);
            Iterator<JsonNode> it = children.iterator();
            while (it.hasNext()) {
                ObjectNode child = (ObjectNode) it.next();
                String vis = child.has("visibleIf") ? child.get("visibleIf").asText() : "";
                boolean dependsOnRemoved = names.stream().anyMatch(n -> vis.contains("{" + n + "}"));
                if (names.contains(child.path("name").asText()) || dependsOnRemoved) {
                    it.remove();
                } else {
                    removeFrom(child, names);
                }
            }
        }
    }

    public void normalize(JsonNode surveyJson) {
        Map<String, String> newNames = new HashMap<>();
        for (JsonNode page : surveyJson.get("pages")) {
            collect(page, new HashMap<>(), newNames);
        }
        newNames.forEach((old, renamed) -> {
            if (!old.equals(renamed)) {
                renames.put(old, renamed);
            }
        });
        for (JsonNode page : surveyJson.get("pages")) {
            apply(page, newNames);
        }
        cleanWhitespace(surveyJson);
    }

    /**
     * Computes new names, checking they're unique within their scope. Questions inside a dynamic panel are scoped to
     * that panel (e.g. both "sibling" and "child" can have a "race" question).
     */
    private void collect(JsonNode node, Map<String, String> scope, Map<String, String> newNames) {
        for (String key : CHILD_KEYS) {
            if (!node.has(key)) {
                continue;
            }
            // template elements of a dynamic panel get their own scope
            Map<String, String> childScope = key.equals("templateElements") ? new HashMap<>() : scope;
            for (JsonNode child : node.get(key)) {
                if (child.has("name")) {
                    String old = child.get("name").asText();
                    String renamed = camelCase(old);
                    String existing = childScope.putIfAbsent(renamed, old);
                    if (existing != null && !existing.equals(old)) {
                        throw new IllegalStateException("Name collision after normalizing: " + old + " and " + existing + " -> " + renamed);
                    }
                    newNames.put(old, renamed);
                }
                collect(child, childScope, newNames);
            }
        }
    }

    private void apply(JsonNode node, Map<String, String> newNames) {
        if (!node.isObject()) {
            return;
        }
        ObjectNode obj = (ObjectNode) node;
        if (obj.has("name") && newNames.containsKey(obj.get("name").asText())) {
            obj.put("name", newNames.get(obj.get("name").asText()));
        }
        for (String key : EXPRESSION_KEYS) {
            if (obj.has(key) && obj.get(key).isTextual()) {
                obj.put(key, renameRefs(obj.get(key).asText(), newNames));
            }
        }
        for (String key : CHILD_KEYS) {
            if (obj.has(key)) {
                obj.get(key).forEach(child -> apply(child, newNames));
            }
        }
    }

    private String renameRefs(String expr, Map<String, String> newNames) {
        return REF_PATTERN.matcher(expr).replaceAll(m -> {
            String ref = m.group(1);
            String prefix = ref.startsWith("panel.") ? "panel." : "";
            String bare = ref.substring(prefix.length());
            return Matcher.quoteReplacement("{" + prefix + newNames.getOrDefault(bare, bare) + "}");
        });
    }

    /** Pepper templates keep their source line breaks and indentation, which render as stray whitespace */
    private void cleanWhitespace(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            for (Iterator<String> it = obj.fieldNames(); it.hasNext(); ) {
                String key = it.next();
                JsonNode value = obj.get(key);
                if (value.isTextual()) {
                    obj.put(key, value.asText().replaceAll("\\s*\\n\\s*", " ").trim());
                } else {
                    cleanWhitespace(value);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).isTextual()) {
                    arr.set(i, objectMapper.getNodeFactory().textNode(arr.get(i).asText().replaceAll("\\s*\\n\\s*", " ").trim()));
                } else {
                    cleanWhitespace(arr.get(i));
                }
            }
        }
    }

    /** UPPER_SNAKE (or snake_case) to lowerCamel, preserving dots, e.g. father.deceased_dna -> father.deceasedDna */
    static String camelCase(String name) {
        StringBuilder out = new StringBuilder();
        for (String dotPart : name.split("\\.", -1)) {
            if (out.length() > 0) {
                out.append('.');
            }
            String[] words = dotPart.toLowerCase().split("_");
            for (int i = 0; i < words.length; i++) {
                String w = words[i];
                if (w.isEmpty()) {
                    continue;
                }
                boolean first = i == 0;
                out.append(first ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1));
            }
        }
        return out.toString();
    }
}
