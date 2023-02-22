package io.github.cooperlyt.cloud.uid.worker.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import reactor.core.publisher.Flux;

public class BlockingDiscoveryClientAdapter implements DiscoveryClientAdapter {

  private final DiscoveryClient discoveryClient;

  public BlockingDiscoveryClientAdapter(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serverId){
    return Flux.fromIterable(discoveryClient.getInstances(serverId));
  }

}
