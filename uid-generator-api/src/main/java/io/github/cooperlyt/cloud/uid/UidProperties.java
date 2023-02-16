package io.github.cooperlyt.cloud.uid;

public interface UidProperties {


  int getTimeBits();

  /**
   * @param timeBits 时间增量值占用位数。当前时间相对于时间基点的增量值，单位为秒
   */
  void setTimeBits(int timeBits);


  int getWorkerBits();

  /**
   * @param value 工作机器ID占用的位数
   */
  void setWorkerBits(int value);


  int getSeqBits();

  /**
   * @param value 序列号占用的位数
   */

  void setSeqBits(int value);


  long getEpochSeconds();

  /**
   * 发号起始时间
   *
   * @param value
   * 时间基点. 例如 2019-02-20 (毫秒: 1550592000000)
   *
   */
  void setEpochStr(String value);


  long getMaxBackwardSeconds();

  /**
   * 系统时钟回拨和使用未来时间最长容忍时间（秒）
   *
   * @param value
   *
   * 设置为0时如果发生系统回拨会抛出异常并无法使用未来时间生成ID
   *
   *
   */
  void setMaxBackwardSeconds(long value);

  boolean isEnableFutureTime();

  /**
   * 当前时间所生成的ID超出最大序列时和时钟回拨时是否允许使用未来时间的序列来生成ID
   *
   * 如果ID发号量巨大而经常需要使用到未来时间应当使用 {@link  CachedUidGenerator},
   * 因持续使用未来时间而得不到回正会引起服务重启后生成重复的ID
   *
   * @param enable
   *
   * true: 会使用未来时间来生成ID会造成ID中的时间不准确,
   * 可以使用多少未来时间由 maxBackwardSeconds 控制
   *
   * false: 会返回一下 Mono 等待到达当前时间后通知订阅者
   *
   */
  void setEnableFutureTime(boolean enable);

}
