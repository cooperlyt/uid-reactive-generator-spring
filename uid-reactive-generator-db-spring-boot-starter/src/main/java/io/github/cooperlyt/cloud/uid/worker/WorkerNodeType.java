package io.github.cooperlyt.cloud.uid.worker;

import io.github.cooperlyt.cloud.uid.utils.ValuedEnum;

public enum WorkerNodeType implements ValuedEnum<Integer> {

  /**
   * 容器
   */
  CONTAINER(1),
  /**
   * 物理机
   */
  ACTUAL(2),

  /**
   * spring cloud
   */
  SPRING_WEB(3);

  /**
   * Lock type
   */
  private final Integer type;

  /**
   * Constructor with field of type
   */
  WorkerNodeType(Integer type) {
    this.type = type;
  }

  @Override
  public Integer value() {
    return type;
  }

}
