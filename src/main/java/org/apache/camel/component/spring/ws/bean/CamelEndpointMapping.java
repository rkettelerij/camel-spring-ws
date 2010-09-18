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
package org.apache.camel.component.spring.ws.bean;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.spring.ws.type.EndpointMappingKey;
import org.apache.camel.component.spring.ws.type.EndpointMappingType;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.xml.xpath.XPathExpression;
import org.w3c.dom.Element;

/**
 * Spring {@link EndpointMapping} for mapping messages to corresponding Camel endpoints. 
 * This class needs to be registered in the Spring <tt>ApplicationContext</tt> when 
 * consuming messages using any of the following URI schemes:
 * 
 * <ul>
 * <li><tt>springws:rootqname:</tt><br/>
 * Equivalent to endpoint mappings specified through {@link org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping}
 * 
 * <li><tt>springws:soapaction:</tt><br/>
 * Equivalent to endpoint mappings specified through {@link org.springframework.ws.soap.server.endpoint.mapping.SoapActionEndpointMapping}
 *  
 * <li><tt>springws:uri:</tt><br/>
 * Equivalent to endpoint mappings specified through {@link org.springframework.ws.server.endpoint.mapping.UriEndpointMapping}
 *  
 * <li><tt>springws:xpathresult:</tt><br/>
 * Equivalent to endpoint mappings specified through {@link org.springframework.ws.server.endpoint.mapping.XPathPayloadEndpointMapping}
 * </ul>
 * 
 * @see org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping
 * @see org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping
 * @see org.springframework.ws.server.endpoint.mapping.UriEndpointMapping
 * @see org.springframework.ws.server.endpoint.mapping.XPathPayloadEndpointMapping
 * @see org.springframework.ws.soap.server.endpoint.mapping.SoapActionEndpointMapping
 * 
 * @author Richard Kettelerij
 * 
 */
public class CamelEndpointMapping extends AbstractEndpointMapping {
	
	private static final String DOUBLE_QUOTE = "\"";
	private Map<EndpointMappingKey, MessageEndpoint> endpoints = new ConcurrentHashMap<EndpointMappingKey, MessageEndpoint>();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

	@Override
	protected Object getEndpointInternal(MessageContext messageContext) throws Exception {
		for (EndpointMappingKey key : endpoints.keySet()) {
			Object messageKey = null;
			switch (key.getType()) {
				case ROOT_QNAME : 
					messageKey = getRootQName(messageContext); 
					break;
				case SOAP_ACTION : 
					messageKey = getSoapAction(messageContext); 
					break;
				case XPATHRESULT :
					messageKey = getXPathResult(messageContext, key.getExpression());
					break;
				case URI : 
					messageKey = getUri(); 
					break;
				default : 
					throw new RuntimeCamelException("Invalid mapping type specified. Supported types are: root QName, SOAP action, XPath expression and URI");
			}
			if (messageKey != null && key.getLookupKey().equals(messageKey)) {
				return endpoints.get(key);
			}
		}        
		return null;
	}
	
    @Override
    protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext, Object endpoint, EndpointInterceptor[] interceptors) {
    	for (EndpointMappingKey key : endpoints.keySet()) {
    		if (EndpointMappingType.SOAP_ACTION.equals(key.getType())) {
    			Object messageKey = getSoapAction(messageContext);
    			if (messageKey != null && key.getLookupKey().equals(messageKey)) {
    				return new SoapEndpointInvocationChain(endpoint, interceptors);
    				// possibly add support for SOAP actors/roles and ultimate receiver in the future
    			}
    		}
		}
        return super.createEndpointInvocationChain(messageContext, endpoint, interceptors);
    }

    /**
     * Roughly equivalent to {@link org.springframework.ws.soap.server.endpoint.mapping.SoapActionEndpointMapping#getEndpoint(MessageContext)}
     */
	private String getSoapAction(MessageContext messageContext) {
		if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage request = (SoapMessage) messageContext.getRequest();
            String soapAction = request.getSoapAction();
            if (StringUtils.hasLength(soapAction) && soapAction.startsWith(DOUBLE_QUOTE) && soapAction.endsWith(DOUBLE_QUOTE)) {
                return soapAction.substring(1, soapAction.length() - 1);
            }
            return soapAction;
        }
        return null;
	}

    /**
     * Roughly equivalent to {@link org.springframework.ws.server.endpoint.mapping.UriEndpointMapping#getEndpoint(MessageContext)}
     */
	private String getUri() throws URISyntaxException {
		TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext != null) {
            WebServiceConnection webServiceConnection = transportContext.getConnection();
            if (webServiceConnection != null) {
                return webServiceConnection.getUri().toString();
            }
        }
        return null;
	}

    /**
     * Roughly equivalent to {@link org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping#getEndpoint(MessageContext)}
     */
	private String getRootQName(MessageContext messageContext) throws TransformerException, XMLStreamException {
		QName qName = PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
		return qName != null ? qName.toString() : null;
	}

    /**
     * Roughly equivalent to {@link org.springframework.ws.server.endpoint.mapping.XPathPayloadEndpointMapping#getEndpoint(MessageContext)}
     */
	private String getXPathResult(MessageContext messageContext, XPathExpression expression) throws TransformerException, XMLStreamException {
		if (expression != null) {
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMResult domResult = new DOMResult();
	        transformer.transform(messageContext.getRequest().getPayloadSource(), domResult);
	        Element elm = (Element) domResult.getNode().getFirstChild();
	        return expression.evaluateAsString(elm); 
		}
		return null;
	}
	
	/**
	 * Used by Camel Spring Web Services endpoint to register consumers
	 * @param key unique consumer key
	 * @param endpoint consumer
	 */
	public void addConsumer(EndpointMappingKey key, MessageEndpoint endpoint) {
		endpoints.put(key, endpoint);
	}
	
	/**
	 * Used by Camel Spring Web Services endpoint to unregister consumers
	 * @param key unique consumer key
	 */
	public void removeConsumer(Object key) {
		endpoints.remove(key);
	}
}
