package cc.coopersoft.cloud.uid.worker.jdbc;

import cc.coopersoft.cloud.uid.worker.jdbc.UidJDBCWorkerConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * UID 测试主类
 *
 * @author wujun
 * @date 2019.02.20 11:01
 */
@SpringBootApplication
public class UidTestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(UidJDBCWorkerConfigure.class, args);
	}
}
