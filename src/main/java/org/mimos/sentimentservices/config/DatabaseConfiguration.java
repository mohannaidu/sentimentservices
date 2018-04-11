package org.mimos.sentimentservices.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;	

@Configuration
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);
    
    private final ApplicationProperties applicationProperties;

    @Autowired
    public DatabaseConfiguration(ApplicationProperties applicationProperties) {
    	this.applicationProperties = applicationProperties;
    }
    
    @Bean(destroyMethod = "close")
    public RestHighLevelClient getRestClient() {
    	return new RestHighLevelClient(
		        RestClient.builder(
		                new HttpHost(applicationProperties.getElasticdb_host(), applicationProperties.getElasticdb_port1(), "http")));
    }

}
