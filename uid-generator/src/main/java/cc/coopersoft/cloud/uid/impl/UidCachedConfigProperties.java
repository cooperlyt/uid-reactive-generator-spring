package cc.coopersoft.cloud.uid.impl;

import cc.coopersoft.cloud.uid.UidProperties;
import cc.coopersoft.cloud.uid.buffer.RejectedPutBufferHandler;
import cc.coopersoft.cloud.uid.buffer.RejectedTakeBufferHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;



public class UidCachedProperties implements cc.coopersoft.cloud.uid.UidCachedProperties {
  @Override
  public int getBoostPower() {
    return 0;
  }

  @Override
  public int getPaddingFactor() {
    return 0;
  }

  @Override
  public RejectedPutBufferHandler getRejectedPutBufferHandler() {
    return null;
  }

  @Override
  public RejectedTakeBufferHandler getRejectedTakeBufferHandler() {
    return null;
  }
}
