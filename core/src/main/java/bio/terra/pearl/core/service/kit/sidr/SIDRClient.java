package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;

public interface SIDRClient {

    SIDROrder postOrder(SIDROrder sidrOrder);

    SIDROrder getOrder(String sidrOrderId);
}
