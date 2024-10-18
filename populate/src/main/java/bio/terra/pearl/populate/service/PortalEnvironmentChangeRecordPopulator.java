package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.publishing.PortalEnvironmentChangeRecordService;
import bio.terra.pearl.populate.dto.PortalEnvironmentChangeRecordPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class PortalEnvironmentChangeRecordPopulator extends BasePopulator<PortalEnvironmentChangeRecord, PortalEnvironmentChangeRecordPopDto, PortalPopulateContext> {
    private final PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService;
    private final AdminUserService adminUserService;
    private final TimeShiftDao timeShiftDao;
    private final PortalService portalService;

    public PortalEnvironmentChangeRecordPopulator(PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService,
                                                  AdminUserService adminUserService, TimeShiftDao timeShiftDao,
                                                  PortalService portalService) {
        this.portalEnvironmentChangeRecordService = portalEnvironmentChangeRecordService;
        this.adminUserService = adminUserService;
        this.timeShiftDao = timeShiftDao;
        this.portalService = portalService;
    }

    @Override
    protected void preProcessDto(PortalEnvironmentChangeRecordPopDto popDto, PortalPopulateContext context) {
       AdminUser user = adminUserService.findByUsername(popDto.getAdminUsername()).orElseThrow();
       popDto.setAdminUserId(user.getId());
       popDto.setPortalId(portalService.findOneByShortcode(context.getPortalShortcode()).orElseThrow().getId());
       try {
           String recordString = objectMapper.writeValueAsString(popDto.getPortalEnvironmentChangeJson());
           popDto.setPortalEnvironmentChange(recordString);
       } catch (Exception e) {
           throw new RuntimeException("Error converting PortalEnvironmentChangeJson to string", e);
       }
    }

    @Override
    protected Class<PortalEnvironmentChangeRecordPopDto> getDtoClazz() {
        return PortalEnvironmentChangeRecordPopDto.class;
    }

    @Override
    public Optional<PortalEnvironmentChangeRecord> findFromDto(PortalEnvironmentChangeRecordPopDto popDto, PortalPopulateContext context) {
        // we don't support looking up existing records
        return Optional.empty();
    }

    @Override
    public PortalEnvironmentChangeRecord overwriteExisting(PortalEnvironmentChangeRecord existingObj, PortalEnvironmentChangeRecordPopDto popDto, PortalPopulateContext context) throws IOException {
        portalEnvironmentChangeRecordService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public PortalEnvironmentChangeRecord createPreserveExisting(PortalEnvironmentChangeRecord existingObj, PortalEnvironmentChangeRecordPopDto popDto, PortalPopulateContext context) throws IOException {
        return createNew(popDto, context, true);
    }

    @Override
    public PortalEnvironmentChangeRecord createNew(PortalEnvironmentChangeRecordPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        PortalEnvironmentChangeRecord changeRecord = portalEnvironmentChangeRecordService.create(popDto);
        if (popDto.isTimeShifted()) {
            timeShiftDao.changeEnrolleeCreationTime(changeRecord.getId(), popDto.shiftedInstant());
        }
        return changeRecord;
    }
}
