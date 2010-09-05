package org.apache.camel.component.spring.ws.type;

/**
 * Endpoint mappings supported by consumer through uri configuration.
 */
public enum EndpointMappingType {
	ROOT_QNAME("rootqname:"), 
	SOAP_ACTION("soapaction:"), 
	XPATH("xpath:"), 
	URI("uri:");
	
	private String prefix;

	private EndpointMappingType(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * Find {@link EndpointMappingType} that corresponds with the prefix of the
	 * given uri. Matching of uri prefix against enum values is case-insensitive
	 * 
	 * @param uri remaining uri part of Spring-WS component
	 * @return EndpointMappingType corresponding to uri prefix
	 */
	public static EndpointMappingType getTypeFromUriPrefix(String uri) {
		if (uri != null) {
			for (EndpointMappingType type : EndpointMappingType.values()) {
				if (uri.toLowerCase().startsWith(type.getPrefix())) {
					return type;
				}
			}
		}
		return null;
	}
}