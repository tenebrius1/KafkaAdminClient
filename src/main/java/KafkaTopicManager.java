import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KafkaTopicManager {
    public static void migrateAllPatitionsFromTopic(String topicName, int brokerId, AdminClient adminClient) {
        // Fetch the current topic partition information
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
        Map<String, TopicDescription> topicDescriptions = describeTopicsResult.all().get();
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
        System.out.println("Reassignment completed for topic: " + topicName);
    }

    public static void describeAllTopics(AdminClient adminClient) {
        // List topics
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Collection<TopicListing> topics = listTopicsResult.listings().get();

        // Describe topics to get metadata
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics.stream().map(TopicListing::name).toList());
        Map<String, TopicDescription> topicDescriptions = describeTopicsResult.all().get();

        // Print topic names and metadata
        for (TopicListing topic : topics) {
            System.out.println("Topic: " + topic.name());
            TopicDescription description = topicDescriptions.get(topic.name());
            System.out.println("Description: " + description);
        }
    }

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092"; // Replace with your Kafka cluster bootstrap servers

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(props)) {
            // Example call to migrateAllPatitions
            // KafkaTopicManager.migrateAllPatitionsFromTopic("quickstart-events", 1, adminClient);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
