package io.github.cooperlyt.cloud.uid;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class WorkerNodeIdTest {


  @Autowired
  private WorkerIdAssigner workerIdAssigner;

  @Test
  public void testWorkerIdAssigner(){

    workerIdAssigner.assignWorkerId().as(StepVerifier::create)
        .assertNext(id -> {
          log.info("id is :{}", id);
          assertTrue(id > 0L);
        })
        .verifyComplete();
  }

}