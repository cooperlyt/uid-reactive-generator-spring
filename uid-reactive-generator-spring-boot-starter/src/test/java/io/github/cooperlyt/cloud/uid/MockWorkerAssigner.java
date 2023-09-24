package io.github.cooperlyt.cloud.uid;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MockWorkerAssigner implements WorkerIdAssigner {


  @Override
  public Mono<Long> assignWorkerId() {
    return Mono.just(1L);
  }

  @Override
  public void releaseWorkerId(long id, long lastTime) {

  }
}
