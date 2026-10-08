package com.nequi.mdw.common.consumer.registry;

import com.nequi.mdw.common.consumer.pojo.ServicePojo;

public interface ResourceLocator {

    ServicePojo getConnectionData(String serviceName, String operation);

    void reload();
}
