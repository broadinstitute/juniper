package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.EnrolleeRelationDto;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;


@Component
public class WithdrawnEnrolleeDao extends BaseJdbiDao<WithdrawnEnrollee> {
  private ProfileDao profileDao;
  private SurveyResponseDao surveyResponseDao;
  private ConsentResponseDao consentResponseDao;
  private ParticipantTaskDao participantTaskDao;
  private PreEnrollmentResponseDao preEnrollmentResponseDao;
  private EnrolleeRelationDao enrolleeRelationDao;

  public WithdrawnEnrolleeDao(Jdbi jdbi, ProfileDao profileDao, SurveyResponseDao surveyResponseDao,
                              ConsentResponseDao consentResponseDao, ParticipantTaskDao participantTaskDao,
                              PreEnrollmentResponseDao preEnrollmentResponseDao, EnrolleeRelationDao enrolleeRelationDao) {
    super(jdbi);
    this.profileDao = profileDao;
    this.surveyResponseDao = surveyResponseDao;
    this.consentResponseDao = consentResponseDao;
    this.participantTaskDao = participantTaskDao;
    this.preEnrollmentResponseDao = preEnrollmentResponseDao;
    this.enrolleeRelationDao = enrolleeRelationDao;
  }

  @Override
  protected Class<WithdrawnEnrollee> getClazz() {
    return WithdrawnEnrollee.class;
  }

  public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
    deleteByProperty("study_environment_id", studyEnvironmentId);
  }

  /** checks whether a withdrawal record exists for the given enrollee shortcode.  */
  public boolean isWithdrawn(String shortcode) {
    return countByProperty("shortcode", shortcode) == 1;
  }

  public int countByStudyEnvironmentId(UUID studyEnvId) {
    return countByProperty("study_environment_id", studyEnvId);
  }

  /**
   * gets everything we need to save for compliance reasons even after a participant withdraws.
   *  For now, we're playing it safe and getting pretty much everything,
   *  later for GDPR compliance, we might trim this down to, e.g., just consent forms
   * */
  public Enrollee loadForWithdrawalPreservation(Enrollee enrollee) {
    enrollee.getSurveyResponses().addAll(surveyResponseDao.findByEnrolleeIdWithAnswers(enrollee.getId()));
    enrollee.getConsentResponses().addAll(consentResponseDao.findByEnrolleeId(enrollee.getId()));
    if (enrollee.getProfileId() != null) {
      enrollee.setProfile(profileDao.loadWithMailingAddress(enrollee.getProfileId()).get());
    }
    enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
    if (enrollee.getPreEnrollmentResponseId() != null) {
      enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).get());
    }
    List<EnrolleeRelation> relationsListByEnrolleeId = enrolleeRelationDao.findAllByEnrolleeId(enrollee.getId());
    List<EnrolleeRelation> relationsListByTargetEnrolleeId = enrolleeRelationDao.findByTargetEnrolleeId(enrollee.getId());
    List<EnrolleeRelationDto> relations = new ArrayList<>();
    relationsListByEnrolleeId.stream().forEach(enrolleeRelation -> relations.add(EnrolleeRelationDto.builder().relation(enrolleeRelation).build()));
    relationsListByTargetEnrolleeId.stream().forEach(enrolleeRelation -> relations.add(EnrolleeRelationDto.builder().relation(enrolleeRelation).build()));
    enrollee.getRelations().addAll(relations);
    return enrollee;
  }
}
