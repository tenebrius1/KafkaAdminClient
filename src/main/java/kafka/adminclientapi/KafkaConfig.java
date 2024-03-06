package kafka.adminclientapi;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    private static AdminClient adminClient;

    public static AdminClient getAdminClient() {
        return adminClient;
    }

    public static void bootstrapAdminClient(String bootstrapServers) throws Exception {
        try {
            Properties props = new Properties();
            System.out.println("Bootstrap servers: " + bootstrapServers);
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            AdminClient adminClient = AdminClient.create(props);

            // Check if broker is online
            adminClient.listTopics().names().get(1, TimeUnit.SECONDS); 
            KafkaConfig.adminClient = adminClient;
        } catch (TimeoutException e) {
            throw new RuntimeException("Connection timed out: Kafka broker might be offline.");
        } catch (Exception e) {
            throw new Exception("Failed to create AdminClient: " + e.getMessage());
        }
    }
}
