package org.bintec.mdw.common.consumer.registry;

import org.bintec.mdw.common.consumer.pojo.ServicePojo;

public interface ResourceLocator {

    ServicePojo getConnectionData(String serviceName, String operation);

    void reload();
}
