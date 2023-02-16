package io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.repositories;

import io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.entities.WorkerNodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerNodeRepository extends CrudRepository<WorkerNodeEntity,Long> {

  Optional<WorkerNodeEntity> getWorkerNodeByHostNameAndPort(String hostName, String port);
}
