package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.workflow.Event;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EventDao extends BaseJdbiDao<Event> {

    public EventDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Event> getClazz() {
        return Event.class;
    }

    public List<Event> findAllByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }
}
