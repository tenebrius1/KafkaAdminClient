package kafka.adminclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaPartitionManager {
    /**
     * Migrates partitions of Kafka topics based on the provided map.
     *
     * @param map         the map containing the reassignment details
     * @param adminClient the Kafka AdminClient instance
     * @return null if the partitions were successfully migrated, or a JsonNode object containing the error message.
     */
    public static JsonNode migratePartitions(HashMap<String, Object> map, AdminClient adminClient) {
        // sample hashmap "partitions":[{"topic":"quickstart-events","partition":0,"replicas":[1]}]}
        try {
            for (Map<String, Object> partition : (List<Map<String, Object>>) map.get("partitions")) {
                String topicName = (String) partition.get("topic");
                int partitionNumber = (int) partition.get("partition");
                List<Integer> replicas = (List<Integer>) partition.get("replicas");

                // Create a reassignment map for the partition
                Map<TopicPartition, Optional<NewPartitionReassignment>> reassignmentMap = new HashMap<>();
                reassignmentMap.put(
                    new TopicPartition(topicName, partitionNumber),
                    Optional.of(new NewPartitionReassignment(replicas))
                );

                // Execute the reassignment
                AlterPartitionReassignmentsResult result = adminClient.alterPartitionReassignments(reassignmentMap);
                result.all().get(); // Wait for the reassignment to complete
            }
            return null;
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }

    /**
     * Migrates all partitions from a topic to a new broker and returns the result as a JsonNode.
     *
     * @param topicName   the name of the topic to migrate partitions from
     * @param brokerId    the ID of the new broker to migrate partitions to
     * @param adminClient the AdminClient instance used to perform the migration
     * @return null if the partitions were successfully migrated, or a JsonNode object containing the error message.
     */
    public static JsonNode migrateAllPatitionsFromTopicToBroker(String topicName, int brokerId, AdminClient adminClient) {
        try {
            // Fetch the current topic partition information
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
            Map<String, TopicDescription> topicDescriptions = describeTopicsResult.allTopicNames().get();
            TopicDescription topicDescription = topicDescriptions.get(topicName);

            // Create a reassignment map for all partitions to the new broker
            Map<TopicPartition, Optional<NewPartitionReassignment>> reassignmentMap = new HashMap<>();
            for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
                reassignmentMap.put(
                    new TopicPartition(topicName, partitionInfo.partition()),
                    Optional.of(new NewPartitionReassignment(Collections.singletonList(brokerId)))
                );
            }

            // Execute the reassignment
            AlterPartitionReassignmentsResult result = adminClient.alterPartitionReassignments(reassignmentMap);
            result.all().get(); // Wait for the reassignment to complete
            return null;
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }
}