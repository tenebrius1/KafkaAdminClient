package kafka.adminclientapi;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    private static AdminClient adminClient;
    private static KubernetesClient kubernetesClient;
    public static String namespace;
    public static String clusterName;

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
            adminClient.listTopics().names().get(2, TimeUnit.SECONDS);
            KafkaConfig.adminClient = adminClient;
        } catch (TimeoutException e) {
            throw new RuntimeException("Connection timed out: Kafka broker might be offline.");
        } catch (Exception e) {
            throw new Exception("Failed to create AdminClient: " + e.getMessage());
        }
    }

    public static void bootstrapKubernetesClient(JsonNode namespace, JsonNode clusterName) {
        try {
            KubernetesClient client = new KubernetesClientBuilder().build();
            String namespaceToUse = namespace == null ? "kafka" : namespace.asText();
            String clusterNameToUse = clusterName == null ? "my-cluster" : clusterName.asText();
            KafkaConfig.namespace = namespaceToUse;
            KafkaConfig.clusterName = clusterNameToUse;
            KafkaConfig.kubernetesClient = client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Kubernetes client: " + e.getMessage());
        }
    }
}
