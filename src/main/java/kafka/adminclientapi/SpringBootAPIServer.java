package kafka.adminclientapi;

import java.util.*;

import kafka.adminclient.KafkaTopicManager; 
import kafka.adminclientapi.KafkaConfig;

import org.apache.kafka.clients.admin.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

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
        KafkaConfig.bootstrapAdminClient(bootstrapServers);
        return "Kafka Admin Client setup with bootstrap server: " + bootstrapServers;
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
        return KafkaTopicManager.DescribeTopic(KafkaConfig.getAdminClient(), topicName);
    }
}
