import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLHandler {
	File file;
	Document doc;

	public XMLHandler(String location) {
		try {

			file = new File(location);	

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();


		} catch (Exception e) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, e.getMessage());
			e.printStackTrace();
		}
	}

	public String get_value(String TagName, String key) {

		NodeList nodeLst = doc.getElementsByTagName(TagName);

		for (int s = 0; s < nodeLst.getLength(); s++) {
			Node fstNode = nodeLst.item(s);
			if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

				Element fstElmnt = (Element) fstNode;
				NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(key);
				Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				NodeList fstNm = fstNmElmnt.getChildNodes();

				return ((Node) fstNm.item(0)).getNodeValue();
			}
		}

		return "";

	}

	
	public void set_value(String TagName, String key, String value) {

		NodeList nodeLst = doc.getElementsByTagName(TagName);

		for (int s = 0; s < nodeLst.getLength(); s++) {
			Node fstNode = nodeLst.item(s);
			if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

				Element fstElmnt = (Element) fstNode;
				NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(key);
				Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				NodeList fstNm = fstNmElmnt.getChildNodes();

				
				System.out.println("Switcing from " + ((Node) fstNm.item(0)).getNodeValue() + " to " + value);

				((Node) fstNm.item(0)).setTextContent(value);
				
			}
		}

	}

}
