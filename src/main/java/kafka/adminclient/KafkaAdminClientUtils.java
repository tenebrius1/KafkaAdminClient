package kafka.adminclient;

import java.lang.Exception;
import java.lang.InterruptedException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.clients.admin.DescribeClusterResult;

public class KafkaAdminClientUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode formatClusterDescription(DescribeClusterResult clusterResult) throws ExecutionException, InterruptedException {
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Cluster ID
        rootNode.put("clusterId", clusterResult.clusterId().get());

        // Node Information (Broker Details)
        ArrayNode brokersNode = rootNode.putArray("brokers");
        for (Node node : clusterResult.nodes().get()) {
            ObjectNode brokerNode = objectMapper.createObjectNode();
            brokerNode.put("brokerId", node.id());
            brokerNode.put("host", node.host());
            brokerNode.put("port", node.port());
            brokersNode.add(brokerNode);
        }

        Node controller = clusterResult.controller().get();
        rootNode.putObject("controller")
                .put("id", controller.id())
                .put("host", controller.host())
                .put("port", controller.port());
        return rootNode;
    }

    public static ObjectNode formatTopicDescription(TopicDescription topicDescription, String topicName) {
        ObjectNode topicJson = objectMapper.createObjectNode();
        topicJson.put("name", topicName);

        ArrayNode partitionsArray = topicJson.putArray("partitions");
        for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
            ObjectNode partitionJson = partitionsArray.addObject();
            partitionJson.put("partition", partitionInfo.partition());
            partitionJson.put("leader", partitionInfo.leader().id());
            partitionJson.putPOJO("replicas", partitionInfo.replicas().stream().map(Node::id).collect(Collectors.toList()));
            partitionJson.putPOJO("isr", partitionInfo.isr().stream().map(Node::id).collect(Collectors.toList()));
        }

        return topicJson;
    }

    public static JsonNode formatTopicDescriptions(Map<String, TopicDescription> topicDescriptions) {
        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode topicsArray = json.putArray("topics");

        for (Map.Entry<String, TopicDescription> entry : topicDescriptions.entrySet()) {
            String topicName = entry.getKey();
            TopicDescription topicDescription = entry.getValue();
            
            ObjectNode topicJson = formatTopicDescription(topicDescription, topicName);
            topicsArray.add(topicJson);
        }

        return json;
    }

    public static JsonNode wrapError(Exception e) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", e.getMessage());
        return errorJson;
    }
}
