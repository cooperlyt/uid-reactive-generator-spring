package io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.entities.WorkerNodeEntity;
import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.repositories.WorkerNodeRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;


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
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty())
        .flatMap(entity -> entity.map(Mono::just).orElse(
            workerNodeRepository.save(
                WorkerNodeEntity
                    .builder()
                    .launchDate(LocalDateTime.now())
                    .created(LocalDateTime.now())
                    .modified(LocalDateTime.now())
                    .type(type.value())
                    .hostName(host)
                    .port(port)
                    .build()
            ))
        )
        .map(WorkerNodeEntity::getId);
  }
}
