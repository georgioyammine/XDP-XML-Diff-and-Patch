
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProcessLocalXML2 {

	public static void main(String[] args) throws Exception {
		File file = new File("a.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		Document document = db.parse(file);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		clean(root);
		print(root, 0);
		System.out.println(root.getNodeName());
		// NodeList staffList = root.getChildNodes();
		// hhhhh
		//se

	}

	private static void print(Node node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
		System.out.print(node.getNodeName() + " ");
		if (node.getNodeType() != Node.TEXT_NODE) {
			System.out.print("{");
			NamedNodeMap att = node.getAttributes();
			for (int i = 0; att != null && i < att.getLength(); i++) {
				System.out.print(att.item(i) + ", ");
			}
			System.out.print("}");
		}

		System.out.println(node.getNodeValue() == null ? "" : node.getNodeValue());

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			// clean(list.item(i));
			print(list.item(i), depth + 1);
		}
	}

	public static void clean(Node node) {
		NodeList childNodes = node.getChildNodes();

		for (int n = childNodes.getLength() - 1; n >= 0; n--) {
			Node child = childNodes.item(n);
			short nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE)
				clean(child);
			else if (nodeType == Node.TEXT_NODE) {
				String trimmedNodeVal = child.getNodeValue().trim();
				if (trimmedNodeVal.length() == 0)
					node.removeChild(child);
				else
					child.setNodeValue(trimmedNodeVal);
			} else if (nodeType == Node.COMMENT_NODE)
				node.removeChild(child);
		}
	}
}
