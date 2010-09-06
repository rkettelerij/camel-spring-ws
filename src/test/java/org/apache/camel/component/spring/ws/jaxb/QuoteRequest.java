package org.apache.camel.component.spring.ws.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;


@XmlRootElement(name = "GetQuote")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteRequest {

    @XmlElement(required = true)
	private String symbol;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}
