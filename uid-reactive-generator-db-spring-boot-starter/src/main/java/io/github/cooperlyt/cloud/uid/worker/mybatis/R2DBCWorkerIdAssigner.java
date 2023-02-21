package io.github.cooperlyt.cloud.uid.worker.mybatis;


import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.r2dbc.WorkerNodeMapper;
import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class R2DBCWorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeMapper r2dbcWorkerNodeMapper;

  public R2DBCWorkerIdAssigner(WorkerNodeIdent workerNodeIdent,WorkerNodeMapper r2dbcWorkerNodeMapper) {
    super(workerNodeIdent);
    this.r2dbcWorkerNodeMapper = r2dbcWorkerNodeMapper;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeIdent workerNodeIdent) {
    return r2dbcWorkerNodeMapper.getWorkerNodeByHostPort(workerNodeIdent.getHost(),workerNodeIdent.getPort())
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty())
        .flatMap(workerNode -> workerNode.map(Mono::just).orElse(createWorkerNode(workerNodeIdent)))
        .map(WorkerNodeEntity::getId);
  }

  private Mono<WorkerNodeEntity> createWorkerNode(WorkerNodeIdent workerNodeIdent){
    log.info("create new Worker node.");
    WorkerNodeEntity workerNode = WorkerNodeEntity.builder()
        .launch(LocalDate.now())
        .type(workerNodeIdent.getType())
        .host(workerNodeIdent.getHost())
        .port(workerNodeIdent.getPort())
        .build();
    return r2dbcWorkerNodeMapper
        .addWorkerNode(workerNode)
        .map(count -> workerNode);
  }



}
