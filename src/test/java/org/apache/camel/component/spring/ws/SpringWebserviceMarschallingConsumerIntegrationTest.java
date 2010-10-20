/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
				.to("springws:http://www.webservicex.net/stockquote.asmx?soapAction=http://www.webserviceX.NET/GetQuote")
				.convertBodyTo(String.class);
				
				from("direct:webservice-marshall-unmarshall").marshal(jaxb)
				.to("springws:http://127.0.0.1:8001/stockquote.asmx?soapAction=http://www.webserviceX.NET/GetQuote")
				.unmarshal(jaxb);
			}
		};
	}
}