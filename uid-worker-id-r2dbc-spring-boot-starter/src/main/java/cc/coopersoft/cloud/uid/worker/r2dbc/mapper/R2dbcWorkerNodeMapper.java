package cc.coopersoft.cloud.uid.worker.r2dbc.mapper;


import cc.coopersoft.cloud.uid.worker.entity.WorkerNodeEntity;
import org.apache.ibatis.annotations.*;
import reactor.core.publisher.Mono;


@Mapper
public interface R2dbcWorkerNodeMapper {

  /**
   * Get {@link WorkerNodeEntity} by node host
   *
   * @param host
   * @param port
   * @return
   */
  @Select("SELECT " +
      " ID," +
      " HOST_NAME," +
      " PORT," +
      " TYPE," +
      " LAUNCH_DATE," +
      " MODIFIED," +
      " CREATED" +
      " FROM" +
      " WORKER_NODE" +
      " WHERE" +
      " HOST_NAME = #{host,jdbcType=VARCHAR} AND PORT = #{port,jdbcType=VARCHAR} limit 1")
  Mono<WorkerNodeEntity> getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);

  /**
   * Add {@link WorkerNodeEntity}
   *
   * @param workerNodeEntity
   */
  @Insert("INSERT INTO WORKER_NODE" +
      "(HOST_NAME," +
      "PORT," +
      "TYPE," +
      "LAUNCH_DATE," +
      "MODIFIED," +
      "CREATED)" +
      "VALUES (" +
      "#{hostName}," +
      "#{port}," +
      "#{type}," +
      "#{launchDate}," +
      "NOW()," +
      "NOW())")
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  Mono<Long> addWorkerNode(WorkerNodeEntity workerNodeEntity);
}
