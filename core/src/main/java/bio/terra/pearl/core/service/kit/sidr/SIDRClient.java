package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;

/**
 * Client for the SIDR (GPO) ordering API. Like the PepperDSMClient, there is a live implementation, a stub
 * implementation, and a wrapper that picks between them based on the StudyEnvironmentConfig.
 */
public interface SIDRClient {

    /**
     * Places an order in SIDR.
     *
     * @param config      the study environment config, used to determine which SIDR environment to talk to
     * @param actor       the admin user on whose behalf the order is placed. SIDR requires that orders
     *                    submitted by a service account name the (SIDR-registered) person placing the order.
     * @param order       the order to place
     * @return the created order, as returned by SIDR (including the SIDR order id and sample ids)
     * @throws SIDRApiException if SIDR rejects the order, or the response can't be processed
     */
    SIDROrder postOrder(StudyEnvironmentConfig config, AdminUser actor, SIDROrder order) throws SIDRApiException;

    /**
     * Fetches an existing order from SIDR by its SIDR order id (e.g. SDOR-...)
     *
     * @throws SIDRApiException if SIDR returns an error, or the response can't be processed
     */
    SIDROrder getOrder(StudyEnvironmentConfig config, String sidrOrderId) throws SIDRApiException;
}
