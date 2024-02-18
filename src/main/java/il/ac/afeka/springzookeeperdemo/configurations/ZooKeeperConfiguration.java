package il.ac.afeka.springzookeeperdemo.configurations;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ZooKeeperConfiguration {
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    private static final int SESSION_TIMEOUT_MS = 3000;

    @Bean(destroyMethod = "close")
    public ZooKeeper zooKeeper() throws IOException {
        return new ZooKeeper(ZOOKEEPER_CONNECTION_STRING, SESSION_TIMEOUT_MS, null);
    }
}
