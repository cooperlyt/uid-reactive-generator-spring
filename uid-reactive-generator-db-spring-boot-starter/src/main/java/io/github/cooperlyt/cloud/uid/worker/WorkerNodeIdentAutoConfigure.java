package io.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.utils.DockerUtils;
import io.github.cooperlyt.cloud.uid.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configurable
public class WorkerNodeIdentAutoConfigure {

  private String fakePort() {
    return System.currentTimeMillis() + "-" + RandomUtils.nextInt(100000);
  }

  @Bean
  @Lazy
  @ConditionalOnNotWebApplication
  @ConditionalOnMissingBean
  @AutoConfigureOrder(2)
  WorkerNodeIdent defaultWorkerNodeIdentProvider(){
    log.info("get host ident by default");
    return WorkerNodeIdent.builder()
        .type(DockerUtils.isDocker() ? WorkerNodeType.CONTAINER.value() : WorkerNodeType.ACTUAL.value())
        .host(DockerUtils.isDocker() ? DockerUtils.getDockerHost() : NetUtils.getLocalAddress())
        .port(DockerUtils.isDocker() ? DockerUtils.getDockerPort() : fakePort())
        .build();

  }

  @Bean
  @Lazy
  @ConditionalOnMissingBean
  @ConditionalOnWebApplication
  @ConditionalOnProperty("server.port")
  @AutoConfigureOrder(1)
  @Autowired
  WorkerNodeIdent webWorkerNodeIdentProvider(@Value("${server.port}") String port){
    log.info("get host ident by web");
    return WorkerNodeIdent.builder()
        .type(WorkerNodeType.SPRING_WEB.value())
        .host(DockerUtils.isDocker() ? DockerUtils.getDockerHost() : NetUtils.getLocalAddress())
        .port(port)
        .build();
  }
}
