package io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
class WorkerIdAssignerTest {

  @Autowired
  private WorkerIdAssigner workerIdAssigner;


  @Test
  public void testDB(){

    Mono<Long>  m = workerIdAssigner.assignWorkerId().cache();
    System.out.println("tttt");

    m.as(StepVerifier::create).assertNext(l ->
            {
            assertTrue(l > 0);
            System.out.println("worker id is:" + l);
            }
        )
        .verifyComplete();


  }
}