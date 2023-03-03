package io.github.cooperlyt.cloud.uid;

import io.github.cooperlyt.cloud.uid.buffer.RejectedPutBufferHandler;
import io.github.cooperlyt.cloud.uid.buffer.TimeIsFutureHandler;
import io.github.cooperlyt.cloud.uid.impl.CachedUidGenerator;
import io.github.cooperlyt.cloud.uid.impl.DefaultUidGenerator;
import io.github.cooperlyt.cloud.uid.impl.UidCachedConfigProperties;
import io.github.cooperlyt.cloud.uid.impl.UidConfigProperties;
import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * UID 的自动配置
 *
 * @author wujun
 * @date 2019.02.20 10:57
 */
@Slf4j
@Configuration(enforceUniqueMethods = false)
public class UidAutoConfigure {


  private final WorkerIdAssigner workerIdAssigner;

  //private final RejectedBufferConfigureImpl rejectedBufferConfigure;

  public UidAutoConfigure(WorkerIdAssigner workerIdAssigner) {
    this.workerIdAssigner = workerIdAssigner;
    //this.rejectedBufferConfigure = rejectedBufferConfigure;
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  @ConfigurationProperties(prefix = "uid.cached-uid-generator")
  UidCachedConfigProperties uidCachedProperties() {
    return new UidCachedConfigProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  @ConfigurationProperties(prefix = "uid")
  UidProperties uidProperties() {
    return new UidConfigProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  DefaultUidGenerator defaultUidGenerator(UidProperties uidProperties) {
    return new DefaultUidGenerator(uidProperties, workerIdAssigner);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  CachedUidGenerator cachedUidGenerator(
      UidProperties uidProperties,
      UidCachedProperties uidCachedProperties) {
    return new CachedUidGenerator(uidProperties, uidCachedProperties, workerIdAssigner);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  CachedUidGenerator cachedUidGenerator(
      UidProperties uidProperties,
      UidCachedProperties uidCachedProperties,
      TimeIsFutureHandler timeIsFutureHandler,
      RejectedPutBufferHandler rejectedPutBufferHandler) {
    return new CachedUidGenerator(uidProperties,
        uidCachedProperties,
        workerIdAssigner,
        rejectedPutBufferHandler,
        timeIsFutureHandler);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  CachedUidGenerator cachedUidGenerator(
      UidProperties uidProperties,
      UidCachedProperties uidCachedProperties,
      RejectedPutBufferHandler rejectedPutBufferHandler) {
    return new CachedUidGenerator(
        uidProperties,
        uidCachedProperties,
        workerIdAssigner,
        rejectedPutBufferHandler);
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  CachedUidGenerator cachedUidGenerator(
      UidProperties uidProperties,
      UidCachedProperties uidCachedProperties,
      TimeIsFutureHandler timeIsFutureHandler) {
    return new CachedUidGenerator(
        uidProperties,
        uidCachedProperties,
        workerIdAssigner,
        timeIsFutureHandler);
  }

  /**
   * example custom rejected handler
   *
   * @return RejectedPutBufferHandler
   */
//  @Bean
//  RejectedPutBufferHandler customPutHandler() {
//    return (r, i) -> {
//        do your
//    };
//  }
}
