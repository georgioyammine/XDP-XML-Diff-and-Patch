
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestingXMLDiff {

	public static void main(String[] args) throws Exception {
//		String f1 = "testNewA.xml";
//		String f2 = "testNewB.xml";
		
//		String f1 = "a2r.xml";
//		String f2 = "a02.xml";
	
//		String f1 = "E1_A.xml";
//		String f2 = "E1_A2.xml";
		
		String f1 = "xmlFiles/SampleDoc (original).xml";
		String f2 = "xmlFiles/SampleDoc (original) V1.xml";
		
		File file = new File(f1);
		File file2 = new File(f2);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document document = db.parse(file);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();

		clean(root);
		// System.out.println(root.getChildNodes().item(0));
		// Node A1 = root.getChildNodes().item(0);

		DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
		dbf2.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db2 = dbf2.newDocumentBuilder();

		Document document2 = db2.parse(file2);
		document2.getDocumentElement().normalize();
		Element root2 = document2.getDocumentElement();

		clean(root2);
		Node A2 = root2.getChildNodes().item(0);

//		print(root, 0);
//		System.out.println();
//		print(root2, 0);
//		System.out.println();
		
		System.out.println("Start");
		double t1 = System.currentTimeMillis();
//		ArrayList<Object> arl = XMLDiffAndPatch.TEDandEditScript(f1, f2);
//		System.out.println(arl);
//		System.out.println("Similarity: "+((int)(10000*(double)arl.get(2))/100.0)+"%");
		
		XMLDiffAndPatch.applyPatchXML(f1, "PATCH_SampleDoc (original).xml_SampleDoc (original) V1.xml_0.1_2.xml", false);
//		System.out.println("Patched");

//		XMLDiffAndPatch.WriteXMLtoFile(XMLDiffAndPatch.getRootNodeFromFile(f1), "______PATCH_SampleDoc (original).xml_SampleDoc (original) V1.xml_0.1.xml", false);
		System.out.println(System.currentTimeMillis()-t1+"ms");
		System.out.println("NEW");
	
	}

	private static void print(Node node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
		System.out.print(node.getNodeName()+" ");
		if (node.getNodeType() != Node.TEXT_NODE) {
			System.out.print("{");
			NamedNodeMap att = node.getAttributes();
			for (int i = 0; att != null && i < att.getLength(); i++) {
				System.out.print(att.item(i).getNodeName() + ": "+att.item(i).getNodeValue());
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
				trimmedNodeVal = removeExtraSpaces(trimmedNodeVal);
				if (trimmedNodeVal.length() == 0)
					node.removeChild(child);
				else
					child.setNodeValue(trimmedNodeVal);
			} else if (nodeType == Node.COMMENT_NODE)
				node.removeChild(child);
		}
	}
	
	public static String removeExtraSpaces(String str) {
		str = str.replaceAll("\\s+", " ");
		return str;
	}

	

	public static void WriteXMLtoFile(Node root, String fileName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(root, true);
			doc.appendChild(rootC);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transf = transformerFactory.newTransformer();

			transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transf.setOutputProperty(OutputKeys.INDENT, "yes");
			transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	
			DOMSource source = new DOMSource(doc);

			File myFile = new File(fileName);

			StreamResult console = new StreamResult(System.out);
			StreamResult filen = new StreamResult(myFile);
			
			transf.transform(source, console);
			transf.transform(source, filen);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
