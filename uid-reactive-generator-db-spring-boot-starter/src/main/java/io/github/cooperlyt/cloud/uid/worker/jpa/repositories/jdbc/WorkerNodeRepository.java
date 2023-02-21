package io.github.cooperlyt.cloud.uid.worker.jpa.repositories.jdbc;

import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerNodeRepository extends CrudRepository<WorkerNodeEntity,Long> {

  Optional<WorkerNodeEntity> getWorkerNodeByHostAndPort(String host, String port);
}
