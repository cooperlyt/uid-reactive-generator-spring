package io.github.cooperlyt.cloud.uid.worker.jpa.jdbc;

import io.github.cooperlyt.cloud.uid.worker.HostWorkerIdAssigner;
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
class WorkerIdAssignerTest {


  @Autowired
  private HostWorkerIdAssigner workerIdAssigner;

  @Test
  public void testWorkerId(){
    assertTrue(workerIdAssigner.assignWorkerId() > 1L);

  }

  @Test
  public void test(){
    long a = 4L;
    a = a % 3L;
    log.debug("return" + a);
  }
}