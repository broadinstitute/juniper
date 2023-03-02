package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.workflow.DataChangeRecordDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.function.Function;
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
public class SnapshotProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotProcessingService.class);
    private ObjectMapper objectMapper;
    private ProfileService profileService;
    private DataChangeRecordDao dataChangeRecordDao;
    public SnapshotProcessingService(ObjectMapper objectMapper, ProfileService profileService,
                                     DataChangeRecordDao dataChangeRecordDao) {
        this.objectMapper = objectMapper;
        this.profileService = profileService;
        this.dataChangeRecordDao = dataChangeRecordDao;
    }

    /** takes a response and a list of mappings and saves any appropriate updates to the data model
     * also logs the changes as persisted DataChangeRecords
     * */
    @Transactional
    public List<DataChangeRecord> processAllAnswerMappings(ResponseData responseData, List<AnswerMapping> mappings,
                                         PortalParticipantUser ppUser, UUID responsibleUserId, UUID enrolleeId,
                                                           UUID surveyId) {
        if (mappings.isEmpty()) {
            return new ArrayList<>();
        }
        UUID operationId = UUID.randomUUID();
        ObjectWithChangeLog<Profile> profileChanges = processProfileAnswerMappings(responseData, mappings, ppUser);
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
    public ObjectWithChangeLog<Profile> processProfileAnswerMappings(ResponseData responseData, List<AnswerMapping> mappings,
                                             PortalParticipantUser ppUser) {
        List<AnswerMapping> profileMappings = mappings.stream().filter(mapping ->
                mapping.getTargetType().equals(AnswerMappingTargetType.PROFILE)).toList();
        if (profileMappings.isEmpty()) {
            return new ObjectWithChangeLog<>(null, new ArrayList<>());
        }
        Profile profile = profileService.loadWithMailingAddress(ppUser.getProfileId()).get();
        ObjectWithChangeLog<Profile> profileChanges = mapValuesToType(responseData, profileMappings,
                profile, AnswerMappingTargetType.PROFILE);
        profileService.updateWithMailingAddress(profile);
        return profileChanges;
    }

    /** returns the target object with the values from the snapshot mapped onto it.  Modifies the passed-in object */
    public <T> ObjectWithChangeLog<T> mapValuesToType(ResponseData responseData, List<AnswerMapping> mappings, T targetObj,
                                 AnswerMappingTargetType targetType) {
        HashMap<String, AnswerMapping> fieldTargetMap = new HashMap<>();
        List<DataChangeRecord> changeRecords = new ArrayList<>();
        mappings.stream().filter(mapping -> mapping.getTargetType().equals(targetType))
                .forEach(mapping -> fieldTargetMap.put(mapping.getQuestionStableId(), mapping));
        for (ResponseDataItem item : responseData.getItems()) {
            String stableId = item.getStableId();
            if (fieldTargetMap.containsKey(stableId) && item.getValue() != null) {
                try {
                    AnswerMapping mapping = fieldTargetMap.get(stableId);
                    String oldValue = Objects.toString(PropertyUtils.getNestedProperty(targetObj, mapping.getTargetField()), "");
                    Function<JsonNode, Object> mapFunc = JSON_MAPPERS.get(mapping.getMapType());
                    Object newValue = mapFunc.apply(item.getValue());
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

    /** legacy value extract method for hardcoded surveys with hardcoded mappings, like REGISTRATION_FIELD_MAP
     * in RegistrationService */
    public <T> T extractValues(ParsedSnapshot snapshot, Map<String, String> stableIdMap, Class<T> clazz) {
        Map<String, Object> fieldValues = new HashMap<>();
        for (ResponseDataItem item : snapshot.getParsedData().getItems()) {
            String stableId = item.getStableId();
            if (stableIdMap.containsKey(stableId)) {
                fieldValues.put(stableIdMap.get(stableId), item.getValue());
            }

        }
        return objectMapper.convertValue(fieldValues, clazz);
    }

    public static final Map<AnswerMappingMapType, Function<JsonNode, Object>> JSON_MAPPERS = Map.of(
            AnswerMappingMapType.TEXT_NODE_TO_STRING, (JsonNode jsonNode) -> jsonNode.asText()
    );
}
