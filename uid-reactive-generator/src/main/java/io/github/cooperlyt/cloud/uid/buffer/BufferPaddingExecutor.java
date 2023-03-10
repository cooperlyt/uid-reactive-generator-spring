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
package io.github.cooperlyt.cloud.uid.buffer;

import io.github.cooperlyt.cloud.uid.utils.NamingThreadFactory;
import io.github.cooperlyt.cloud.uid.utils.PaddedAtomicLong;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an executor for padding {@link RingBuffer}<br>
 * There are two kinds of executors: one for scheduled padding, the other for padding immediately.
 * 
 * @author yutianbao
 */
@Slf4j
public class BufferPaddingExecutor {
    /** Constants */
    private static final String WORKER_NAME = "RingBuffer-Padding-Worker";
    private static final String SCHEDULE_NAME = "RingBuffer-Padding-Schedule";
    /** 5 minutes */
    private static final long DEFAULT_SCHEDULE_INTERVAL = 5 * 60L;
    
    /** Whether buffer padding is running */
    private final AtomicBoolean running;

    /** We can borrow UIDs from the future, here store the last second we have consumed */
    private final PaddedAtomicLong lastSecond;

    /** RingBuffer & BufferUidProvider */
    private final RingBuffer ringBuffer;
    private final BufferedUidProvider uidProvider;

    /** Padding immediately by the thread pool */
    private final ExecutorService bufferPadExecutors;
    /** Padding schedule thread */
    private final ScheduledExecutorService bufferPadSchedule;
    
    /** Schedule interval Unit as seconds */
    private long scheduleInterval = DEFAULT_SCHEDULE_INTERVAL;

    private TimeIsFutureHandler timeIsFutureHandler = this::allowTimeToFuture;

    /**
     * Constructor with {@link RingBuffer} and {@link BufferedUidProvider}, default use schedule
     *
     * @param ringBuffer {@link RingBuffer}
     * @param uidProvider {@link BufferedUidProvider}
     */
    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider uidProvider) {
        this(ringBuffer, uidProvider, true);
    }

    /**
     * Constructor with {@link RingBuffer}, {@link BufferedUidProvider}, and whether use schedule padding
     *
     * @param ringBuffer {@link RingBuffer}
     * @param uidProvider {@link BufferedUidProvider}
     * @param usingSchedule
     */
    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider uidProvider, boolean usingSchedule) {
        this.running = new AtomicBoolean(false);
        this.lastSecond = new PaddedAtomicLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        this.ringBuffer = ringBuffer;
        this.uidProvider = uidProvider;

        // initialize thread pool
        int cores = Runtime.getRuntime().availableProcessors();
        bufferPadExecutors = Executors.newFixedThreadPool(cores * 2, new NamingThreadFactory(WORKER_NAME));

        // initialize schedule thread
        if (usingSchedule) {
            bufferPadSchedule = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory(SCHEDULE_NAME));
        } else {
            bufferPadSchedule = null;
        }
    }

    /**
     * Start executors such as schedule
     */
    public void start() {
        if (bufferPadSchedule != null) {
            bufferPadSchedule.scheduleWithFixedDelay(this::asyncPadding, scheduleInterval, scheduleInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Shutdown executors
     */
    public void shutdown() {
        if (!bufferPadExecutors.isShutdown()) {
            bufferPadExecutors.shutdownNow();
        }

        if (bufferPadSchedule != null && !bufferPadSchedule.isShutdown()) {
            bufferPadSchedule.shutdownNow();
        }
    }

    /**
     * Whether is padding
     *
     * @return
     */
    public boolean isRunning() {
        return running.get();
    }


    private Mono<Void> paddingFuture = Mono.empty();

    public Mono<Void> getPaddingFuture() {
        return paddingFuture;
    }

    /**
     * Padding buffer in the thread pool
     */
    public void asyncPadding() {
//        Mono.fromFuture(CompletableFuture.runAsync(this::paddingBuffer,bufferPadExecutors)).
        paddingFuture = Mono.fromFuture(CompletableFuture.runAsync(this::paddingBuffer,bufferPadExecutors)).cache();
//        t.isDone()
//        bufferPadExecutors.submit(this::paddingBuffer).;
    }

    /**
     * Padding buffer fill the slots until to catch the cursor
     */
    public void paddingBuffer() {

        log.info("Ready to padding buffer lastSecond:{}. {}", lastSecond.get(), ringBuffer);

        // is still running
        if (!running.compareAndSet(false, true)) {
            log.info("Padding buffer is still running. {}", ringBuffer);
            return;
        }

        // fill the rest slots until to catch the cursor
        boolean isFullRingBuffer = false;
        while (!isFullRingBuffer) {
            long providerTime = lastSecond.incrementAndGet();
            long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            if (providerTime > currentTime){
                timeIsFutureHandler.timeIsFuture(providerTime,currentTime);
            }
            List<Long> uidList = uidProvider.provide(providerTime);
            for (Long uid : uidList) {
                isFullRingBuffer = !ringBuffer.put(uid);
                if (isFullRingBuffer) {
                    break;
                }
            }
        }

        // not running now
        running.compareAndSet(true, false);
        log.info("End to padding buffer lastSecond:{}. {}", lastSecond.get(), ringBuffer);
    }

    /**
     * Policy for {@link TimeIsFutureHandler}, we just do logging
     */
    protected void allowTimeToFuture(long idTime, long currentTime) {
        log.warn("Rejected take buffer. id time: {} MILLISECONDS, currentTime: {} MILLISECONDS", idTime, currentTime);
    }

    /**
     * Setters
     */
    public void setScheduleInterval(long scheduleInterval) {
        assert scheduleInterval > 0 : "Schedule interval must positive!";
        this.scheduleInterval = scheduleInterval;
    }

    public void setTimeToFutureHandler(TimeIsFutureHandler timeIsFutureHandler){
        this.timeIsFutureHandler = timeIsFutureHandler;
    }


    
}
