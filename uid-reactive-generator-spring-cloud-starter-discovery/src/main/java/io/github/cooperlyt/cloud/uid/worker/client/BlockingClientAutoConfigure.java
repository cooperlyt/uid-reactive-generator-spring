package io.github.cooperlyt.cloud.uid.worker.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnBlockingDiscoveryEnabled
public class BlockingClientAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  DiscoveryClientAdapter orderGenerator(DiscoveryClient discoveryClient){
    return new BlockingDiscoveryClientAdapter(discoveryClient);
  }

}
