package io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
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
  private HostWorkerIdAssigner workerIdAssigner;

  @Test
  public void testWorkerId(){
    workerIdAssigner.assignWorkerId().as(StepVerifier::create)
        .assertNext(id -> {assertTrue(id > 0); System.out.println("id is:" + id);})
            .verifyComplete();


  }

}