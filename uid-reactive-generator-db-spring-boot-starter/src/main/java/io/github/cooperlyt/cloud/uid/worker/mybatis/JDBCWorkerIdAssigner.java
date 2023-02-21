package io.github.cooperlyt.cloud.uid.worker.mybatis;


import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.jdbc.WorkerNodeMapper;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class JDBCWorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeMapper workerNodeMapper;

  public JDBCWorkerIdAssigner(WorkerNodeIdent workerNodeIdent,WorkerNodeMapper workerNodeMapper) {
    super(workerNodeIdent);
    this.workerNodeMapper = workerNodeMapper;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeIdent workerNodeIdent) {
    return Mono.just(workerNodeMapper
        .getWorkerNodeByHostPort(workerNodeIdent.getHost(), workerNodeIdent.getPort())
        .map(WorkerNodeEntity::getId)
        .orElseGet(() -> {
          WorkerNodeEntity workerNode = WorkerNodeEntity.builder()
              .type(workerNodeIdent.getType())
              .host(workerNodeIdent.getHost())
              .port(workerNodeIdent.getPort())
              .launch(LocalDate.now())
              .build();
          workerNodeMapper.addWorkerNode(workerNode);
          return workerNode.getId();
        }));
  }
}
