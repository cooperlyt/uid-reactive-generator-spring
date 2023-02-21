package ui.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@ConditionalOnReactiveDiscoveryEnabled
public class WorkerNodeIdReactiveAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(ReactiveDiscoveryClient discoveryClient){
    return null;
  }
}
