package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.dao.EnvironmentDao;
import bio.terra.pearl.core.dao.ParticipantUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DaoHolder {
    @Autowired
    public ParticipantUserDao participantUserDao;

    @Autowired
    public EnvironmentDao environmentDao;
}
