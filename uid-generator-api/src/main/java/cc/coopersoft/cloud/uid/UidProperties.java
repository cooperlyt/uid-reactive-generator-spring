package cc.coopersoft.cloud.uid;

public interface UidProperties {

  /**
   * 时间增量值占用位数。当前时间相对于时间基点的增量值，单位为秒
   */
  int getTimeBits();

  /**
   * 工作机器ID占用的位数
   */
  int getWorkerBits();

  /**
   * 序列号占用的位数
   */
  int getSeqBits();

  /**
   * 时间基点. 例如 2019-02-20 (毫秒: 1550592000000)
   */
  long getEpochSeconds();

  /**
   * 是否容忍时钟回拨, 默认:true
   */
  boolean isEnableBackward();

  /**
   * 时钟回拨最长容忍时间（秒）
   */
  long getMaxBackwardSeconds();

}
