
uid-generator-spring-boot
==========================

UidGenerator是Java实现的, 基于[Snowflake](https://github.com/twitter/snowflake)算法的唯一ID生成器。

基与 [Snowflake](https://github.com/twitter/snowflake)，[百度UidGenerator](https://github.com/baidu/uid-generator)，[uid-generator-spring-boot-starter](https://github.com/wujun234/uid-generator-spring-boot-starter)


* 更改了打包结构，抽出了Worker node id的生成接口， 使其可以自定义实现分配 Worker node id的方法， 本项目中实现了两种数据库存储的方法：
* 支持 Spring boot Autoconfigure 
* 支持 jdbc 和 r2dbc 两种数据库连接方式

* mybatis jdbc 实现 [uid-worker-id-jdbc-spring-boot-starter](https://github.com/cooperlyt/uid-generator-spring-boot/tree/master/uid-worker-id-jdbc-spring-boot-starter)
* mybatis r2dbc 实现 [uid-worker-id-r2dbc-spring-boot-start](https://github.com/cooperlyt/uid-generator-spring-boot/tree/master/uid-worker-id-r2dbc-spring-boot-starter)

你以可以根据[接口包](https://github.com/cooperlyt/uid-generator-spring-boot/tree/master/uid-generator-api)自己实现Worker node分配的实现

## 原理和性能

请参见[Snowflake](https://github.com/twitter/snowflake)和[百度UidGenerator](https://github.com/baidu/uid-generator)

## 使用

### Maven 

#### spring boot autoconfig 方式
```xml
<dependency>
    <groupId>uid-generator-spring-boot-starter</groupId>
    <artifactId>uid</artifactId>
    <version>1.0.3.RELEASE</version>
</dependency>

<!-- 选择一种 worker node 分配方式 -->

<!-- jdbc -->

<dependency>
    <groupId>cooperlyt.github.io</groupId>
    <artifactId>uid-worker-id-jdbc-spring-boot-starter</artifactId>
    <version>1.0.1.BATE</version>
</dependency>

        <!-- 选择你的数据库jdbc驱动 -->
<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
</dependency>

<!-- r2dbc -->

<dependency>
    <groupId>io.github.cooperlyt</groupId>
    <artifactId>uid-worker-id-r2dbc-spring-boot-starter</artifactId>
    <version>1.0.1.BATE</version>
</dependency>

        <!-- 选择你的数据库据r2dbc驱动 -->
<dependency>
    <groupId>org.mariadb</groupId>
    <artifactId>r2dbc-mariadb</artifactId>
    <version>1.1.3</version>
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

### spring boot 配置

#### ID 生成 配置
```yml
uid:
  timeBits: 30             # 时间位, 默认:30
  workerBits: 16           # 机器位, 默认:16
  seqBits: 7               # 序列号, 默认:7
  epochStr: "2019-02-20"   # 初始时间, 默认:"2019-02-20"
  enableBackward: true    # 是否容忍时钟回拨, 默认:true
  maxBackwardSeconds: 1    # 时钟回拨最长容忍时间（秒）, 默认:1
  CachedUidGenerator:     # CachedUidGenerator相关参数
    boostPower: 3          # RingBuffer size扩容参数, 可提高UID生成的吞吐量, 默认:3
    paddingFactor: 50      # 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
    #scheduleInterval: 60    # 默认:不配置此项, 即不使用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
```

#### jdbc 配置

```yml
mybatis:
  configuration:
    default-fetch-size: 100
    default-statement-timeout: 30
    map-underscore-to-camel-case: true
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://127.0.0.1:3306/database?
    username: root
    password: ****
```

#### r2dbc 配置

```yml

r2dbc:
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    map-underscore-to-camel-case: true
spring:
  r2dbc:
    mybatis:
      r2dbc-url: r2dbc:mysql://127.0.0.1:3306/database
      username: root
      password: ****
      pool:
        max-idle-time: PT3M
        validation-query: SELECT 1 FROM DUAL
        initial-size: 1
        max-size: 3
        acquire-retry: 3
        validation-depth: REMOTE
        max-create-connection-time: PT30S

```

#### 自定义 拒绝策略 

```java
  @Bean
  RejectedPutBufferHandler customPutHandler() {
      return (r, i) -> {
      do your
      };
  }

  @Bean
  RejectedTakeBufferHandler customTakeHandler() {
      return (r, i) -> {
      do your
      };
  }
  
```

### 使用


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
    long uid = cachedUidGenerator.getUID();

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"450795408770","timestamp":"2019-02-20 14:55:39","workerId":"27","sequence":"2"}
    System.out.println(cachedUidGenerator.parseUID(uid));

}
```
**defaultUidGenerator** 和 **cachedUidGenerator** 的区别和选择方式请参见 [百度UidGenerator](https://github.com/baidu/uid-generator)