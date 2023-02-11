package io.github.cooperlyt.cloud.uid;

public interface UidCachedProperties {

  /**
   * RingBuffer size扩容参数, 可提高UID生成的吞吐量.
   * 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
   */
  int getBoostPower();

  /**
   * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
   * 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
   * 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
   */
  int getPaddingFactor();

  Long getScheduleInterval();

}
