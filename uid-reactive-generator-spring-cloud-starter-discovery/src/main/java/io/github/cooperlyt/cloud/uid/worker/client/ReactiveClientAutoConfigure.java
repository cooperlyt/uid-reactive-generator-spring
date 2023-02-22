package io.github.cooperlyt.cloud.uid.worker.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@ConditionalOnReactiveDiscoveryEnabled
@Configuration
public class ReactiveClientAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  DiscoveryClientAdapter orderGenerator(ReactiveDiscoveryClient discoveryClient){
    return new ReactiveDiscoveryClientAdapter(discoveryClient);
  }
}
