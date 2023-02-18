package io.github.cooperlyt.cloud.uid.impl;

import io.github.cooperlyt.cloud.uid.UidProperties;
import io.github.cooperlyt.cloud.uid.utils.DateUtils;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * UID 的配置
 *
 * @author wujun
 * @date 2019.02.20 10:31
 */
public class UidConfigProperties implements UidProperties {



    /**
     * 时间增量值占用位数。当前时间相对于时间基点的增量值，单位为秒
     */
    private int timeBits = 30;

    /**
     * 工作机器ID占用的位数
     */
    private int workerBits = 16;

    /**
     * 序列号占用的位数
     */
    private int seqBits = 7;

    /**
     * 时间基点. 例如 2019-02-20 (毫秒: 1550592000000)
     */
    private String epochStr = "2019-02-20";

    /**
     * 时间基点对应的毫秒数
     */
    private long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1550592000000L);

    /**
     * 时钟回拨最长容忍时间（秒）
     */
    private long maxBackwardSeconds = 1L;

    /**
     * 是否允许使用未来时间
     */
    private boolean enableFutureTime = false;

    @Override
    public boolean isEnableFutureTime() {
        return enableFutureTime;
    }

    @Override
    public void setEnableFutureTime(boolean enableFutureTime) {
        this.enableFutureTime = enableFutureTime;
    }

    @Override
    public int getTimeBits() {
        return timeBits;
    }

    @Override
    public void setTimeBits(int timeBits) {
        if (timeBits > 0) {
            this.timeBits = timeBits;
        }
    }

    @Override
    public int getWorkerBits() {
        return workerBits;
    }

    @Override
    public void setWorkerBits(int workerBits) {
        if (workerBits > 0) {
            this.workerBits = workerBits;
        }
    }

    @Override
    public int getSeqBits() {
        return seqBits;
    }

    @Override
    public void setSeqBits(int seqBits) {
        if (seqBits > 0) {
            this.seqBits = seqBits;
        }
    }

    @Override
    public void setEpochStr(String epochStr) {
        if (StringUtils.isNotBlank(epochStr)) {
            this.epochStr = epochStr;
            this.epochSeconds = TimeUnit.MILLISECONDS.toSeconds(DateUtils.parseByDayPattern(epochStr).getTime());
        }
    }

    @Override
    public long getEpochSeconds() {
        return epochSeconds;
    }

    @Override
    public long getMaxBackwardSeconds() {
        return maxBackwardSeconds;
    }

    @Override
    public void setMaxBackwardSeconds(long maxBackwardSeconds) {
        this.maxBackwardSeconds = maxBackwardSeconds;
    }

}
