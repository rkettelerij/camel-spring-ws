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
import org.apache.camel.component.spring.ws.type.EndpointMappingType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
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
 * This class should be registered as a Spring bean in the <tt>ApplicationContext</tt>.
 * 
 * @author Richard Kettelerij
 */
public class CamelEndpointMapping extends AbstractEndpointMapping {
	
	private Map<Object, EndpointHolder> consumers = new ConcurrentHashMap<Object, EndpointHolder>();
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

	@Override
	protected Object getEndpointInternal(MessageContext messageContext) throws Exception {
		for (Map.Entry<Object, EndpointHolder> entry : consumers.entrySet()) {
			Object messageKey = null;
			switch (entry.getValue().getType()) {
				case ROOT_QNAME : 
					messageKey = getRootQName(messageContext); 
					break;
				case SOAP_ACTION : 
					messageKey = getSoapAction(messageContext); 
					break;
				case XPATH :
					messageKey = getXPathExpression(messageContext, entry.getKey());
					break;
				case URI : 
					messageKey = getUri(); 
					break;
				default : 
					throw new RuntimeCamelException("Invalid endpoint mapping type specified");
			}
			if (messageKey != null && messageKey.equals(entry.getKey())) {
				return entry.getValue().getEndpoint();
			}
		}        
		return null;
	}
	
    @Override
    protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext, Object endpoint, EndpointInterceptor[] interceptors) {
    	for (Map.Entry<Object, EndpointHolder> entry : consumers.entrySet()) {
    		if (EndpointMappingType.SOAP_ACTION.equals(entry.getValue().getType())) {
    			Object messageKey = getSoapAction(messageContext);
    			if (messageKey != null && messageKey.equals(entry.getKey())) {
    				return new SoapEndpointInvocationChain(endpoint, interceptors);
    				// TODO add support for SOAP actors/roles and ultimate receiver
    			}
    		}
		}
        return super.createEndpointInvocationChain(messageContext, endpoint, interceptors);
    }

	private String getSoapAction(MessageContext messageContext) {
		if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage request = (SoapMessage) messageContext.getRequest();
            String soapAction = request.getSoapAction();
            if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' && soapAction.charAt(soapAction.length() - 1) == '"') {
                return soapAction.substring(1, soapAction.length() - 1);
            }
            return soapAction;
        }
        return null;
	}

	private String getUri() throws URISyntaxException {
		TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext != null) {
            WebServiceConnection connection = transportContext.getConnection();
            if (connection != null) {
                return connection.getUri().toString();
            }
        }
        return null;
	}

	private String getRootQName(MessageContext messageContext) throws TransformerException, XMLStreamException {
		QName qName = PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
		return qName != null ? qName.toString() : null;
	}

	private String getXPathExpression(MessageContext messageContext, Object lookupKey) throws TransformerException, XMLStreamException {
		if (false) { //FIXME
			XPathExpression expression = (XPathExpression) lookupKey;
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMResult domResult = new DOMResult();
	        transformer.transform(messageContext.getRequest().getPayloadSource(), domResult);
	        Element elm = (Element) domResult.getNode().getFirstChild();
	        return expression.evaluateAsString(elm); 
		}
		return null;
	}
	
	public void addConsumer(Object key, EndpointMappingType type, MessageEndpoint endpoint) {
		consumers.put(key, new EndpointHolder(type, endpoint));
	}
	
	public void removeConsumer(Object key) {
		consumers.remove(key);
	}
	
	protected class EndpointHolder {
		private EndpointMappingType type;
		private MessageEndpoint endpoint;
		
		public EndpointHolder(EndpointMappingType type, MessageEndpoint endpoint) {
			super();
			Assert.notNull(type);
			Assert.notNull(endpoint);
			this.type = type;
			this.endpoint = endpoint;
		}

		public EndpointMappingType getType() {
			return type;
		}

		public MessageEndpoint getEndpoint() {
			return endpoint;
		}
	}
}
