
import java.io.File;

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

public class TestingNNJ {

	public static void main(String[] args) throws Exception {
		String f1 = "sample1.xml";
		String f2 = "sampleDoc (original).xml";
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

		print(root, 0);
		System.out.println();
		print(root2, 0);
		System.out.println();
//		ArrayList<Object> nnj = NNJ.TED(root, root2, "A", "B");
//		System.out.println(nnj);
//		// System.out.println(nnj.get(2));
//		ArrayList<String> fes = NNJ.formatEditScipt((ArrayList<NNJ.XYZ>) nnj.get(1));
//		System.out.println(fes);

//		Node c = NNJ.applyPatch(fes, root, root2);
//		WriteXMLtoFile(c, "patched" + file.getName().substring(0, file.getName().indexOf(".")) + "&"
//				+ file2.getName().substring(0, file.getName().indexOf(".")) + ".xml");
		
//		NNJ.editScriptToXML((ArrayList<NNJ.XYZ>) nnj.get(1), file, file2,root2,1);
		
//		System.out.println("Distance = "+NNJ.TEDandEditScript(f1, f2));
		NNJ.applyPatchXML(f1, "PATCH_sample1.xml_sampleDoc (original).xml_0.1_2.xml");
		// NodeList staffList = root.getChildNodes();
		// hhhhh
		// se
		// se

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
				System.out.print(att.item(i).getNodeName() + ", "+att.item(i).getNodeValue());
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
