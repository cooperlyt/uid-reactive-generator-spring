package io.github.cooperlyt.cloud.uid.worker.jpa;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.jpa.repositories.r2dbc.WorkerNodeRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


public class R2DBCWorkerIdAssigner extends HostWorkerIdAssigner {

  private final WorkerNodeRepository workerNodeRepository;

  public R2DBCWorkerIdAssigner(WorkerNodeIdent workerNodeIdent,
                               WorkerNodeRepository workerNodeRepository) {
    super(workerNodeIdent);
    this.workerNodeRepository = workerNodeRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public Mono<Long> assignWorkerId(WorkerNodeIdent workerNodeIdent) {
    return workerNodeRepository
        .getWorkerNodeByHostAndPort(workerNodeIdent.getHost(),workerNodeIdent.getPort())
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty())
        .flatMap(entity -> entity.map(Mono::just).orElse(
            workerNodeRepository.save(
                WorkerNodeEntity
                    .builder()
                    .launch(LocalDate.now())
                    .created(LocalDateTime.now())
                    .modified(LocalDateTime.now())
                    .type(workerNodeIdent.getType())
                    .host(workerNodeIdent.getHost())
                    .port(workerNodeIdent.getPort())
                    .build()
            ))
        )
        .map(WorkerNodeEntity::getId);
  }
}
