package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.WithdrawnEnrolleeDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawnEnrolleeService extends ImmutableEntityService<WithdrawnEnrollee, WithdrawnEnrolleeDao> {
  private EnrolleeService enrolleeService;
  private ObjectMapper objectMapper;
  private PortalParticipantUserService portalParticipantUserService;
  private ParticipantUserService participantUserService;

  public WithdrawnEnrolleeService(WithdrawnEnrolleeDao dao, EnrolleeService enrolleeService, ObjectMapper objectMapper,
                                  PortalParticipantUserService portalParticipantUserService, ParticipantUserService participantUserService) {
    super(dao);
    this.enrolleeService = enrolleeService;
    this.objectMapper = objectMapper;
    this.portalParticipantUserService = portalParticipantUserService;
    this.participantUserService = participantUserService;
  }

  public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
    dao.deleteByStudyEnvironmentId(studyEnvironmentId);
  }

  public int countByStudyEnvironmentId(UUID studyEnvironmentId) {
    return dao.countByStudyEnvironmentId(studyEnvironmentId);
  }

  public boolean isWithdrawn(String shortcode) {
    return dao.isWithdrawn(shortcode);
  }

  /**
   * creates a WithdrawnEnrollee for the passed-in enrollee, and DELETES THE ENROLLEE.
   * Although the WithdrawnEnrollee record may contain much of the enrollee's data, this should be assumed to be
   * an irreversible operation.
   */
  @Transactional
  public WithdrawnEnrollee withdrawEnrollee(Enrollee enrollee) {
    dao.loadForWithdrawalPreservation(enrollee);
    ParticipantUser user = participantUserService.find(enrollee.getParticipantUserId()).get();
    try {
      WithdrawnEnrollee withdrawnEnrollee = WithdrawnEnrollee.builder()
              .shortcode(enrollee.getShortcode())
              .studyEnvironmentId(enrollee.getStudyEnvironmentId())
              .enrolleeData(objectMapper.writeValueAsString(enrollee))
              .userData(objectMapper.writeValueAsString(user))
              .build();
      withdrawnEnrollee = create(withdrawnEnrollee);
      enrolleeService.delete(enrollee.getId(), CascadeProperty.EMPTY_SET);

      return withdrawnEnrollee;
    } catch (JsonProcessingException e) {
      throw new InternalServerException("Error serializing enrollee or user data", e);
    }
  }

  @Transactional
  public List<WithdrawnEnrollee> withdrawFromPortal(PortalParticipantUser ppUser) throws JsonProcessingException {
    List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(ppUser);
    List<WithdrawnEnrollee> withdrawns = new ArrayList<>();
    for (Enrollee enrollee : enrollees) {
      withdrawns.add(withdrawEnrollee(enrollee));
    }
    portalParticipantUserService.delete(ppUser.getId(), CascadeProperty.EMPTY_SET);
    return withdrawns;
  }

  @Transactional
  public List<WithdrawnEnrollee> withdrawFromJuniper(ParticipantUser participantUser) throws JsonProcessingException {
    List<PortalParticipantUser> ppUsers =portalParticipantUserService.findByParticipantUserId(participantUser.getId());
    List<WithdrawnEnrollee> withdrawns = new ArrayList<>();
    for (PortalParticipantUser ppUser : ppUsers) {
      withdrawns.addAll(withdrawFromPortal(ppUser));
    }
    participantUserService.delete(participantUser.getId(), CascadeProperty.EMPTY_SET);
    return withdrawns;
  }
}
