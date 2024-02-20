package kafka.adminclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KafkaAdminClientUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode formatTopicDescriptions(Map<String, TopicDescription> topicDescriptions) {
        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode topicsArray = json.putArray("topics");

        for (Map.Entry<String, TopicDescription> entry : topicDescriptions.entrySet()) {
            String topicName = entry.getKey();
            TopicDescription topicDescription = entry.getValue();

            ObjectNode topicJson = topicsArray.addObject();
            topicJson.put("name", topicName);
            topicJson.put("internal", topicDescription.isInternal());

            ArrayNode partitionsArray = topicJson.putArray("partitions");
            for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
                ObjectNode partitionJson = partitionsArray.addObject();
                partitionJson.put("partition", partitionInfo.partition());
                partitionJson.put("leader", partitionInfo.leader().id());
                partitionJson.putPOJO("replicas", partitionInfo.replicas().stream().map(Node::id).collect(Collectors.toList()));
                partitionJson.putPOJO("isr", partitionInfo.isr().stream().map(Node::id).collect(Collectors.toList()));
            }
        }

        return json;
    }
}
