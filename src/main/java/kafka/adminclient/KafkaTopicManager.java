package kafka.adminclient;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kafka.adminclient.KafkaAdminClientUtils;

public class KafkaTopicManager {
    public static JsonNode describeTopic(AdminClient adminClient, String topicName) {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topicName));
            TopicDescription topicDescription = result.topicNameValues().get(topicName).get();

            return KafkaAdminClientUtils.formatTopicDescription(topicDescription, topicName);
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }

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

    public static void migrateAllPatitionsFromTopic(String topicName, int brokerId, AdminClient adminClient) throws ExecutionException, InterruptedException {
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
        System.out.println("Reassignment completed for topic: " + topicName);
    }
    
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
