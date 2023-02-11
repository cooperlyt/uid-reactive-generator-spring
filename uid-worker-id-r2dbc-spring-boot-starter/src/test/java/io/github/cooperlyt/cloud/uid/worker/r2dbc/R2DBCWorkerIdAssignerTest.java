package io.github.cooperlyt.cloud.uid.worker.r2dbc;

import io.github.cooperlyt.cloud.uid.exception.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
class R2DBCWorkerIdAssignerTest {

  @Autowired
  private WorkerIdAssigner workerIdAssigner;


  @Test
  public void testDB(){

    long l = workerIdAssigner.assignWorkerId();
    log.info("return : " + l);
    assertTrue(l > 0);

  }
}