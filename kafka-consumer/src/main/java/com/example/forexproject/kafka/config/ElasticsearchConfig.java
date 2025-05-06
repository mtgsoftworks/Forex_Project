package com.example.forexproject.kafka.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "opensearch")
public class ElasticsearchConfig {

    private String host = "localhost";
    private int port = 9200;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient esClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
    }
}
