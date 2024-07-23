package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Component
public class WithdrawnEnrolleeDao extends BaseJdbiDao<WithdrawnEnrollee> {
  private final ProfileDao profileDao;
  private final SurveyResponseDao surveyResponseDao;
  private final ParticipantTaskDao participantTaskDao;
  private final PreEnrollmentResponseDao preEnrollmentResponseDao;
  private final EnrolleeRelationDao enrolleeRelationDao;
  private final FamilyEnrolleeDao familyEnrolleeDao;

  public WithdrawnEnrolleeDao(Jdbi jdbi, ProfileDao profileDao, SurveyResponseDao surveyResponseDao, ParticipantTaskDao participantTaskDao,
                              PreEnrollmentResponseDao preEnrollmentResponseDao, EnrolleeRelationDao enrolleeRelationDao, FamilyEnrolleeDao familyEnrolleeDao) {
    super(jdbi);
    this.profileDao = profileDao;
    this.surveyResponseDao = surveyResponseDao;
    this.participantTaskDao = participantTaskDao;
    this.preEnrollmentResponseDao = preEnrollmentResponseDao;
    this.enrolleeRelationDao = enrolleeRelationDao;
    this.familyEnrolleeDao = familyEnrolleeDao;
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
    if (enrollee.getProfileId() != null) {
      enrollee.setProfile(profileDao.loadWithMailingAddress(enrollee.getProfileId()).get());
    }
    enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
    if (enrollee.getPreEnrollmentResponseId() != null) {
      enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).get());
    }
    enrollee.setFamilyEnrollees(familyEnrolleeDao.findByFamilyId(enrollee.getId()));
    List<EnrolleeRelation> relationsByEnrollee = enrolleeRelationDao.findAllByEnrolleeId(enrollee.getId());
    List<EnrolleeRelation> relationsByTargetEnrollee = enrolleeRelationDao.findByTargetEnrolleeId(enrollee.getId());
    enrollee.getRelations().addAll(relationsByEnrollee);
    enrollee.getRelations().addAll(relationsByTargetEnrollee);
    return enrollee;
  }
}
