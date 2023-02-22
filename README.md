
uid-reactive-generator-spring
==========================

[In Chinese 中文版](https://github.com/cooperlyt/uid-generator-spring-boot/blob/master/README.cn.md)

UidReactiveGenerator is a Java implemented, [Snowflake](https://github.com/twitter/snowflake) based distributed unique ID generator, Supported Reactive Programming and R2DBC。

Based on [Snowflake](https://github.com/twitter/snowflake)，[UidGenerator](https://github.com/baidu/uid-generator)，[uid-generator-spring-boot-starter](https://github.com/wujun234/uid-generator-spring-boot-starter)


* Change packages，Extract Worker node id API.
* Supported Spring boot Autoconfigure 
* Supported Reactive Programming，Return a `Mono<Long>`, 
  * In CachedUidGenerator：When consumption rate is higher than refill rate，Notify subscribers in a non-blocking manner after waiting for the fill to complete.
  * In DefaultUidGenerator and not allow use future time state：Notify subscribers in a non-blocking way after the current time ID is exhausted and waits for the next second.
* Supported get worker node id from db by mybatis jdbc, mybatis r2bc , jap jdbc , jap r2dbc 
* Supported get worker node id from Spring Discovery service


## Principle and performance

Refer [Snowflake](https://github.com/twitter/snowflake) and [UidGenerator](https://github.com/baidu/uid-generator)

## Usage

###  For Spring boot autoconfig

#### Worker node ID by Spring Discover service(not need databases)

```xml
<dependency>
  <groupId>io.github.cooperlyt</groupId>
  <artifactId>uid-reactive-generator-spring-cloud-starter-discovery</artifactId>
  <version>1.1.1</version>
</dependency>

... 
```
NOTE: only test on Consul

#### Worker node ID by DB

```xml
<dependency>
  <groupId>io.github.cooperlyt</groupId>
  <artifactId>uid-reactive-generator-db-spring-boot-starter</artifactId>
  <version>1.1.1</version>
</dependency>
```
* Mybatis JDBC:
```xml
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.3.0</version>
        </dependency>
```
* Mybatis R2DBC
  Refer [reactive-mybatis-support](https://github.com/chenggangpro/reactive-mybatis-support)
* JPA JDBC:
```xml
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>
```
* JPA R2DBC
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-r2dbc</artifactId>
        </dependency>
```


* Databases scrip：
```sql
DROP TABLE IF EXISTS WORKER_NODE;
CREATE TABLE WORKER_NODE
(
  ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
  HOST VARCHAR(64) NOT NULL COMMENT 'host name',
  PORT VARCHAR(64) NOT NULL COMMENT 'port',
  TYPE INT NOT NULL COMMENT 'node type: CONTAINER(1), ACTUAL(2), FAKE(3)',
  LAUNCH DATE NOT NULL COMMENT 'launch date',
  MODIFIED TIMESTAMP NOT NULL COMMENT 'modified time',
  CREATED TIMESTAMP NOT NULL COMMENT 'created time',
  PRIMARY KEY(ID)
) COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```





####  CachedUidGenerator rejected handler(optional)

```java
  // After generating a batch of IDs, the processing method when the buffer ring is full and cannot be filled, the default is to discard and print the log
  @Bean
  RejectedPutBufferHandler customPutHandler() {
      return (buffer, id) -> {
      //do your
      };
  }

  // The processing method when the future time (time that has not arrived) is used due to too fast consumption, the default is to allow and print the log
  @Bean
  TimeIsFutureHandler customFutureTimeHandler() {
      return (futureTime, currentTime) -> {
      //do your
      };
  }
  
```

#### ID generator configures。
```yml
uid:
  timeBits: 30             # default: 30
  workerBits: 16           # worker node bits, default:16
  seqBits: 7               # sequence bits, default:7
  epochStr: "2023-02-17"   # Initial time, default:"2019-02-20"
  enableFutureTime: false  # all use future time , how long is `maxBackwardSeconds` ， default: false
  maxBackwardSeconds: 1    # The maximum tolerance time for system clock callback and use of future time (seconds), default:1
  CachedUidGenerator:     # CachedUidGenerator configures
    boostPower: 3          # RingBuffer size, default:3
    paddingFactor: 50      # Specifies when to fill the UID into the RingBuffer, the value is a percentage (0, 100), the default is 50
    #scheduleInterval: 60    # Default: Do not configure this item, that is, do not use the Schedule thread to fill the buffer ring regularly. If you want to use it, please specify the Schedule thread time interval, unit: second
```

### Code

#### 

```java
//Real time
//@Resource
//private UidGenerator defaultUidGenerator;

//After generating an id, generate a batch of ids according to the serial number + 1, and cache them for later requests
@Resource
private UidGenerator cachedUidGenerator;



@Test
public void testSerialGenerate() {
    // Generate UID
    cachedUidGenerator.getUID().flatMap(id -> {
      // Do your`s ...
        });

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"450795408770","timestamp":"2019-02-20 14:55:39","workerId":"27","sequence":"2"}
    System.out.println(cachedUidGenerator.parseUID(uid));

}
```

#### Strategic choice


* CachedUidGenerator:

    It is suitable for **continuously high consumption** ID distribution, and will also maintain the accuracy of ID sorting for certificate generation. This method will increase memory and CPU cache usage to a certain extent.

* DefaultUidGenerator and enableFutureTime is true :

  It is suitable for **continuously maintaining low consumption after occasional sudden consumption increases** ID distribution. This method has higher performance than CachedUidGenerator when sudden high consumption occurs, but it should not continue to maintain high consumption (consumable future The time is controlled by maxBackwardSeconds, and an exception will be thrown after exceeding), because excessive use of the future time may cause duplicate IDs to be generated after the service is restarted and the ID sorting in a short period of time will be inaccurate.
* DefaultUidGenerator and enableFutureTime is false :

  It is suitable for **low consumption real-time** ID distribution. This method can guarantee the accuracy of the time in the ID and can meet the high-precision ID sorting. Once the consumption is higher than the current time, a waiting response will be returned. , wait for the next available time to issue the ID (this method has the lowest performance when the consumption is greater than the number of IDs that can be generated at the current time), the maximum waiting time is controlled by maxBackwardSeconds, and an exception is thrown after exceeding.
