package kafka.adminclient;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.strimzi.api.kafka.Crds;

public class KafkaBrokerManager {
    public static JsonNode scaleBrokers(KubernetesClient kubernetesClient, String namespsace, String clustername, int newBrokerCount) {
        try {
            Crds.kafkaOperation(kubernetesClient).inNamespace(namespsace).withName(clustername).edit(
                    kafka -> {
                        kafka.getSpec().getKafka().setReplicas(newBrokerCount);
                        return kafka;
                    }
            );
            return null;
        } catch (Exception e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }
}
