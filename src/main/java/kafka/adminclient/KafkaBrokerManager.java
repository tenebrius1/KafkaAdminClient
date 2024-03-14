package kafka.adminclient;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.strimzi.api.kafka.Crds;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePool;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePoolBuilder;

public class KafkaBrokerManager {

    public static JsonNode scaleBrokers(KubernetesClient kubernetesClient, String namespace, String clusterName, int newBrokerCount) {
        try {
            KafkaNodePool existingNodePool = Crds.kafkaNodePoolOperation(kubernetesClient)
                    .inNamespace(namespace)
                    .withName("broker")
                    .get();

            KafkaNodePool updatedNodePool = new KafkaNodePoolBuilder(existingNodePool)
                    .editSpec()
                    .withReplicas(newBrokerCount)
                    .endSpec()
                    .build();

            Crds.kafkaNodePoolOperation(kubernetesClient)
                    .inNamespace(namespace)
                    .withName("broker")
                    .patch(updatedNodePool);

            return null;
        } catch (Exception e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }
}

