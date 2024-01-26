package bio.terra.pearl.core.dao.participant;

import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeRelationDao extends BaseMutableJdbiDao<EnrolleeRelation> {

    public EnrolleeRelationDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<EnrolleeRelation> getClazz() {
        return EnrolleeRelation.class;
    }

    public List<EnrolleeRelation> findByParticipantUserId(UUID participantUserId){
        return findAllByProperty("participant_user_id", participantUserId);
    }
}
