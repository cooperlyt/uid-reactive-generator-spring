/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cooperlyt.cloud.uid.impl;

import io.github.cooperlyt.cloud.uid.BitsAllocator;
import io.github.cooperlyt.cloud.uid.UidGenerator;
import io.github.cooperlyt.cloud.uid.UidProperties;
import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.exception.UidGenerateException;
import io.github.cooperlyt.cloud.uid.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Represents an implementation of {@link UidGenerator}
 * <p>
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <li>sign: The highest bit is 0
 * <li>delta seconds: The next 28 bits, represents delta seconds since a customer epoch(2016-05-20 00:00:00.000).
 * Supports about 8.7 years until to 2024-11-20 21:24:16
 * <li>worker id: The next 22 bits, represents the worker's id which assigns based on database, max id is about 420W
 * <li>sequence: The next 13 bits, represents a sequence within the same second, max for 8192/s<br><br>
 * <p>
 * The {@link DefaultUidGenerator#parseUID(long)} is a tool method to parse the bits
 * <p>
 * <pre>{@code
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          28bits              22bits         13bits
 * }</pre>
 * <p>
 * You can also specified the bits by Spring property setting.
 * <li>timeBits: default as 28
 * <li>workerBits: default as 22
 * <li>seqBits: default as 13
 * <li>epochStr: Epoch date string format 'yyyy-MM-dd'. Default as '2016-05-20'<p>
 * <p>
 * <b>Note that:</b> The total bits must be 64 -1
 *
 * @author yutianbao
 * @author wujun
 */

@Slf4j
public class DefaultUidGenerator implements UidGenerator, InitializingBean, DisposableBean {

  public DefaultUidGenerator(UidProperties uidProperties, WorkerIdAssigner workerIdAssigner) {
    this.uidProperties = uidProperties;
    this.workerIdAssigner = workerIdAssigner;
  }

  protected final UidProperties uidProperties;

  private final WorkerIdAssigner workerIdAssigner;

  /**
   * Bit分配器,Stable fields after spring bean initializing
   */
  protected BitsAllocator bitsAllocator;
  protected Mono<Long> workerId;

  /**
   * Volatile fields caused by nextId()
   */
  protected long sequence = 0L;
  protected long lastSecond = -1L;


  @Override
  public void afterPropertiesSet() throws Exception {
    log.debug("uid is begging initialize");
    // initialize bits allocator
    bitsAllocator = new BitsAllocator(uidProperties.getTimeBits(), uidProperties.getWorkerBits(), uidProperties.getSeqBits());

    // initialize worker id
    workerId = workerIdAssigner.assignWorkerId()
        .map(workerId -> {
          if (workerId > bitsAllocator.getMaxWorkerId()) {
            log.error("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
            return workerId % bitsAllocator.getMaxWorkerId();

          }
          log.debug("Initialized worker node id:{}",workerId);
          return workerId;
        })
        .switchIfEmpty(Mono.error(new IllegalAccessException()))
        .cache();

    log.info("Initialized bits(1, {}, {}, {}) ", uidProperties.getTimeBits(), uidProperties.getWorkerBits(), uidProperties.getSeqBits());
  }

  @Override
  public Mono<Long> getUID() throws UidGenerateException {
    try {
      log.debug("request uid by default");
      return nextId();
    } catch (Exception e) {
      log.error("Generate unique id exception. ", e);
      throw new UidGenerateException(e);
    }
  }

  @Override
  public String parseUID(long uid) {
    long totalBits = BitsAllocator.TOTAL_BITS;
    long workerIdBits = bitsAllocator.getWorkerIdBits();
    long sequenceBits = bitsAllocator.getSequenceBits();

    // parse UID
    long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
    long workerId = (uid << (totalBits - workerIdBits - sequenceBits)) >>> (totalBits - workerIdBits);
    long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

    Date thatTime = new Date(TimeUnit.SECONDS.toMillis(uidProperties.getEpochSeconds() + deltaSeconds));
    String thatTimeStr = DateUtils.formatByDateTimePattern(thatTime);

    // format as string
    return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
        uid, thatTimeStr, workerId, sequence);
  }

  /**
   * Get UID
   *
   * @return UID
   * @throws UidGenerateException in the case: Clock moved backwards; Exceeds the max timestamp
   */
  protected Mono<Long> nextId() {
    return Mono.just(getCurrentSecond())
        .flatMap(this::idRequest)
        .flatMap(request -> workerId.share()
            .map(workerId -> bitsAllocator
                .allocate(request.second - uidProperties.getEpochSeconds(),
                    workerId,
                    request.sequence))
        );
  }

  public Mono<IdRequest> test(){
    return idRequest(getCurrentSecond());
  }

  public synchronized Mono<IdRequest> idRequest(long requestSecond){

    if (requestSecond < lastSecond){
      long refusedSeconds = lastSecond - requestSecond;

      if (refusedSeconds <= uidProperties.getMaxBackwardSeconds()) {
        if (uidProperties.isEnableFutureTime()) {
          log.info("use future time: {}", lastSecond);
           requestSecond = lastSecond;
        } else {
          Executor delayed = CompletableFuture.delayedExecutor(refusedSeconds - 1, TimeUnit.SECONDS);
          log.info("result is reactive! wait for {} seconds", refusedSeconds);
          return Mono.fromFuture(CompletableFuture.supplyAsync(() -> getNextSecond(lastSecond),delayed))
              .flatMap(this::idRequest);
        }
      } else {
        log.error("Clock moved backwards. Refusing for {} seconds", refusedSeconds);
        return Mono.error(new UidGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds));

        // change worker id? I don`t think so!
//                    workerId = workerIdAssigner.assignFakeWorkerId();
//                    log.error("Clock moved backwards. Assigned New WorkerId %d", workerId);
//                    if (workerId > bitsAllocator.getMaxWorkerId()) {
//                        log.error("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
//                        workerId = workerId % bitsAllocator.getMaxWorkerId();
//                        log.info("new Worker id = " + workerId);
//                    }
      }

    }

    //assert requestSecond >= lastSecond : "no allow backwards in this function";
    if (requestSecond == lastSecond) {
      sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
      // Exceed the max sequence, we wait the next second to generate uid
      if (sequence == 0) {
        requestSecond++;
        if (! uidProperties.isEnableFutureTime()) {
          //currentSecond = getNextSecond(lastSecond);
          log.info("result is reactive, next a second");
          lastSecond = requestSecond;
          return Mono.fromFuture(CompletableFuture.supplyAsync(()-> getNextSecond(lastSecond)))
                      .map(second -> IdRequest.builder().second(second).sequence(0).build());
        }
      }
    } else {
      // At the different second, sequence restart from zero
      sequence = 0L;
    }
    lastSecond = requestSecond;
    return Mono.just(IdRequest.builder().second(lastSecond).sequence(sequence).build());
  }

  /**
   * Get next second
   */
  private long getNextSecond(long lastTimestamp) {
    long timestamp = getCurrentSecond();
    while (timestamp <= lastTimestamp) {
      timestamp = getCurrentSecond();
    }
    return lastTimestamp;
  }
  /**
   * Get current second
   */
  private long getCurrentSecond() {
    long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    if (currentSecond - uidProperties.getEpochSeconds() > bitsAllocator.getMaxDeltaSeconds()) {
      throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
    }

    return currentSecond;
  }

  @Override
  public void destroy() throws Exception {
    workerId.blockOptional().ifPresent(workerId -> workerIdAssigner.releaseWorkerId(workerId, lastSecond));
  }

  @AllArgsConstructor
  @Builder
  @Getter
  static class IdRequest {

    private long second;

    private long sequence;

  }


}
