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

import io.github.cooperlyt.cloud.uid.UidCachedProperties;
import io.github.cooperlyt.cloud.uid.UidProperties;
import io.github.cooperlyt.cloud.uid.buffer.*;
import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.exception.UidGenerateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cached implementation of {@link UidGenerator} extends
 * from {@link DefaultUidGenerator}, based on a lock free {@link RingBuffer}<p>
 * <p>
 * The spring properties you can specified as below:<br>
 * <li><b>boostPower:</b> RingBuffer size boost for a power of 2, Sample: boostPower is 3, it means the buffer size
 * will be <code>({@link BitsAllocator#getMaxSequence()} + 1) &lt;&lt;
 * {@link #boostPower}</code>, Default as {@value #DEFAULT_BOOST_POWER}
 * <li><b>paddingFactor:</b> Represents a percent value of (0 - 100). When the count of rest available UIDs reach the
 * threshold, it will trigger padding buffer. Default as{@link RingBuffer#DEFAULT_PADDING_PERCENT}
 * Sample: paddingFactor=20, bufferSize=1000 -> threshold=1000 * 20 /100, padding buffer will be triggered when tail-cursor<threshold
 * <li><b>scheduleInterval:</b> Padding buffer in a schedule, specify padding buffer interval, Unit as second
 * <li><b>rejectedPutBufferHandler:</b> Policy for rejected put buffer. Default as discard put request, just do logging
 * <li><b>rejectedTakeBufferHandler:</b> Policy for rejected take buffer. Default as throwing up an exception
 *
 * @author yutianbao
 * @author wujun
 */
@Slf4j
public class CachedUidGenerator extends DefaultUidGenerator implements DisposableBean {

    /**
     * RingBuffer
     */
    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;

    /** 拒绝策略: 当环已满, 无法继续填充时
     默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式)并以@Autowired方式注入 */
    private final RejectedPutBufferHandler rejectedPutBufferHandler;

    /** 拒绝策略: 当环已空, 无法继续获取时
     默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式)并以@Autowired方式注入 */
    private final TimeIsFutureHandler timeIsFutureHandler;

    private final UidCachedProperties uidCachedProperties;

    public CachedUidGenerator(
        UidProperties uidProperties,
        UidCachedProperties uidCachedProperties,
        WorkerIdAssigner workerIdAssigner,
        RejectedPutBufferHandler rejectedPutBufferHandler,
        TimeIsFutureHandler timeIsFutureHandler
    ) {
        super(uidProperties, workerIdAssigner);
        this.uidCachedProperties = uidCachedProperties;
        this.timeIsFutureHandler = timeIsFutureHandler;
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    public CachedUidGenerator(
        UidProperties uidProperties,
        UidCachedProperties uidCachedProperties,
        WorkerIdAssigner workerIdAssigner,
        TimeIsFutureHandler timeIsFutureHandler
    ) {
        super(uidProperties, workerIdAssigner);
        this.uidCachedProperties = uidCachedProperties;
        this.timeIsFutureHandler = timeIsFutureHandler;
        this.rejectedPutBufferHandler = null;
    }

    public CachedUidGenerator(
        UidProperties uidProperties,
        UidCachedProperties uidCachedProperties,
        WorkerIdAssigner workerIdAssigner,
        RejectedPutBufferHandler rejectedPutBufferHandler
    ) {
        super(uidProperties, workerIdAssigner);
        this.uidCachedProperties = uidCachedProperties;
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
        this.timeIsFutureHandler = null;
    }

    public CachedUidGenerator(
        UidProperties uidProperties,
        UidCachedProperties uidCachedProperties,
        WorkerIdAssigner workerIdAssigner
    ) {
        super(uidProperties, workerIdAssigner);
        this.uidCachedProperties = uidCachedProperties;
        this.timeIsFutureHandler = null;
        this.rejectedPutBufferHandler = null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // initialize workerId & bitsAllocator
        super.afterPropertiesSet();

        // initialize RingBuffer & RingBufferPaddingExecutor
        this.initRingBuffer();
        log.info("Initialized RingBuffer successfully.");
    }

    @Override
    public Mono<Long> getUID() {
        try {
            return workerId.flatMap(id -> ringBuffer.take(id));
        } catch (Exception e) {
            log.error("Generate unique id exception. ", e);
            throw new UidGenerateException(e);
        }
    }

    @Override
    public String parseUID(long uid) {
        return super.parseUID(uid);
    }

    @Override
    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
        super.destroy();
    }

    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @param currentSecond
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long workerId, long currentSecond) {

        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        // Allocate the first sequence of the second, the others can be calculated with the offset
        long firstSeqUid = bitsAllocator.allocate(currentSecond - uidProperties.getEpochSeconds(), workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }


    /**
     * Initialize RingBuffer & RingBufferPaddingExecutor
     */
    private void initRingBuffer() {
        // initialize RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << uidCachedProperties.getBoostPower();
        this.ringBuffer = new RingBuffer(bufferSize, uidCachedProperties.getPaddingFactor());
        log.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, uidCachedProperties.getPaddingFactor());

        // initialize RingBufferPaddingExecutor
        boolean usingSchedule = (uidCachedProperties.getScheduleInterval() != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(uidCachedProperties.getScheduleInterval());
        }
        if (timeIsFutureHandler != null){
            bufferPaddingExecutor.setTimeToFutureHandler(timeIsFutureHandler);
        }

        log.info("Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule, uidCachedProperties.getScheduleInterval());

        // set rejected put/take handle policy
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);

        if (rejectedPutBufferHandler != null){
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }


        // fill in all slots of the RingBuffer
        //bufferPaddingExecutor.paddingBuffer();

        // start buffer padding threads
        workerId.subscribe(id -> bufferPaddingExecutor.start(id));
        //bufferPaddingExecutor.start();
    }

}
