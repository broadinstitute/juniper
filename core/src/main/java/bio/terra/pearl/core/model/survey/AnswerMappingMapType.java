package bio.terra.pearl.core.model.survey;

public enum AnswerMappingMapType {
    STRING_TO_STRING, // String->String no-op
    STRING_TO_LOCAL_DATE, // answer is a string (e.g. "1987-7-24") to get converted to localDate
    STRING_TO_BOOLEAN // answer is a string (e.g. "true") to get converted to boolean

}
