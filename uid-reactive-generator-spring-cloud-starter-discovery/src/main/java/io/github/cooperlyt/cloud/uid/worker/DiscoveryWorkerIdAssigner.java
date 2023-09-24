package io.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.worker.client.DiscoveryClientAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;

@Slf4j
public class DiscoveryWorkerIdAssigner implements WorkerIdAssigner {

  private final DiscoveryClientAdapter discoveryClientAdapter;


  private String serverId;

  private String instanceId;

  private Long prepareWorkerId;

  private final Sinks.One<InstanceRegisteredEvent<?>> sink = Sinks.one();


  public DiscoveryWorkerIdAssigner(DiscoveryClientAdapter discoveryClientAdapter) {
    log.debug("create DiscoveryWorkerIdAssigner.");
    this.discoveryClientAdapter = discoveryClientAdapter;
  }

  @Override
  public Mono<Long> assignWorkerId() {
    return sink.asMono().flatMap(event -> getWorkerNodeId());
  }

  @Override
  public void releaseWorkerId(long id, long lastTime) {
    log.info("worker node {} release by {}", id, lastTime);
  }

  @EventListener
  public void onRegisteredEvent(InstanceRegisteredEvent<?> event){
    assert StringUtils.hasText(serverId) : "call this on PreRegisteredEvent after";
    assert StringUtils.hasText(instanceId) : "call this on PreRegisteredEvent after";
    assert prepareWorkerId != null : "call this on PreRegisteredEvent after";

    sink.tryEmitValue(event).orThrow();
  }


  @EventListener
  public void onPreRegisteredEvent(InstancePreRegisteredEvent event) {

    serverId = event.getRegistration().getServiceId();
    instanceId = event.getRegistration().getInstanceId();
    prepareWorkerId = discoveryClientAdapter.getInstances(serverId)
        .filter(instance -> !instance.getInstanceId().equals(instanceId))
        .map(this::getOrder)

        .reduce(0L,(max,order) -> (order > max) ? order : max)
        .blockOptional().orElse(0L) + 1L;

    event.getRegistration().getMetadata().put("order", String.valueOf(prepareWorkerId));
  }

  private Long getOrder(ServiceInstance instance){
    return Long.valueOf(instance.getMetadata().getOrDefault("order","0"));
  }

  private Mono<Long> getWorkerNodeId(){
    return discoveryClientAdapter.getInstances(serverId)
        .reduce(new ArrayList<Long>(),(list, instance) -> {
          if (instance.getInstanceId().equals(instanceId)){
            if (!prepareWorkerId.equals(getOrder(instance))){
              throw new IllegalStateException("discovery worker node id valid fail!");
            }
          }else
            list.add(getOrder(instance));
          return list;
        })
        .flatMap(list -> {
          if (list.contains(prepareWorkerId)){
            log.warn("worker node id duplicate");
            list.sort(Long::compareTo);
            for (long i = 0 ; i <= list.get(list.size() - 1) ; i++ ){
              if (!list.contains(i)){
                log.warn("use a idle id: {}", i);
                return Mono.just(i);
              }
            }
            log.error("discovery worker node id({}) duplicate",prepareWorkerId);
            return Mono.error(new IllegalStateException("discovery worker node id duplicate!"));
          }
          return Mono.just(prepareWorkerId);
        });
  }
}
