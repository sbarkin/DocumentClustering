package xmlDocument;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLDocument {

	
	/* Return string value for first occurence of the given tag within
	 * the specified element.  Ignores any successors.
	 */
	static public String getStringValue(Element e, String tagname) {
		NodeList list = e.getElementsByTagName(tagname);
		String value = list.item(0).getTextContent();
		return value;
	}
	
	static public Document buildDocumentFromFile(String filename)
			throws ParserConfigurationException, SAXException, IOException {
		File inFile = new File(filename);
		
		/* build document */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		
		Document myDoc = docBuilder.parse(inFile);
		return myDoc;
	}
	
	static public void displayDocumentInfo(Document d) {
		String encoding = d.getXmlEncoding();
		String version = d.getXmlVersion();
		Element element = d.getDocumentElement();
		String tagname = element.getTagName();

		System.out.printf("Document encoding: %s\n", encoding);
		System.out.printf("Document version: %s\n", version);
		System.out.printf("Tag name for element: %s\n", tagname);
	}
}
