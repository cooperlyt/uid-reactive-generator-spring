package io.github.cooperlyt.cloud.uid.worker.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import reactor.core.publisher.Flux;

@Slf4j
public class BlockingDiscoveryClientAdapter implements DiscoveryClientAdapter {

  private final DiscoveryClient discoveryClient;

  public BlockingDiscoveryClientAdapter(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
    log.info("init by DiscoveryClient");
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serverId){
    return Flux.fromIterable(discoveryClient.getInstances(serverId));
  }

}
