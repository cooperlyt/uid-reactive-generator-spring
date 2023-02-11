package cc.coopersoft.cloud.uid.worker.jdbc;

import cc.coopersoft.cloud.uid.worker.DisposableWorkerIdAssigner;
import cc.coopersoft.cloud.uid.worker.entity.WorkerNodeEntity;
import cc.coopersoft.cloud.uid.worker.jdbc.mapper.JdbcWorkerNodeMapper;
import org.springframework.transaction.annotation.Transactional;

public class JDBCWorkerIdAssigner extends DisposableWorkerIdAssigner {

  private final JdbcWorkerNodeMapper jdbcWorkerNodeMapper;

  public JDBCWorkerIdAssigner(JdbcWorkerNodeMapper jdbcWorkerNodeMapper) {
    this.jdbcWorkerNodeMapper = jdbcWorkerNodeMapper;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected long assignWorkerId(WorkerNodeEntity workerNodeEntity) {
    return jdbcWorkerNodeMapper
        .getWorkerNodeByHostPort(workerNodeEntity.getHostName(), workerNodeEntity.getPort())
        .map(WorkerNodeEntity::getId)
        .orElseGet(() -> {
          jdbcWorkerNodeMapper.addWorkerNode(workerNodeEntity);
          return workerNodeEntity.getId();
        });
  }
}
