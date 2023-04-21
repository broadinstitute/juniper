package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.workflow.DataChangeRecordDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles mapping ParsedSnapshots (typically received from the frontend) into objects.  This is done with stableIdMaps
 * which map question stableIds to the object properties they should be assigned to.
 */
@Service
public class AnswerProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(AnswerProcessingService.class);
    private ObjectMapper objectMapper;
    private ProfileService profileService;
    private DataChangeRecordDao dataChangeRecordDao;
    public AnswerProcessingService(ObjectMapper objectMapper, ProfileService profileService,
                                   DataChangeRecordDao dataChangeRecordDao) {
        this.objectMapper = objectMapper;
        this.profileService = profileService;
        this.dataChangeRecordDao = dataChangeRecordDao;
    }

    /** takes a response and a list of mappings and saves any appropriate updates to the data model
     * also logs the changes as persisted DataChangeRecords
     * */
    @Transactional
    public List<DataChangeRecord> processAllAnswerMappings(List<Answer> answers, List<AnswerMapping> mappings,
                                         PortalParticipantUser ppUser, UUID responsibleUserId, UUID enrolleeId,
                                                           UUID surveyId) {
        if (mappings.isEmpty()) {
            return new ArrayList<>();
        }
        UUID operationId = UUID.randomUUID();
        ObjectWithChangeLog<Profile> profileChanges = processProfileAnswerMappings(answers, mappings, ppUser);
        /**
         * for now, it's assumed these record updates are a small number at a time -- if this gets large, it
         * might be worth creating as a batch
         */
        profileChanges.changeRecords().stream().forEach(changeRecord -> {
            changeRecord.setResponsibleUserId(responsibleUserId);
            changeRecord.setPortalParticipantUserId(ppUser.getId());
            changeRecord.setEnrolleeId(enrolleeId);
            changeRecord.setSurveyId(surveyId);
            changeRecord.setOperationId(operationId);
            dataChangeRecordDao.create(changeRecord);
        });
        return profileChanges.changeRecords();
    }

    /**
     * Processes the snapshot and saves any corresponding updates to the participant profile.  if no updates are needed
     * this does not load the participant's profile, and instead returns an object with a null 'obj' and an empty changelist
     */
    @Transactional
    public ObjectWithChangeLog<Profile> processProfileAnswerMappings(List<Answer> answers, List<AnswerMapping> mappings,
                                             PortalParticipantUser ppUser) {
        List<AnswerMapping> profileMappings = mappings.stream().filter(mapping ->
                mapping.getTargetType().equals(AnswerMappingTargetType.PROFILE)).toList();
        if (profileMappings.isEmpty() || !hasTargetedChanges(profileMappings, answers, AnswerMappingTargetType.PROFILE)) {
            return new ObjectWithChangeLog<>(null, new ArrayList<>());
        }
        Profile profile = profileService.loadWithMailingAddress(ppUser.getProfileId()).get();
        ObjectWithChangeLog<Profile> profileChanges = mapValuesToType(answers, profileMappings,
                profile, AnswerMappingTargetType.PROFILE);
        profileService.updateWithMailingAddress(profile);
        return profileChanges;
    }

    /**
     * returns true if any answers in the list are specifically for the mapped fields.  Checking this first
     * saves loading the entire participant profile on every submission
     * */
    public boolean hasTargetedChanges(List<AnswerMapping> mappings, List<Answer> answers,
                                      AnswerMappingTargetType targetType) {
        HashMap<String, AnswerMapping> fieldTargetMap = new HashMap<>();
        mappings.stream().filter(mapping -> mapping.getTargetType().equals(targetType))
                .forEach(mapping -> fieldTargetMap.put(mapping.getQuestionStableId(), mapping));
        return answers.stream().anyMatch(answer -> fieldTargetMap.containsKey(answer.getQuestionStableId()));
    }

    /** returns the target object with the values from the snapshot mapped onto it.  Modifies the passed-in object */
    public <T> ObjectWithChangeLog<T> mapValuesToType(List<Answer> answers, List<AnswerMapping> mappings, T targetObj,
                                 AnswerMappingTargetType targetType) {
        HashMap<String, AnswerMapping> fieldTargetMap = new HashMap<>();
        List<DataChangeRecord> changeRecords = new ArrayList<>();
        mappings.stream().filter(mapping -> mapping.getTargetType().equals(targetType))
                .forEach(mapping -> fieldTargetMap.put(mapping.getQuestionStableId(), mapping));
        for (Answer answer : answers) {
            String stableId = answer.getQuestionStableId();
            if (fieldTargetMap.containsKey(stableId)) {
                try {
                    AnswerMapping mapping = fieldTargetMap.get(stableId);
                    String oldValue = Objects.toString(PropertyUtils.getNestedProperty(targetObj, mapping.getTargetField()), "");
                    BiFunction<Answer, AnswerMapping, Object> mapFunc = JSON_MAPPERS.get(mapping.getMapType());
                    Object newValue = mapFunc.apply(answer, mapping);
                    PropertyUtils.setNestedProperty(targetObj, mapping.getTargetField(), newValue);
                    DataChangeRecord changeRecord = DataChangeRecord.builder()
                            .modelName(targetType.name())
                            .fieldName(mapping.getTargetField())
                            .oldValue(oldValue)
                            // track the new value as what was actually set to catch type conversion errors
                            .newValue(Objects.toString(PropertyUtils.getNestedProperty(targetObj, mapping.getTargetField()), ""))
                            .build();
                    changeRecords.add(changeRecord);
                } catch (Exception e) {
                    String errorMsg = String.format("Error setting property %s, from stableId %s",
                            fieldTargetMap.get(stableId), stableId);
                    logger.error(errorMsg, e);
                }
            }
        }
        return new ObjectWithChangeLog<T>(targetObj, changeRecords);
    }

    public static final Map<AnswerMappingMapType, BiFunction<Answer, AnswerMapping, Object>> JSON_MAPPERS = Map.of(
            AnswerMappingMapType.STRING_TO_STRING, (Answer answer, AnswerMapping mapping) -> answer.getStringValue(),
            AnswerMappingMapType.STRING_TO_LOCAL_DATE, (Answer answer, AnswerMapping mapping) ->
                    mapToDate(answer.getStringValue(), mapping)
    );

    public static LocalDate mapToDate(String dateString, AnswerMapping mapping) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mapping.getFormatString());
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            if (mapping.isErrorOnFail()) {
                throw new IllegalArgumentException("Could not parse date " + dateString + " to format " + mapping.getFormatString());
            }
        }
        return null;
    }
}
