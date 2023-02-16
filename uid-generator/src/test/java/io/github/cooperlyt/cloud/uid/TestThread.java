package io.github.cooperlyt.cloud.uid;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class TestThread {

  private long test = 0;

  public long returnLong(){
    System.out.println("ccc");
    try {
      Thread.sleep(10000);
      test = 1;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return 1L;
  }

  @Test void testError(){
    Mono.just(1)
        .flatMap(l -> l == 1 ?  Mono.error(new IllegalAccessException()) : Mono.just(2) )
        .map(l -> {System.out.println("aaa");return l;} )
        .switchIfEmpty(Mono.just(3))
        .map(l -> {System.out.println("bbb");return l;})
        .as(StepVerifier::create)
        //.assertNext(l -> System.out.println("ccc"))
        .verifyError();
  }

  @Test
  public void test(){



    long t = 0;
    Executor delayed = CompletableFuture.delayedExecutor(2L, TimeUnit.SECONDS);
    Mono<Long>  aa =   Mono.fromFuture(CompletableFuture.supplyAsync(this::returnLong))
   // Mono<Long> aa = Mono.just(this.returnLong())
        .map(l -> {System.out.println("bbb");return l;});

    System.out.println("test var is:" + test);

        try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
        System.out.println("1:" + t);
     aa =   aa.cache();
    System.out.println("2:" + t);

    aa =   aa.share();
    System.out.println("3:" + t);



    Mono<Long> bb = aa.share();
aa.as(StepVerifier::create)
        .assertNext(l -> System.out.println("r:" + l))
        .verifyComplete();


    aa.share().as(StepVerifier::create)
        .assertNext(l -> System.out.println("r:" + l))
        .verifyComplete();

//  Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//      .assertNext(l -> System.out.println("r:" + l))
//      .verifyComplete();
//
//    Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//        .assertNext(l -> System.out.println("r:" + l))
//        .verifyComplete();
//    Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//        .assertNext(l -> System.out.println("r:" + l))
//        .verifyComplete();
//
//    Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//        .assertNext(l -> System.out.println("r:" + l))
//        .verifyComplete();
//    Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//        .assertNext(l -> System.out.println("r:" + l))
//        .verifyComplete();
//
//    Mono.empty().switchIfEmpty(bb).as(StepVerifier::create)
//        .assertNext(l -> System.out.println("r:" + l))
//        .verifyComplete();

  }





}
