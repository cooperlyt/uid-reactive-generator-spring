package io.github.cooperlyt.cloud.uid.worker.jpa;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.jpa.repositories.jdbc.WorkerNodeRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;


public class JDBCWorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeRepository workerNodeRepository;

  public JDBCWorkerIdAssigner(WorkerNodeIdent workerNodeIdent,WorkerNodeRepository workerNodeRepository) {
    super(workerNodeIdent);
    this.workerNodeRepository = workerNodeRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected Mono<Long> assignWorkerId(WorkerNodeIdent workerNodeIdent) {
    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> workerNodeRepository
        .getWorkerNodeByHostAndPort(workerNodeIdent.getHost(), workerNodeIdent.getPort())
        .map(WorkerNodeEntity::getId)
        .orElseGet(() -> workerNodeRepository.save(
            WorkerNodeEntity.builder()
                .type(workerNodeIdent.getType())
                .host(workerNodeIdent.getHost())
                .port(workerNodeIdent.getPort())
                .launch(LocalDate.now())
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .build()
        ).getId())));
  }

}
