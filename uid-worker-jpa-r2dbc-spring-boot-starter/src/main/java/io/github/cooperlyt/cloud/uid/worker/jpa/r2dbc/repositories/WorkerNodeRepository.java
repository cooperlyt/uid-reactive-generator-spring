package io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.repositories;

import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.entities.WorkerNodeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WorkerNodeRepository extends ReactiveCrudRepository<WorkerNodeEntity,Long> {

  Mono<WorkerNodeEntity> getWorkerNodeByHostNameAndPort(String hostName, String port);

}
