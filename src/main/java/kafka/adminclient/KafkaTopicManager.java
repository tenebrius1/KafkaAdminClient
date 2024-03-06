package kafka.adminclient;

import java.lang.InterruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;

public class KafkaTopicManager {
    /**
     * Retrieves the cluster description as a JSON node.
     * Information included are the brokers and controllers running
     * along with their ID and ports.
     *
     * @param adminClient the Kafka admin client
     * @return the cluster description as a JSON node
     */
    public static JsonNode describeCluster(AdminClient adminClient) {
        try {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            return KafkaAdminClientUtils.formatClusterDescription(clusterResult);
        } catch (ExecutionException | InterruptedException e) {
            return KafkaAdminClientUtils.wrapError(e);
        }
    }

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
