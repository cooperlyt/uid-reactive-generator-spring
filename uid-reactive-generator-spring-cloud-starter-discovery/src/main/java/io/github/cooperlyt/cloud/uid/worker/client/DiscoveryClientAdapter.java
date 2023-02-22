package io.github.cooperlyt.cloud.uid.worker.client;

import org.springframework.cloud.client.ServiceInstance;
import reactor.core.publisher.Flux;

public interface DiscoveryClientAdapter {

  Flux<ServiceInstance> getInstances(String serverId);

}
