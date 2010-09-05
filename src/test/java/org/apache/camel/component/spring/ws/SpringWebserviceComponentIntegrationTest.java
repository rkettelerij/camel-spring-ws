package org.apache.camel.component.spring.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.transform.Source;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.converter.jaxp.StringSource;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class SpringWebserviceComponentIntegrationTest extends AbstractJUnit4SpringContextTests {

	private static final String stockQuoteWebserviceUri = "http://www.webservicex.net/stockquote.asmx";

	private static final String xmlRequestForGoogleStockQuote 
		= "<GetQuote xmlns=\"http://www.webserviceX.NET/\"><symbol>GOOG</symbol></GetQuote>";
	
    @Produce
    protected ProducerTemplate template;
    
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceWithDefaultTemplate() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebserviceWithDefaultTemplate", xmlRequestForGoogleStockQuote);
		
		assertNotNull(result);
		assertTrue(result instanceof Source);
	}

	@Test(timeout=5000)
	public void consumeStockQuoteWebservice() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebservice", xmlRequestForGoogleStockQuote);
		
		assertNotNull(result);
		assertTrue(result instanceof Source);
	}
	
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceWithCamelStringSourceInput() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebservice", new StringSource(xmlRequestForGoogleStockQuote));
		
		assertNotNull(result);
		assertTrue(result instanceof Source);
	}
	
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceWithNonDefaultMessageFactory() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebserviceWithNonDefaultMessageFactory", xmlRequestForGoogleStockQuote);
		
		assertNotNull(result);
		assertTrue(result instanceof Source);
	}
	
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceAndConvertResult() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebserviceAsString", xmlRequestForGoogleStockQuote);
		
		assertNotNull(result);
		assertTrue(result instanceof String);
		String resultMessage = (String) result;
		assertTrue(resultMessage.contains("Google Inc."));
	}
	
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceAndProvideEndpointUriByHeader() throws Exception {
		Object result = template.requestBodyAndHeader("direct:stockQuoteWebserviceWithoutDefaultUri", xmlRequestForGoogleStockQuote, 
				SpringWebserviceConstants.SPRING_WS_ENDPOINT_URI, stockQuoteWebserviceUri);
		
		assertNotNull(result);
		assertTrue(result instanceof String);
		String resultMessage = (String) result;
		assertTrue(resultMessage.contains("Google Inc."));
	}
	
	@Test(timeout=5000)
	public void consumeStockQuoteWebserviceWithWsAddresing() throws Exception {
		Object result = template.requestBody("direct:stockQuoteWebserviceWithWsAddresing", xmlRequestForGoogleStockQuote);
		
		assertNotNull(result);
		//assertTrue(result instanceof Source);
	}
}
 