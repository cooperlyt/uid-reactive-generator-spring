package io.github.cooperlyt.cloud.uid.impl;

import io.github.cooperlyt.cloud.uid.UidCachedProperties;
import io.github.cooperlyt.cloud.uid.buffer.RingBuffer;
import org.springframework.util.Assert;


public class UidCachedConfigProperties implements UidCachedProperties {

  private static final int DEFAULT_BOOST_POWER = 3;



  // --------------------- 配置属性 begin ---------------------
  /**
   * RingBuffer size扩容参数, 可提高UID生成的吞吐量.
   * 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
   */
  private int boostPower = DEFAULT_BOOST_POWER;
  /**
   * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
   * 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
   * 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
   */
  private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;

  /**
   * 另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充
   * 默认:不配置此项, 即不使用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
   */
  private Long scheduleInterval;


  @Override
  public int getBoostPower() {
    return boostPower;
  }

  @Override
  public int getPaddingFactor() {
    return paddingFactor;
  }

  @Override
  public Long getScheduleInterval() {
    return scheduleInterval;
  }

  @Override
  public void setBoostPower(int boostPower) {
    Assert.isTrue(boostPower > 0, "Boost power must be positive!");
    this.boostPower = boostPower;
  }

  @Override
  public void setPaddingFactor(int paddingFactor) {
    Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "padding factor must be in (0, 100)!");
    this.paddingFactor = paddingFactor;
  }

  @Override
  public void setScheduleInterval(long scheduleInterval) {
    Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
    this.scheduleInterval = scheduleInterval;
  }


}
