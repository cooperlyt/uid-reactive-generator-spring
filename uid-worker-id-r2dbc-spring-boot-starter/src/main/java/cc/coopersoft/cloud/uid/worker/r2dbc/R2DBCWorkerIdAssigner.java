package cc.coopersoft.cloud.uid.worker.r2dbc;


import cc.coopersoft.cloud.uid.worker.DisposableWorkerIdAssigner;
import cc.coopersoft.cloud.uid.worker.entity.WorkerNodeEntity;
import cc.coopersoft.cloud.uid.worker.r2dbc.mapper.R2dbcWorkerNodeMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnClass(R2dbcEntityOperations.class)
@Component
public class R2DBCWorkerIdAssigner extends DisposableWorkerIdAssigner {

  private final R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper;

  public R2DBCWorkerIdAssigner(R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper) {
    this.r2dbcWorkerNodeMapper = r2dbcWorkerNodeMapper;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  protected long assignWorkerId(WorkerNodeEntity workerNodeEntity) {
    return r2dbcWorkerNodeMapper.getWorkerNodeByHostPort(workerNodeEntity.getHostName(),workerNodeEntity.getPort())
        .switchIfEmpty(r2dbcWorkerNodeMapper.addWorkerNode(workerNodeEntity).map(row -> workerNodeEntity))
        .map(WorkerNodeEntity::getId)
        .block();
  }
}
