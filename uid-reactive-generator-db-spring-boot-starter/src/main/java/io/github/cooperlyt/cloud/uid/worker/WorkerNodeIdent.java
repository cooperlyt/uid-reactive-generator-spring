package io.github.cooperlyt.cloud.uid.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;

@Data
@SuperBuilder
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public class WorkerNodeIdent {
  /**
   * Type of CONTAINER: HostName, ACTUAL : IP.
   */
  private String host;
  /**
   * Type of CONTAINER: Port, ACTUAL : Timestamp + Random(0-10000)
   */
  private String port;

  /**
   * type of {@link WorkerNodeType}
   */
  private int type;
}
