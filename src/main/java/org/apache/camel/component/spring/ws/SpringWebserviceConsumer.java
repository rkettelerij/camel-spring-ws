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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

public class SpringWebserviceConsumer extends DefaultConsumer implements MessageEndpoint {
	
	private SpringWebserviceEndpoint endpoint;
	private SpringWebserviceConfiguration configuration;

	public SpringWebserviceConsumer(Endpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = (SpringWebserviceEndpoint) endpoint;
		this.configuration = this.endpoint.getConfiguration();
	}

	/**
	 * Invoked by Spring-WS when a {@link WebServiceMessage} is received
	 */
	public void invoke(MessageContext messageContext) throws Exception {
		// convert WebserviceMessage properties (added through interceptors) to Camel exchange properties
		Exchange exchange = new DefaultExchange(endpoint.getCamelContext(), ExchangePattern.InOptionalOut);
		String[] propertyNames = messageContext.getPropertyNames();
		if (propertyNames != null) {
			for (String propertyName : propertyNames) {
				exchange.setProperty(propertyName, messageContext.getProperty(propertyName));
			}
		}
		
		// convert SOAP headers to Camel exchange headers
		WebServiceMessage request = messageContext.getRequest();
		Map<String, Object> headers = new HashMap<String, Object>();
		if (request instanceof SoapMessage) {
			SoapMessage soapMessage = (SoapMessage) request;
			SoapHeader soapHeader = soapMessage.getSoapHeader();
			if (soapHeader != null) {
				Iterator<?> attributeIter = soapHeader.getAllAttributes();
				while (attributeIter.hasNext()) {
					QName name = (QName) attributeIter.next();
					headers.put(name.toString(), soapHeader.getAttributeValue(name));
				}
				Iterator<?> elementIter = soapHeader.examineAllHeaderElements();
				while (elementIter.hasNext()) {
					Object element = elementIter.next();
					if (element instanceof SoapHeaderElement) {
						QName name = ((SoapHeaderElement) element).getName();
						headers.put(name.toString(), element);
					}
				}
			}
		}

		// create inbound message
		SpringWebserviceMessage inMessage = new SpringWebserviceMessage(request);
		inMessage.setHeaders(headers);
		exchange.setIn(inMessage);
		
		// start message processing
		getProcessor().process(exchange);
		
		// create webservice response from output body
		if (exchange.getPattern().isOutCapable()) {
			Source responseSource = exchange.getOut(Source.class);
			if (responseSource != null) {
				WebServiceMessage response = messageContext.getResponse();
				TransformerSupportDelegate transformerSupportDelegate = new TransformerSupportDelegate();
				transformerSupportDelegate.transformSourceToResult(responseSource, response.getPayloadResult());
			}
		}
	}
	
	/**
	 * Transform webservice response {@link Source} object to {@link Result}
	 */
	private class TransformerSupportDelegate extends TransformerObjectSupport {
		void transformSourceToResult(Source source, Result result) throws TransformerException {
			this.transform(source, result);
		}
	}

	@Override
	protected void doStop() throws Exception {
		configuration.getEndpointMapping().removeConsumer(endpoint.getEndpointKey());
		super.doStop();
	}

	@Override
	protected void doStart() throws Exception {
		configuration.getEndpointMapping().addConsumer(endpoint.getEndpointKey(), configuration.getEndpointMappingType(), this);
		super.doStart();
	}

}
