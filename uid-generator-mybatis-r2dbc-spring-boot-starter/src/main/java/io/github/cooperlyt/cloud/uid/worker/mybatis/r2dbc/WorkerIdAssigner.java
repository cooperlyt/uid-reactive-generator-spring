package io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc;


import io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc.mapper.R2dbcWorkerNodeMapper;
import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;

@Slf4j
public class WorkerIdAssigner extends HostWorkerIdAssigner {

  private final R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper;

  public WorkerIdAssigner(R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper) {
    this.r2dbcWorkerNodeMapper = r2dbcWorkerNodeMapper;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeType type, String host, String port) {
    return r2dbcWorkerNodeMapper.getWorkerNodeByHostPort(host,port)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty())
        .flatMap(workerNode -> workerNode.map(Mono::just).orElse(createWorkerNode(type,host,port)))
        .map(WorkerNodeEntity::getId);

  }

  private Mono<WorkerNodeEntity> createWorkerNode(WorkerNodeType type, String host, String port){
    log.info("create new Worker node.");
    WorkerNodeEntity workerNode = WorkerNodeEntity.builder()
        .launchDate(new Date())
        .type(type.value())
        .hostName(host)
        .port(port)
        .build();
    return r2dbcWorkerNodeMapper
        .addWorkerNode(workerNode)
        .map(count -> workerNode);
  }


}
