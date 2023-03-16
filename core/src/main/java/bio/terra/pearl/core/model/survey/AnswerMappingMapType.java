package bio.terra.pearl.core.model.survey;

public enum AnswerMappingMapType {
    TEXT_NODE_TO_STRING, // the answer will come in as a TextNode and get converted to a string
    TEXT_NODE_TO_LOCAL_DATE // answer is a string (e.g. "3/5/1987") and get converted to localDate
}
