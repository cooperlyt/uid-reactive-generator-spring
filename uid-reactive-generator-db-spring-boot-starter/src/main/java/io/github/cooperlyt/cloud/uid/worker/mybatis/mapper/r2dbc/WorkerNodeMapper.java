package io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.entities.WorkerNodeEntity;
import org.apache.ibatis.annotations.*;
import reactor.core.publisher.Mono;

public interface WorkerNodeMapper {

  /**
   * Get {@link WorkerNodeEntity} by node host
   *
   * @param host
   * @param port
   * @return
   */
  @Select("SELECT " +
      " ID," +
      " HOST," +
      " PORT," +
      " TYPE," +
      " LAUNCH," +
      " MODIFIED," +
      " CREATED" +
      " FROM" +
      " WORKER_NODE" +
      " WHERE" +
      " HOST = #{host,jdbcType=VARCHAR} AND PORT = #{port,jdbcType=VARCHAR} limit 1")
  Mono<WorkerNodeEntity> getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);

  /**
   * Add {@link WorkerNodeEntity}
   *
   * @param workerNodeEntity
   */
  @Insert("INSERT INTO WORKER_NODE" +
      "(HOST," +
      "PORT," +
      "TYPE," +
      "LAUNCH," +
      "MODIFIED," +
      "CREATED)" +
      "VALUES (" +
      "#{host}," +
      "#{port}," +
      "#{type}," +
      "#{launch}," +
      "NOW()," +
      "NOW())")
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  Mono<Long> addWorkerNode(WorkerNodeEntity workerNodeEntity);
}
