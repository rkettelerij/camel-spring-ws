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
import java.net.URISyntaxException;

import org.springframework.ws.client.core.WebServiceTemplate;

public class SpringWebserviceConfiguration {

	/* Producer configuration */
	private WebServiceTemplate webServiceTemplate;
	private String soapAction;
	private URI wsAddressingAction;

	public WebServiceTemplate getWebServiceTemplate() {
		return webServiceTemplate;
	}

	public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		this.webServiceTemplate = webServiceTemplate;
	}

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
	
	public String getEndpointUri() {
		return webServiceTemplate.getDefaultUri();
	}

	public URI getWsAddressingAction() {
		return wsAddressingAction;
	}

	public void setWsAddressingAction(URI wsAddressingAction) {
		this.wsAddressingAction = wsAddressingAction;
	}

	public void setWsAddressingAction(String wsAddressingAction) throws URISyntaxException {
		this.wsAddressingAction = new URI(wsAddressingAction);
	}
}
