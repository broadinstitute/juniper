package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.VersionedEntityService;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.UUID;

public class PublishingUtils {

    protected static <C extends BaseEntity & VersionedEntityConfig, T extends BaseEntity & Versioned>
    C applyChangesToVersionedConfig(VersionedConfigChange<T> versionedConfigChange,
                                    CrudService<C, ?> configService,
                                    VersionedEntityService<T, ?> documentService,
                                    EnvironmentName destEnvName) throws Exception {
        C destConfig = configService.find(versionedConfigChange.destId()).get();
        for (ConfigChange change : versionedConfigChange.configChanges()) {
            setPropertyEnumSafe(destConfig, change.propertyName(), change.newValue());
        }
        if (versionedConfigChange.documentChange().isChanged()) {
            VersionedEntityChange<T> docChange = versionedConfigChange.documentChange();
            UUID newDocumentId = null;
            if (docChange.newStableId() != null) {
                newDocumentId = documentService.findByStableId(docChange.newStableId(), docChange.newVersion(), docChange.portalId()).get().getId();
            }
            assignPublishedVersionIfNeeded(destEnvName, docChange, documentService);
            destConfig.updateVersionedEntityId(newDocumentId);
        }
        return configService.update(destConfig);
    }


    public static <T extends BaseEntity & Versioned, D extends BaseVersionedJdbiDao<T>> void assignPublishedVersionIfNeeded(
            EnvironmentName destEnvName,
            VersionedEntityChange<T> change,
            VersionedEntityService<T, D> service) {
        if (destEnvName.isLive() && change.newStableId() != null) {
            T entity = service.findByStableId(change.newStableId(), change.newVersion(), change.portalId()).orElseThrow();
            service.assignPublishedVersion(entity.getId());
        }
    }

    public static <T extends BaseEntity & Versioned, D extends BaseVersionedJdbiDao<T>> void assignPublishedVersionIfNeeded(
            EnvironmentName destEnvName,
            VersionedEntityConfig newConfig,
            VersionedEntityService<T, D> service) {
        if (destEnvName.isLive() && newConfig.versionedEntityId() != null)  {
            T entity = service.find(newConfig.versionedEntityId()).get();
            service.assignPublishedVersion(entity.getId());
        }
    }

    public static void setPropertyEnumSafe(Object object, String propertyName, Object newValue) throws Exception {
        if(object.getClass().getDeclaredField(propertyName).getType().isEnum()) {
            PropertyUtils.setProperty(object, propertyName, stringToEnum((Class<Enum>) PropertyUtils.getPropertyType(object, propertyName), newValue.toString()));
        } else {
            PropertyUtils.setProperty(object, propertyName, newValue);
        }
    }

    public static <T extends Enum<T>> T stringToEnum(Class<T> enumType, String value) {
        return Enum.valueOf(enumType, value);
    }
}
