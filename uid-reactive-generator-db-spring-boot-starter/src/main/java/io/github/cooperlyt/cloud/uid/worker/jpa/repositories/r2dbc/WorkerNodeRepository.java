package io.github.cooperlyt.cloud.uid.worker.jpa.repositories.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WorkerNodeRepository extends ReactiveCrudRepository<WorkerNodeEntity,Long> {

  Mono<WorkerNodeEntity> getWorkerNodeByHostAndPort(String host, String port);

}
