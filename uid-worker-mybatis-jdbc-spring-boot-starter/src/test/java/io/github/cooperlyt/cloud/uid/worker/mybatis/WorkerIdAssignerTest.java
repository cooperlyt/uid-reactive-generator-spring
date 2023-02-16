package io.github.cooperlyt.cloud.uid.worker.mybatis;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
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
class WorkerIdAssignerTest {

  @Autowired
  private WorkerIdAssigner workerIdAssigner;

  @Test
  public void testDB() {
    workerIdAssigner.assignWorkerId().as(StepVerifier::create)
        .assertNext(workerId -> assertTrue(workerId > 0))
        .verifyComplete();
  }

}