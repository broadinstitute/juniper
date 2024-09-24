package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.Boolean.parseBoolean;

/**
 * Handles mapping ParsedSnapshots (typically received from the frontend) into objects.  This is done with stableIdMaps
 * which map question stableIds to the object properties they should be assigned to.
 */
@Service
@Slf4j
public class AnswerProcessingService {
    private final ProfileService profileService;
    private final PortalParticipantUserService portalParticipantUserService;

    public AnswerProcessingService(ProfileService profileService, PortalParticipantUserService portalParticipantUserService) {
        this.profileService = profileService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    /** takes a response and a list of mappings and saves any appropriate updates to the data model
     * also logs the changes as persisted DataChangeRecords
     * */
    @Transactional
    public void processAllAnswerMappings(
            Enrollee enrollee,
            List<Answer> answers,
            List<AnswerMapping> mappings,
            PortalParticipantUser operatorPpUser,
            ResponsibleEntity operator,
            DataAuditInfo auditInfo) {
        if (mappings.isEmpty()) {
            return;
        }
        processProfileAnswerMappings(enrollee, answers, mappings, operator, auditInfo);
        processProxyProfileAnswerMappings(enrollee, answers, mappings, operatorPpUser, auditInfo);
    }

    /**
     * Processes the snapshot and saves any corresponding updates to the participant profile.  if no updates are needed
     * this does not load the participant's profile, and instead returns an object with a null 'obj' and an empty changelist
     */
    @Transactional
    public void processProfileAnswerMappings(
            Enrollee enrollee,
            List<Answer> answers,
            List<AnswerMapping> mappings,
            ResponsibleEntity operator,
            DataAuditInfo auditInfo) {
        List<AnswerMapping> profileMappings = mappings.stream().filter(mapping ->
                mapping.getTargetType().equals(AnswerMappingTargetType.PROFILE)).toList();
        if (profileMappings.isEmpty() || !hasTargetedChanges(profileMappings, answers, AnswerMappingTargetType.PROFILE)) {
            return;
        }
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId()).get();

        mapValuesToType(
                answers,
                profileMappings,
                profile,
                AnswerMappingTargetType.PROFILE);

        profileService.updateWithMailingAddress(profile, auditInfo);
    }

    @Transactional
    public void processProxyProfileAnswerMappings(
            Enrollee enrollee,
            List<Answer> answers,
            List<AnswerMapping> mappings,
            PortalParticipantUser operator,
            DataAuditInfo auditInfo) {

        // if the ppUser is the same as the enrollee, we're not in a proxy environment
        if (operator.getParticipantUserId().equals(enrollee.getParticipantUserId())) {
            return;
        }

        List<AnswerMapping> proxyProfileMappings = mappings.stream().filter(mapping ->
                mapping.getTargetType().equals(AnswerMappingTargetType.PROXY_PROFILE)).toList();

        if (proxyProfileMappings.isEmpty() || !hasTargetedChanges(proxyProfileMappings, answers, AnswerMappingTargetType.PROXY_PROFILE)) {
            return;
        }

        // grab the operator (which is the proxy) profile to update it
        Profile profile = profileService.loadWithMailingAddress(operator.getProfileId()).get();
        mapValuesToType(
                answers,
                proxyProfileMappings,
                profile,
                AnswerMappingTargetType.PROXY_PROFILE);
        profileService.updateWithMailingAddress(profile, auditInfo);
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
        List<ParticipantDataChange> changeRecords = new ArrayList<>();
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
                    ParticipantDataChange changeRecord = ParticipantDataChange.builder()
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
                    log.error(errorMsg, e);
                }
            }
        }
        return new ObjectWithChangeLog<T>(targetObj, changeRecords);
    }

    public static final Map<AnswerMappingMapType, BiFunction<Answer, AnswerMapping, Object>> JSON_MAPPERS = Map.of(
            AnswerMappingMapType.STRING_TO_STRING, (Answer answer, AnswerMapping mapping) -> StringUtils.trim(answer.getStringValue()),
            AnswerMappingMapType.STRING_TO_LOCAL_DATE, (Answer answer, AnswerMapping mapping) ->
                    mapToDate(answer.getStringValue(), mapping),
            AnswerMappingMapType.STRING_TO_BOOLEAN, (Answer answer, AnswerMapping mapping) -> parseBoolean(answer.getStringValue())
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
