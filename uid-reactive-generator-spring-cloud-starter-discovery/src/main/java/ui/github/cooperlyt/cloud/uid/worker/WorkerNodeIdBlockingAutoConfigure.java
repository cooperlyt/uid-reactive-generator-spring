package ui.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnBlockingDiscoveryEnabled
public class WorkerNodeIdBlockingAutoConfigure {


  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(DiscoveryClient discoveryClient){
    return new DiscoveryWorkerIdAssigner(discoveryClient);
  }


}
