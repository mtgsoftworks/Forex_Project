package com.example.forexproject.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens Kafka messages and indexes them into OpenSearch / Elasticsearch.
 */
@Service
public class ElasticConsumerService {

    private static final Logger logger = LogManager.getLogger(ElasticConsumerService.class);

    private static final String INDEX_NAME = "forex_rates";

    @Autowired
    private RestHighLevelClient esClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "forex_topic", groupId = "forex_elastic_group")
    public void listenAndIndex(String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length == 4) {
                Map<String, Object> json = new HashMap<>();
                json.put("rateName", parts[0]);
                json.put("bid", Double.parseDouble(parts[1]));
                json.put("ask", Double.parseDouble(parts[2]));
                json.put("timestamp", parts[3]);

                IndexRequest req = new IndexRequest(INDEX_NAME).source(json);
                IndexResponse resp = esClient.index(req, RequestOptions.DEFAULT);
                logger.debug("Document indexed with id {}", resp.getId());
            } else {
                logger.warn("Unexpected Kafka message format: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error indexing message to Elastic: {}", e.getMessage(), e);
        }
    }
}
