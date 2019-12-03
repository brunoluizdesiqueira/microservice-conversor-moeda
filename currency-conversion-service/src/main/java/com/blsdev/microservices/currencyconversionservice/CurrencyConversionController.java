package com.blsdev.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CurrencyExchangeServiceProxy proxy;

	@GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrency(@PathVariable final String from, @PathVariable final String to,
			@PathVariable final BigDecimal quantity) {

		// Feign - Problem 1
		final Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);

		final ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversionBean.class,
				uriVariables);

		final CurrencyConversionBean response = responseEntity.getBody();

		return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(), quantity,
				quantity.multiply(response.getConversionMultiple()), response.getPort());
	}

	@GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
	@HystrixCommand(fallbackMethod = "fallbackConvertCurrencyFeign")
	public CurrencyConversionBean convertCurrencyFeign(@PathVariable final String from, @PathVariable final String to,
			@PathVariable final BigDecimal quantity) {

		final CurrencyConversionBean response = proxy.retrieveExchangeValue(from, to);

		logger.info("{}", response);

		return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(), quantity,
				quantity.multiply(response.getConversionMultiple()), response.getPort());
	}

	@GetMapping("/fallback")
	@HystrixCommand(fallbackMethod= "fallbackConvertCurrency")
	public CurrencyConversionBean fallback() {
		throw new RuntimeException("Não Implementado");
	}

	public CurrencyConversionBean fallbackConvertCurrency() {
		BigDecimal qtd = new BigDecimal(10);
		return extracted("USD", "INR", qtd);
	}

	public CurrencyConversionBean fallbackConvertCurrencyFeign(final String from, final String to,
			final BigDecimal quantity) {
		return extracted(from, to, quantity);
	}

	private CurrencyConversionBean extracted(final String from, final String to, final BigDecimal quantity) {
		
		logger.info("{}", "Serviço Convert Currency fora do ar!");

		final CurrencyConversionBean response = proxy.retrieveExchangeValue("USD", "INR");

		logger.info("{}", response);

		BigDecimal qtd = new BigDecimal(10);
		
		return new CurrencyConversionBean(
				response.getId(), 
				"USD", 
				"INR", 
				response.getConversionMultiple(), 
				qtd, 
				qtd.multiply(response.getConversionMultiple()), 
				response.getPort());
	}

}
