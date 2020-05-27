package com.georgioyammine.classes;
import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util {
	public static int contains(Node a, ArrayList<Node> listB) {
		for(int i = 0; i<listB.size();i++) {
			if(a.getNodeName().equals((listB.get(i).getNodeName())))
				return i;
		}
		return -1;
	}


	public static ArrayList<Node> getArlFromNNM(NamedNodeMap a){
		if(a==null)
			return new ArrayList<Node>();
		ArrayList<Node> listA = new ArrayList<Node>();
		for(int i = 0; i<a.getLength();i++) {
			listA.add(a.item(i));
		}
		return listA;
	}

	public static String WriteXMLtoFile(Node root, String fileName, boolean overwrite) {
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

			int index = 1;
			String fileName2 = fileName;
			if (!overwrite) {
				while (new File(fileName2).exists()) {
					index++;
					fileName2 = fileName.substring(0, fileName.lastIndexOf('.')) + "_" + index
							+ fileName.substring(fileName.lastIndexOf('.'));
				}
			}
			File myFile = new File(fileName2);

			// StreamResult console = new StreamResult(System.out);
			StreamResult filen = new StreamResult(myFile);

			// transf.transform(source, console);
			transf.transform(source, filen);
			return myFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}
	public static boolean stringContainedIn(Node rootA, ArrayList<Node> listB) {
		String[] arr = rootA.getTextContent().split("\\s+");
		for (int i = 0; i < listB.size(); i++) {
			String[] arr2 = listB.get(i).getTextContent().split("\\s+");
			if (arr.equals(arr2))
				return true;
		}
		return false;
	}

	public static String getAlphaNumericString(int n) {

		// chose a Character random from this String
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {

			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index = (int) (AlphaNumericString.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}

	public static void print(Node node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
		System.out.print(node.getNodeName() + " ");
		if (node.getNodeType() != Node.TEXT_NODE) {
			System.out.print("{");
			NamedNodeMap att = node.getAttributes();
			for (int i = 0; att != null && i < att.getLength(); i++) {
				System.out.print(att.item(i).getNodeName() + ", " + att.item(i).getNodeValue());
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

}
