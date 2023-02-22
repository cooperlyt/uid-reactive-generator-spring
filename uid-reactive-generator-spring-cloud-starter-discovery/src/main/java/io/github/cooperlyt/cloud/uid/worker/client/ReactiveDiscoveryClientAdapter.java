package io.github.cooperlyt.cloud.uid.worker.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;

public class ReactiveDiscoveryClientAdapter implements DiscoveryClientAdapter {

  private final ReactiveDiscoveryClient discoveryClient;

  public ReactiveDiscoveryClientAdapter(ReactiveDiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serverId){
    return discoveryClient.getInstances(serverId);
  }

}
