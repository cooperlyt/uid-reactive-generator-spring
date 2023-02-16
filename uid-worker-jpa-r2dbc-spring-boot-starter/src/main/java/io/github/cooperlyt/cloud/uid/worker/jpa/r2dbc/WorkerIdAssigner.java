package io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.repositories.WorkerNodeRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;


public class WorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeRepository workerNodeRepository;

  public WorkerIdAssigner(WorkerNodeRepository workerNodeRepository) {
    this.workerNodeRepository = workerNodeRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeType type, String host, String port) {
    return workerNodeRepository
        .getWorkerNodeByHostNameAndPort(host,port)
        .switchIfEmpty(workerNodeRepository.save(
            WorkerNodeEntity
            .builder()
                .launchDate(new Date())
                .type(type.value())
                .hostName(host)
                .port(port)
                .build()
        ))
        .map(WorkerNodeEntity::getId);
  }
}
