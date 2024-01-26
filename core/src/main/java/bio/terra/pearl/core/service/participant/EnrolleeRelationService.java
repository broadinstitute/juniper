package bio.terra.pearl.core.service.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationService extends CrudService<EnrolleeRelation, EnrolleeRelationDao> {
    EnrolleeService enrolleeService;

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao, EnrolleeService enrolleeService) {
        super(enrolleeRelationDao);
        this.enrolleeService = enrolleeService;
    }

    public List<EnrolleeRelation> findEnrolleeRelationsByParticipantUserId(UUID participantUserId) {
        return dao.findByParticipantUserId(participantUserId);
    }

    public List<Enrollee> findGovernedEnrollees(UUID participantUserId) {
        List<EnrolleeRelation> enrolleeRelationsList =
                findEnrolleeRelationsByParticipantUserId(participantUserId).stream().filter(EnrolleeRelation::isProxy)
                        .collect(Collectors.toList());
        List<Enrollee> governedEnrollees = new ArrayList<>();
        enrolleeRelationsList.forEach(enrolleeRelation ->
            enrolleeService.findOneByEnrolleeId(enrolleeRelation.getEnrolleeId()).ifPresent(governedEnrollees::add));
        return governedEnrollees;

    }
}
