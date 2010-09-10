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

import java.io.StringReader;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class SpringWebserviceComponentProducerIntegrationTest extends CamelSpringTestSupport {

	private static final String xmlRequestForGoogleStockQuote = "<GetQuote xmlns=\"http://www.webserviceX.NET/\"><symbol>GOOG</symbol></GetQuote>";
	private static final String xmlRequestForGoogleStockQuoteNoNamespace = "<GetQuote><symbol>GOOG</symbol></GetQuote>";
	private static final String xmlRequestForGoogleStockQuoteNoNamespaceDifferentBody = "<GetQuote><symbol>GRABME</symbol></GetQuote>";
	
	@EndpointInject(uri = "mock:testRootQName")
    private MockEndpoint resultEndpointRootQName;
	
	@EndpointInject(uri = "mock:testSoapAction")
    private MockEndpoint resultEndpointSoapAction;
	
	@EndpointInject(uri = "mock:testUri")
    private MockEndpoint resultEndpointUri;
	
	@EndpointInject(uri = "mock:testXPath")
    private MockEndpoint resultEndpointXPath;

	private final WebServiceTemplate webServiceTemplate;
	
	public SpringWebserviceComponentProducerIntegrationTest() {
		webServiceTemplate = new WebServiceTemplate();
		webServiceTemplate.setDefaultUri("http://127.0.0.1:8080/stockquote");
	}

	@Test
	public void testRootQName() throws Exception {
		StreamSource source = new StreamSource(new StringReader(xmlRequestForGoogleStockQuote));
		StreamResult result = new StreamResult(System.out);
		webServiceTemplate.sendSourceAndReceiveToResult(source, result);
		resultEndpointRootQName.expectedMinimumMessageCount(1);
		resultEndpointRootQName.assertIsSatisfied();
	}
	
	@Test
	public void testSoapAction() throws Exception {
		StreamSource source = new StreamSource(new StringReader(xmlRequestForGoogleStockQuoteNoNamespace));
		StreamResult result = new StreamResult(System.out);
		webServiceTemplate.sendSourceAndReceiveToResult(source, new SoapActionCallback("http://www.webserviceX.NET/GetQuote"), result);
		resultEndpointSoapAction.expectedMinimumMessageCount(1);
		resultEndpointSoapAction.assertIsSatisfied();
	}
	
	@Test
	public void testUri() throws Exception {
		StreamSource source = new StreamSource(new StringReader(xmlRequestForGoogleStockQuoteNoNamespace));
		StreamResult result = new StreamResult(System.out);
		webServiceTemplate.sendSourceAndReceiveToResult("http://localhost:8080/stockquote2", source, result);
		resultEndpointUri.expectedMinimumMessageCount(1);
		resultEndpointUri.assertIsSatisfied();
	}
	
	@Test
	public void testXPath() throws Exception {
		StreamSource source = new StreamSource(new StringReader(xmlRequestForGoogleStockQuoteNoNamespaceDifferentBody));
		StreamResult result = new StreamResult(System.out);
		webServiceTemplate.sendSourceAndReceiveToResult(source, result);
		resultEndpointXPath.expectedMinimumMessageCount(1);
		resultEndpointXPath.assertIsSatisfied();
	}

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("org/apache/camel/component/spring/ws/SpringWebserviceComponentProducerIntegrationTest-context.xml");
	}
}