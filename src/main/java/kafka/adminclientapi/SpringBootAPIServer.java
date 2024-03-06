package kafka.adminclientapi;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;
import kafka.adminclient.KafkaPartitionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

import kafka.adminclient.KafkaTopicManager; 

@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
public class SpringBootAPIServer {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAPIServer.class, args);
    }
}

@RestController
class KafkaAdminClientSetupController {
    @PostMapping("/bootstrap")
    public String SetupKafkaAdminClient(@RequestBody JsonNode payload) {
        String bootstrapServers = payload.get("boostrapServers").asText();
        try {
            KafkaConfig.bootstrapAdminClient(bootstrapServers);
            return "AdminClient setup successfully!";
        } catch (Exception e) {
            return e.getMessage();
        }        
    }
}

@RestController
@RequestMapping("/cluster")
class ClusterController {
    @GetMapping("/describe")
    public JsonNode DescribeCluster() {
        return KafkaTopicManager.describeCluster(KafkaConfig.getAdminClient());
    }
}

@RestController
@RequestMapping("/partition")
class PartitionController {
    @PostMapping("/reassignAll")
    public ResponseEntity<JsonNode> ReassignAllPartitions(@RequestBody JsonNode payload) {
        int brokerId = payload.get("brokerId").asInt();
        String topicName = payload.get("topicName").asText();
        JsonNode res = KafkaPartitionManager.migrateAllPatitionsFromTopicToBroker(topicName, brokerId, KafkaConfig.getAdminClient());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("message", String.format("Successfully reassigned all partitions of topic %s to broker %d", topicName, brokerId));
        return res != null 
                ? new ResponseEntity<>(res, HttpStatus.BAD_REQUEST) 
                : new ResponseEntity<>(successNode, HttpStatus.OK);
    }

    @PostMapping("/reassign")
    public ResponseEntity<JsonNode> ReassignPartitions(@RequestBody JsonNode payload) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.convertValue(payload, new TypeReference<HashMap<String, Object>>(){});
        JsonNode res = KafkaPartitionManager.migratePartitions(map, KafkaConfig.getAdminClient());

        mapper = new ObjectMapper();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("message", "Successfully reassigned partitions");
        return res != null 
                ? new ResponseEntity<>(res, HttpStatus.BAD_REQUEST) 
                : new ResponseEntity<>(successNode, HttpStatus.OK);
    }
}

@RestController
@RequestMapping("/topic")
class TopicController {
    @GetMapping("/describeall")
    public JsonNode DescribeAllTopics() {
        return KafkaTopicManager.describeAllTopics(KafkaConfig.getAdminClient());
    }
    
    @GetMapping("/describe/{topicName}")
    public JsonNode DescribeTopic(@PathVariable("topicName") String topicName) {
        return KafkaTopicManager.describeTopic(KafkaConfig.getAdminClient(), topicName);
    }

    @PostMapping("/create")
    public ResponseEntity<JsonNode> CreateTopic(@RequestBody JsonNode payload) {
        String topicName = payload.get("topicName").asText();
        int numPartitions = payload.get("numPartitions").asInt();
        short replicationFactor = (short) payload.get("replicationFactor").asInt();
        JsonNode res = KafkaTopicManager.createTopic(topicName, numPartitions, replicationFactor, KafkaConfig.getAdminClient());
        return res != null 
                ? new ResponseEntity<>(res, HttpStatus.BAD_REQUEST) 
                : new ResponseEntity<>(DescribeTopic(topicName), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<JsonNode> DeleteTopics(@RequestBody JsonNode payload) {
        String topicNames = payload.get("topicNames").asText();
        List<String> topicNamesList = Arrays.asList(topicNames.split(","));
        topicNamesList = topicNamesList.stream()
                                 .map(String::strip)
                                 .collect(Collectors.toList()); 
        JsonNode res = KafkaTopicManager.deleteTopics(topicNamesList, KafkaConfig.getAdminClient());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("message", "Successfully deleted topics");
        ArrayNode deletedTopicsNode = successNode.putArray("deletedTopics");
        topicNamesList.forEach(deletedTopicsNode::add);

        return res != null 
                ? new ResponseEntity<>(res, HttpStatus.BAD_REQUEST) 
                : new ResponseEntity<>(successNode, HttpStatus.OK);
    }
}
