package com.blsdev.microservices.limitsservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("limits-service")
public class Configuration {
	private int minimum;
	private int maximum;
}
