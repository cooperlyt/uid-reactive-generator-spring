package io.github.cooperlyt.cloud.uid.worker.jpa.jdbc;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.repositories.WorkerNodeRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.CompletableFuture;


public class WorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeRepository workerNodeRepository;

  public WorkerIdAssigner(WorkerNodeRepository workerNodeRepository) {
    this.workerNodeRepository = workerNodeRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeType type, String host, String port) {
    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> workerNodeRepository
        .getWorkerNodeByHostNameAndPort(host, port)
        .map(WorkerNodeEntity::getId)
        .orElseGet(() -> workerNodeRepository.save(
            WorkerNodeEntity.builder()
                .type(type.value())
                .hostName(host)
                .port(port)
                .launchDate(new Date())
                .created(new Date())
                .modified(new Date())
                .build()
        ).getId())));
  }

}
