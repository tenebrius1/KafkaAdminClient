package kafka.adminclient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterPartitionReassignmentsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.NewPartitionReassignment;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import kafka.adminclient.KafkaAdminClientUtils;

public class KafkaTopicManager {
    /**
     * Retrieves the description of a Kafka topic as a JSON node.
     * Information returned includes the topic name, partitions, leader, replicas, and ISRs.
     *
     * @param adminClient the Kafka AdminClient instance
     * @param topicName   the name of the topic to describe
     * @return the description of the topic as a JSON node
     */
    public static JsonNode describeTopic(AdminClient adminClient, String topicName) {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topicName));
            TopicDescription topicDescription = result.topicNameValues().get(topicName).get();

            return KafkaAdminClientUtils.formatTopicDescription(topicDescription, topicName);
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }

    /**
     * Retrieves the metadata of all topics in the Kafka cluster and returns it as a JSON node.
     * Information returned for each topic is the same as that returned by the describeTopic method.
     *
     * @param adminClient the Kafka AdminClient instance.
     * @return a JSON node containing the metadata of all topics.
     */
    public static JsonNode describeAllTopics(AdminClient adminClient) {
        try {
            // List topics
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Collection<TopicListing> topics = listTopicsResult.listings().get();

            // Describe topics to get metadata
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics.stream().map(TopicListing::name).toList());
            Map<String, TopicDescription> topicDescriptions = describeTopicsResult.allTopicNames().get(); 
            
            return KafkaAdminClientUtils.formatTopicDescriptions(topicDescriptions);
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }

    /**
     * Migrates all partitions from a topic to a new broker and returns the result as a JsonNode.
     * Run this script before decommissioning a broker to ensure that all partitions are migrated to other brokers.
     * Note that this may cause partitions to have a lower replica count than the replication factor.
     *
     * @param topicName   the name of the topic to migrate partitions from
     * @param brokerId    the ID of the new broker to migrate partitions to
     * @param adminClient the AdminClient instance used to perform the migration
     * @return a JsonNode representing the result of the migration
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
    
    /**
     * Creates a new topic with the specified name, number of partitions, and replication factor.
     * 
     * @param topicName         the name of the topic to create.
     * @param partitions        the number of partitions for the topic.
     * @param replicationFactor the replication factor for the topic.
     * @param adminClient       the AdminClient instance.
     * @return null if the topic was successfully created, or a JsonNode object containing the error message.
     */
    public static JsonNode createTopic(String topicName, 
                        int partitions, short replicationFactor, AdminClient adminClient) {
        try {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            return null;
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }
    
    /**
     * Deletes the specified topics from the Kafka cluster.
     * 
     * @param topicsToDelete the list of topics to delete
     * @param adminClient    the Kafka AdminClient instance
     * @return null if the topics were successfully deleted, or a JsonNode object containing the error message.
     */
    public static JsonNode deleteTopics(List<String> topicsToDelete, AdminClient adminClient) {
        try {
            DeleteTopicsOptions options = new DeleteTopicsOptions();
            DeleteTopicsResult result = adminClient.deleteTopics(topicsToDelete, options);
            result.all().get();
            return null;
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }
}
