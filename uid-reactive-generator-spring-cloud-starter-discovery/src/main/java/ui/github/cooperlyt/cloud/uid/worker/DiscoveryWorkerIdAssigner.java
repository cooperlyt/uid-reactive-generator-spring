package ui.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Mono;

@Slf4j
public class DiscoveryWorkerIdAssigner implements WorkerIdAssigner, ApplicationListener<InstancePreRegisteredEvent> {

  private final DiscoveryClient discoveryClient;

  public DiscoveryWorkerIdAssigner(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public Mono<Long> assignWorkerId() {
    return null;
  }

  @Override
  public void releaseWorkerId(long id, long lastTime) {
    log.info("worker node {} release by {}", id, lastTime);
  }

  @Override
  public void onApplicationEvent(InstancePreRegisteredEvent event) {
    long newOrder = discoveryClient.getInstances("corp")
        .stream().filter(instance -> !instance.getInstanceId().equals(event.getRegistration().getInstanceId()))
        .map(instance -> Long.valueOf(instance.getMetadata().getOrDefault("order","0")))
        .max(Long::compare).orElse(0L) + 1;

    event.getRegistration().getMetadata().put("order",String.valueOf(newOrder));
  }
}
