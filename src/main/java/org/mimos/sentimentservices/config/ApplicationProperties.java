package org.mimos.sentimentservices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Sentimentservices.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
	
	private String elasticdb_host;
	private Integer elasticdb_port1;
	private Integer elasticdb_port2;
	
	public String getElasticdb_host() {
		return elasticdb_host;
	}
	public void setElasticdb_host(String elasticdb_host) {
		this.elasticdb_host = elasticdb_host;
	}
	public Integer getElasticdb_port1() {
		return elasticdb_port1;
	}
	public void setElasticdb_port1(Integer elasticdb_port1) {
		this.elasticdb_port1 = elasticdb_port1;
	}
	public Integer getElasticdb_port2() {
		return elasticdb_port2;
	}
	public void setElasticdb_port2(Integer elasticdb_port2) {
		this.elasticdb_port2 = elasticdb_port2;
	}

	
}
