package io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc;


import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc.mapper.JDBCWorkerNodeMapper;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;


public class WorkerIdAssigner extends HostWorkerIdAssigner {

  private final JDBCWorkerNodeMapper jdbcWorkerNodeMapper;

  public WorkerIdAssigner(JDBCWorkerNodeMapper jdbcWorkerNodeMapper) {
    this.jdbcWorkerNodeMapper = jdbcWorkerNodeMapper;
  }



  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeType type, String host, String port) {
    return Mono.just(jdbcWorkerNodeMapper
        .getWorkerNodeByHostPort(host, port)
        .map(WorkerNodeEntity::getId)
        .orElseGet(() -> {
          WorkerNodeEntity workerNode = WorkerNodeEntity.builder()
              .type(type.value())
              .hostName(host)
              .port(port)
              .launchDate(new Date())
              .build();
          jdbcWorkerNodeMapper.addWorkerNode(workerNode);
          return workerNode.getId();
        }));
  }

}
