package com.blsdev.microservices.currencyexchangeservice;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrencyExchangeController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private ExchangeValueRepository repository;

	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	@HystrixCommand(fallbackMethod="fallbackRetieveExchangeValue2")
	public ExchangeValue retrieveExchangeValue(@PathVariable String from, @PathVariable String to) {
		
		ExchangeValue exchangeValue = repository.findByFromAndTo(from, to);

		exchangeValue.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
		
		logger.info("{}", exchangeValue);
		
		return exchangeValue;                                                                                         	
	}	

	@GetMapping("/fallback")
	@HystrixCommand(fallbackMethod="fallbackRetieveExchangeValue")
	public ExchangeValue retrieveValue() {
		throw new RuntimeException("Nao implementado");
	}

	public ExchangeValue fallbackRetieveExchangeValue() {

		ExchangeValue exchangeValue = repository.findByFromAndTo("USD", "INR");

		exchangeValue.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
		
		logger.info("{}", exchangeValue);
		
		return exchangeValue;              
	}

	public ExchangeValue fallbackRetieveExchangeValue2(String from, String to) {

		ExchangeValue exchangeValue = repository.findByFromAndTo("USD", "INR");

		exchangeValue.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
		
		logger.info("{}", exchangeValue);
		
		return exchangeValue;              
	}
}
