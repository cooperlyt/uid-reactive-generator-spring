
uid-reactive-generator-spring
==========================

UidReactiveGenerator是Java实现的, 基于[Snowflake](https://github.com/twitter/snowflake)算法的支持响应式编程分布式唯一ID生成器。

基与 [雪花算法](https://github.com/twitter/snowflake)，[百度UidGenerator](https://github.com/baidu/uid-generator)，[uid-generator-spring-boot-starter](https://github.com/wujun234/uid-generator-spring-boot-starter)


* 更改了打包结构，抽出了Worker node id的生成接口， 使其可以自定义实现分配 Worker node id的方法， 本项目中实现了四种数据库存储的方法。
* 支持 Spring boot Autoconfigure 
* 支持响应式编程，返回`Mono<Long>`, 
  * 在CachedUidGenerator：消耗速度高于填充速度时，等待填充完成后以非阻塞的方式通知订阅者。
  * 在DefaultUidGenerator不允许使用未来时间（未到达的时间）状态：当前时间ID耗尽等待下一秒后非阻塞的方式通知订阅者
* 支持 mybatis jdbc, mybatis r2bc , jap jdbc ,jap r2dbc 四种数据库连接方式
* 支持通过Spring发现服务生成WorkerNodeID（不需要数据库）

你以可以根据[接口包](https://github.com/cooperlyt/uid-generator-spring-boot/tree/master/uid-generator-api)自己实现Worker node分配的实现

## 原理和性能

请参见[Snowflake](https://github.com/twitter/snowflake)和[百度UidGenerator](https://github.com/baidu/uid-generator)

## 使用

|project-version|spring-boot|
|:--|:--|
|`1.1.x`|`<=2.7.x`|
|`1.2.x`|`>=3`|

#### spring boot autoconfig 方式

#### Worker node ID by Spring Discover service(不需要数据库)

```xml
<dependency>
  <groupId>io.github.cooperlyt</groupId>
  <artifactId>uid-reactive-generator-spring-cloud-starter-discovery</artifactId>
  <version>1.1.1</version>
</dependency>

... 
```
NOTE: 仅在Consul下测试，其它发现服务器没有进行测试。

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
* Mybatis R2DBC (从1.2版本以后移除)

  参见 [reactive-mybatis-support](https://github.com/chenggangpro/reactive-mybatis-support)
  
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



### 数据库（可选）
如果使用数据库实现的Worker node id分配器,需要先建立表 WORKER_NODE, 脚本：
```sql
DROP TABLE IF EXISTS WORKER_NODE;
CREATE TABLE WORKER_NODE
(
ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
HOST_NAME VARCHAR(64) NOT NULL COMMENT 'host name',
PORT VARCHAR(64) NOT NULL COMMENT 'port',
TYPE INT NOT NULL COMMENT 'node type: CONTAINER(1), ACTUAL(2), FAKE(3)',
LAUNCH_DATE DATE NOT NULL COMMENT 'launch date',
MODIFIED TIMESTAMP NOT NULL COMMENT 'modified time',
CREATED TIMESTAMP NOT NULL COMMENT 'created time',
PRIMARY KEY(ID)
) COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```

#### 自定义 CachedUidGenerator 拒绝策略 

```java
  // 生成一批ID后由于buffer环已满无法填充时的处理方式, 默认为丢弃并打印日志
  @Bean
  RejectedPutBufferHandler customPutHandler() {
      return (buffer, id) -> {
      do your
      };
  }

  // 由于消耗过快使用到了未来时间（未到达的时间）时的处理方式, 默认为允许并打印日志
  @Bean
  TimeIsFutureHandler customFutureTimeHandler() {
      return (futureTime, currentTime) -> {
      do your
      };
  }
  
```

#### ID 生成配置， 如应用在生产环境请确认以下参数，并确保你已经理解每个参数的意义。
```yml
uid:
  timeBits: 30             # 时间位, 默认:30
  workerBits: 16           # 机器位, 默认:16
  seqBits: 7               # 序列号, 默认:7
  epochStr: "2023-02-17"   # 初始时间, 默认:"2019-02-20"
  enableFutureTime: false # 允许使用未来时间生成ID,可以使用多少未来时间由 maxBackwardSeconds 控制， 默认: false
  maxBackwardSeconds: 1    # 系统时钟回拨和使用未来时间最长容忍时间（秒）, 默认:1
  CachedUidGenerator:     # CachedUidGenerator相关参数
    boostPower: 3          # RingBuffer size扩容参数, 可提高UID生成的吞吐量, 默认:3
    paddingFactor: 50      # 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
    #scheduleInterval: 60    # 默认:不配置此项, 即不使用Schedule线程定时填充buffer环. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
```

### 使用

#### 

```java
//实时生成
//@Resource
//private UidGenerator defaultUidGenerator;

//生成一次id之后，按序列号+1生成一批id，缓存，供之后请求 
@Resource
private UidGenerator cachedUidGenerator;



@Test
public void testSerialGenerate() {
    // Generate UID
    Mono<Long> uid = cachedUidGenerator.getUID();

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"450795408770","timestamp":"2019-02-20 14:55:39","workerId":"27","sequence":"2"}
    System.out.println(cachedUidGenerator.parseUID(uid));

}
```

#### 策略选择


* CachedUidGenerator:
  
  适合**持续高消耗量**的ID分发，也会保持证生成ID排序准确性，此方式会一定程度上的增加内存和CPU缓存占用。

* enableFutureTime 为true时的 DefaultUidGenerator:

  适合**偶然突发的消费增加后持续保持低消耗量**的ID分发，此方式在突发高消费时的性能要比CachedUidGenerator还要高，但不应该持续保持高消耗(可消耗的未来时间由maxBackwardSeconds控制，超出后抛出异常)，因为过度使用未来时间有可能会造成服务重启后生成重复ID和短时间内的ID排序不精确。

* enableFutureTime 为false时的 DefaultUidGenerator:
  
  适合**低消耗量实时**的ID分发，此方式可以保正ID中的时间精确，可满足高精确性的ID排序，一但消耗量高于当前时间的发号量时会返回一个等待响应，等待下一可用时间后发出ID（此方式在消费量大于当前时间可生成ID数量后性能最低），最大等待时间由maxBackwardSeconds控制，超出后抛出异常。

对于发号性能的测式可使用此 [测试用例](https://github.com/cooperlyt/uid-generator-spring-boot/tree/master/uid-generator-spring-boot-starter/src/test/java/io/github/cooperlyt/cloud/uid) 进行测试，并参见[百度UidGenerator](https://github.com/baidu/uid-generator)
