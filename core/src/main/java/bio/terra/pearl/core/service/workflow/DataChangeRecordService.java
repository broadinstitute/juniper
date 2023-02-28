package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.DataChangeRecordDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import java.util.List;

public class DataChangeRecordService extends CrudService<DataChangeRecord, DataChangeRecordDao> {
    private EnrolleeService enrolleeService;


    public DataChangeRecordService(DataChangeRecordDao dao, EnrolleeService enrolleeService) {
        super(dao);
        this.enrolleeService = enrolleeService;
    }

    public List<DataChangeRecord> findByEnrolleeId(String enrolleeShortcode) {
        Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
        return dao.findByEnrolleeId(enrollee.getId());
    }
}
