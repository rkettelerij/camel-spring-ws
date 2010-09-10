package org.apache.camel.component.spring.ws;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.w3c.dom.Document;

/**
 * Generates static response on StockQuote webservice requests
 */
public class StockQuoteResponseProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		File file = new File("src/test/resources/stockquote-response.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		exchange.getOut().setBody(doc);
	}

}
