package org.apache.camel.component.spring.ws.type;

import org.springframework.xml.xpath.XPathExpression;

public class EndpointMappingKey {
	private EndpointMappingType type;
	private String lookupKey;
	
	/** expression in case type is 'xpath' */
	private XPathExpression expression;

	public EndpointMappingKey(EndpointMappingType type, String lookupKey,
			XPathExpression expression) {
		super();
		this.type = type;
		this.lookupKey = lookupKey;
		this.expression = expression;
	}

	public EndpointMappingType getType() {
		return type;
	}

	public void setType(EndpointMappingType type) {
		this.type = type;
	}

	public String getLookupKey() {
		return lookupKey;
	}

	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}

	public XPathExpression getExpression() {
		return expression;
	}

	public void setExpression(XPathExpression expression) {
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lookupKey == null) ? 0 : lookupKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndpointMappingKey other = (EndpointMappingKey) obj;
		if (lookupKey == null) {
			if (other.lookupKey != null)
				return false;
		} else if (!lookupKey.equals(other.lookupKey))
			return false;
		return true;
	}
}