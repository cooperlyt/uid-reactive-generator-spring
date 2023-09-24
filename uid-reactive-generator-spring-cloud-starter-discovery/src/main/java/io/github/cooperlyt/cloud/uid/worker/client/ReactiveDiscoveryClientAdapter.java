package io.github.cooperlyt.cloud.uid.worker.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveDiscoveryClientAdapter implements DiscoveryClientAdapter {

  private final ReactiveDiscoveryClient discoveryClient;

  public ReactiveDiscoveryClientAdapter(ReactiveDiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
    log.info("init by ReactiveDiscoveryClient");
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serverId){
    return discoveryClient.getInstances(serverId);
  }

}
