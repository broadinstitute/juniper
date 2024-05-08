package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;

import java.util.Collection;
import java.util.UUID;

public interface PepperDSMClient {
    /**
     * Sends a sample kit request to Pepper.
     *
     * @param studyShortcode    the shortcode of the Juniper study
     * @param enrollee          the enrollee to receive the sample kit
     * @param kitRequest        sample kit request details
     * @param address           mailing address for the sample kit
     * @return status result from Pepper
     * @throws PepperApiException on error from Pepper or failure to process the Pepper response
     */
    PepperKit sendKitRequest(String studyShortcode, StudyEnvironmentConfig studyEnvironmentConfig, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) throws PepperApiException, PepperParseException;
    PepperKit fetchKitStatus(StudyEnvironmentConfig studyEnvironmentConfig, UUID kitRequestId) throws PepperApiException, PepperParseException;
    Collection<PepperKit> fetchKitStatusByStudy(String studyShortcode, StudyEnvironmentConfig studyEnvironmentConfig) throws PepperApiException, PepperParseException;
}
