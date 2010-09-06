package org.apache.camel.component.spring.ws;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.ws.jaxb.QuoteRequest;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SpringWebserviceMarschallingConsumerIntegrationTest extends CamelTestSupport {
	
	@Test
	public void consumeStockQuoteWebservice() throws Exception {
		QuoteRequest request = new QuoteRequest(); 
		request.setSymbol("GOOG");
		
		Object result = template.requestBody("direct:webservice-marshall", request);

		assertNotNull(result);
		assertTrue(result instanceof String);
		String resultMessage = (String) result;
		assertTrue(resultMessage.contains("Google Inc."));
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				JaxbDataFormat jaxb = new JaxbDataFormat(false);
				jaxb.setContextPath("org.apache.camel.component.spring.ws.jaxb");
				
				from("direct:webservice-marshall").marshal(jaxb)
				.to("springws:http://127.0.0.1:8001/stockquote.asmx?soapAction=http://www.webserviceX.NET/GetQuote")
				.convertBodyTo(String.class);
				
				from("direct:webservice-marshall-unmarshall").marshal(jaxb)
				.to("springws:http://127.0.0.1:8001/stockquote.asmx?soapAction=http://www.webserviceX.NET/GetQuote")
				.convertBodyTo(String.class);
			}
		};
	}
}


//<GetQuoteResponse xmlns="http://www.webserviceX.NET/"><GetQuoteResult>&lt;StockQuotes&gt;&lt;Stock&gt;&lt;Symbol&gt;GOOG&lt;/Symbol&gt;&lt;Last&gt;470.30&lt;/Last&gt;&lt;Date&gt;9/3/2010&lt;/Date&gt;&lt;Time&gt;4:00pm&lt;/Time&gt;&lt;Change&gt;+7.12&lt;/Change&gt;&lt;Open&gt;470.41&lt;/Open&gt;&lt;High&gt;471.88&lt;/High&gt;&lt;Low&gt;467.44&lt;/Low&gt;&lt;Volume&gt;2545726&lt;/Volume&gt;&lt;MktCap&gt;149.9B&lt;/MktCap&gt;&lt;PreviousClose&gt;463.18&lt;/PreviousClose&gt;&lt;PercentageChange&gt;+1.54%&lt;/PercentageChange&gt;&lt;AnnRange&gt;433.63 - 629.51&lt;/AnnRange&gt;&lt;Earns&gt;23.025&lt;/Earns&gt;&lt;P-E&gt;20.12&lt;/P-E&gt;&lt;Name&gt;Google Inc.&lt;/Name&gt;&lt;/Stock&gt;&lt;/StockQuotes&gt;</GetQuoteResult></GetQuoteResponse>