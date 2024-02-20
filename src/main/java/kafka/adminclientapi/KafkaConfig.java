package kafka.adminclientapi;

import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

@Configuration
public class KafkaConfig {
    private static AdminClient adminClient;

    public static AdminClient getAdminClient() {
        return adminClient;
    }

    public static void bootstrapAdminClient(String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        AdminClient adminClient = AdminClient.create(props);
        KafkaConfig.adminClient = adminClient;
    }
}
