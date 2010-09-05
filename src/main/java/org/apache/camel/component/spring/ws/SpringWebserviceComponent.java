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

import java.net.URI;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.EndpointHelper;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Apache Camel component for working with Spring Webservices (a.k.a Spring-WS).
 * 
 * @author Richard Kettelerij
 */
public class SpringWebserviceComponent extends DefaultComponent {

	public SpringWebserviceComponent() {
		super();
	}

	public SpringWebserviceComponent(CamelContext context) {
		super(context);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		URI webServiceEndpointUri = new URI(remaining);
		
        // Get a WebServiceTemplate from the registry if specified by an option on the component, else create a new template with Spring-WS defaults
        WebServiceTemplate webServiceTemplate = resolveAndRemoveReferenceParameter(parameters, "webServiceTemplate", WebServiceTemplate.class, new WebServiceTemplate());
    	WebServiceMessageSender messageSender = resolveAndRemoveReferenceParameter(parameters, "messageSender", WebServiceMessageSender.class, null);
    	WebServiceMessageFactory messageFactory = resolveAndRemoveReferenceParameter(parameters, "messageFactory", WebServiceMessageFactory.class, null);
    	
        if (webServiceTemplate.getDefaultUri() == null) {
            webServiceTemplate.setDefaultUri(webServiceEndpointUri.toString());
        }
        if (messageSender != null) {
        	webServiceTemplate.setMessageSender(messageSender);
        }
        if (messageFactory != null) {
        	webServiceTemplate.setMessageFactory(messageFactory);
        }
        SpringWebserviceConfiguration configuration = new SpringWebserviceConfiguration();
        configuration.setWebServiceTemplate(webServiceTemplate);
        EndpointHelper.setProperties(getCamelContext(), configuration, parameters);
        return new SpringWebserviceEndpoint(this, configuration);
	}
}
