
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.zip.CRC32;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NNJ {
	final static int updateRootCost = 1;
	final static int insertContained = 1;
	final static int deleteContained = 1;
	final static int deleteOrInsertLeaf = 1;
	private static ArrayList<Node> TreesInA = new ArrayList<>();
	private static ArrayList<Node> TreesInB = new ArrayList<>();
	private static String randomDelete = "jci8ElHvLDr6DKejR7ng";
	static double version = 0.1;
	

	private static class XYZ {
		int x, y, nx, ny, z;
		String a, b;

		public XYZ(String A, String B, int x, int y, int nx, int ny, int z) {
			this.a = A;
			this.b = B;
			this.x = x;
			this.y = y;
			this.nx = nx;
			this.ny = ny;
			this.z = z;
		}

		public String toString() {
			return "[" + a + ", " + b + ", " + x + ", " + y + ", " + nx + ", " + ny + ", " + z + "]";
		}
	}

	public static ArrayList<Object> TED(Node rootA, Node rootB, String R1, String R2, boolean print) {

		clean(rootA);
		clean(rootB);

		getTreesInA(rootA); // pre-processing
		getTreesInB(rootB); // pre-processing

		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();

		int m = listA.getLength();
		int n = listB.getLength();

		int[][] dist = new int[m + 1][n + 1];
		ArrayList<Object>[][] pointers = new ArrayList[m + 1][n + 1];
		ArrayList<Object> results = new ArrayList<>();
		results.add(dist);
		results.add(pointers);

		dist[0][0] = CostUpdateRoot(rootA, rootB);
		ArrayList<Object> init = new ArrayList<>();
		init.add(new XYZ(R1, R2, -1, -1, 0, 0, dist[0][0]));
		pointers[0][0] = init;

		for (int i = 1; i <= m; i++) {
			dist[i][0] = dist[i - 1][0] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
			ArrayList<Object> del = new ArrayList<>();
			del.add(new XYZ(R1, R2, i - 1, 0, i, 0, dist[i][0]));
			pointers[i][0] = del;
		}
		for (int j = 1; j <= n; j++) {
			dist[0][j] = dist[0][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
			ArrayList<Object> ins = new ArrayList<>();
			ins.add(new XYZ(R1, R2, 0, j - 1, 0, j, dist[0][j]));
			pointers[0][j] = ins;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				// System.out.println(rootA.getChildNodes().item(i-1));
				// System.out.println(rootB.getChildNodes().item(j-1));
				// System.out.println(i+" "+j+" "+
				// rootA+" "+rootB+
				// " "+(CostDeleteTree(rootA))
				// +" "+(CostInsertTree(rootB))
				// );

				ArrayList<Object> updateInfo = TED(listA.item(i - 1), listB.item(j - 1), R1 + i, R2 + j, false);
				int update = dist[i - 1][j - 1] + (int) updateInfo.get(0);
				int delete = dist[i - 1][j] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
				int insert = dist[i][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
				if (update < delete) {
					if (update < insert) {
						dist[i][j] = update;
						ArrayList<Object> upd = new ArrayList<>();
						upd.add(new XYZ(R1, R2, i - 1, j - 1, i, j, dist[i][j]));
						upd.add(updateInfo.get(1));
						pointers[i][j] = upd;
					} else {
						dist[i][j] = insert;
						ArrayList<Object> ins = new ArrayList<>();
						ins.add(new XYZ(R1, R2, i, j - 1, i, j, dist[i][j]));
						pointers[i][j] = ins;
					}
				} else {
					if (delete < insert) {
						dist[i][j] = delete;
						ArrayList<Object> del = new ArrayList<>();
						del.add(new XYZ(R1, R2, i - 1, j, i, j, dist[i][j]));
						pointers[i][j] = del;
					} else {
						dist[i][j] = insert;
						ArrayList<Object> ins = new ArrayList<>();
						ins.add(new XYZ(R1, R2, i, j - 1, i, j, dist[i][j]));
						pointers[i][j] = ins;
					}
				}

				// dist[i][j] = Math.min(Math.min(dist[i-1][j-1] + TED(listA.item(i-1),
				// listB.item(j-1)),
				// dist[i-1][j] + CostDeleteTree(rootA.getChildNodes().item(i-1))),
				// dist[i][j-1] + CostInsertTree(rootB.getChildNodes().item(j-1)));
			}
		}
		if (print) {
			System.out.println(R1 + ":" + rootA + " " + R2 + ":" + rootB);
			for (int i = 0; i <= m; i++) {
				System.out.println(Arrays.toString(dist[i]));
			}
			System.out.println();
		}

		ArrayList<XYZ> ES = getEditScript(m, n, pointers, dist, R1, R2);

		results.add(0, ES);
		results.add(0, dist[m][n]);
		// results.add(2,fes);
		return results;
	}

	private static ArrayList<XYZ> getEditScript(int m, int n, ArrayList<Object>[][] pointers, int[][] dist, String r1,
			String r2) {
		ArrayList<XYZ> reversedEditScript = new ArrayList<>();
		// XYZ last;
		// if(pointers[m][n]==null)
		// last = new XYZ("X", "X", 0 ,0,0, 0,0);
		// else
		// last = (XYZ)pointers[m][n].get(0);
		// reversedEditScript.add(last);

		int a = m, b = n;
		int A = 10;
		while (a != -1 && b != -1 && ((XYZ) (pointers[a][b].get(0))).z != 0) {
			ArrayList<Object> prev = pointers[a][b];
			if (prev.size() > 1) {
				// System.out.println(((int[][])(prev.get(1)))[0][0]);
				// reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));
				reversedEditScript.addAll((ArrayList<XYZ>) prev.get(1));
				// reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));
			} else
				reversedEditScript.add((XYZ) prev.get(0));

			a = (int) ((XYZ) prev.get(0)).x;
			b = (int) ((XYZ) prev.get(0)).y;

		}
		return reversedEditScript;
	}

	private static ArrayList<String> formatEditScipt(ArrayList<XYZ> E) {
		ArrayList<XYZ> ES = new ArrayList<>(E);
		ArrayList<String> fes = new ArrayList<>();
		Collections.reverse(ES);
		for (XYZ token : ES) {
			StringBuilder temp = new StringBuilder(" { ");
			if (token.x == -1) {
				temp.append("Update R(" + token.a + ") R(" + token.b + ")");
			} else {
				if (token.x < token.nx) {
					temp.append("Delete " + token.a + token.nx);
				} else {
					temp.append("Insert " + token.b + token.ny);
				}
			}
			temp.append(" } ");
			fes.add(temp.toString());
		}
		return fes;
	}

	public static int TEDandEditScript(String fileName1, String fileName2) throws Exception {

		File file = new File(fileName1);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(file);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		clean(root);

		File file2 = new File(fileName2);
		DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
		dbf2.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db2 = dbf2.newDocumentBuilder();
		Document document2 = db2.parse(file2);
		document2.getDocumentElement().normalize();
		Element root2 = document2.getDocumentElement();

		ArrayList<Object> nnj = NNJ.TED(root, root2, "A", "B", false);

		int distance = (Integer) nnj.get(0);
		editScriptToXML((ArrayList<NNJ.XYZ>) nnj.get(1), file, file2, root2, distance);
		return distance;
	}

	private static void editScriptToXMLnoOrder(ArrayList<XYZ> E, File original, File newFile, Node rootB, int distance) {
		try {
			ArrayList<XYZ> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XML_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);

			Element file1 = document.createElement("Original_File");
			file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setTextContent(newFile.getName());

			Element difference = document.createElement("Distance");
			difference.setTextContent("" + distance);

			root.appendChild(file1);
			root.appendChild(file2);
			root.appendChild(difference);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			Collections.reverse(ES);

			for (XYZ token : ES) {
				Element elt;
				if (token.x == -1) {
					elt = document.createElement("Update");

					String op = token.b + "";
					op = op.substring(1); // removing B or Tree Name
					Node toUpdate = document.importNode(rootB, true);

					while (op.length() > 0) {
						toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					elt.setAttribute(token.a, toUpdate.getNodeName());

				} else {
					if (token.x < token.nx) {
						elt = document.createElement("Delete");
						// elt.setTextContent(token.a+token.nx+"");
						elt.setAttribute("at", token.a + token.nx + "");
					} else {
						elt = document.createElement("Insert");
						elt.setAttribute("at", "A" + (token.b + token.ny + "").substring(1));
						// elt.setTextContent(token.b+token.ny+"");

						String opc = token.b + token.ny + "";
						opc = opc.substring(1); // removing B or Tree Name
						Node toInsert = document.importNode(rootB, true);

						while (opc.length() > 0) {
							toInsert = toInsert.getChildNodes().item(Integer.parseInt("" + opc.charAt(0)) - 1);
							opc = opc.substring(1);
						}
						elt.appendChild(toInsert);
					}
				}
				// System.out.println("XX"+elt);
				es.appendChild(elt);
			}

			String fileName = "PATCH_" + original.getName() + "_" + newFile.getName() + "_" + version + ".xml";

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transf = transformerFactory.newTransformer();

			transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transf.setOutputProperty(OutputKeys.INDENT, "yes");
			transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DOMSource source = new DOMSource(document);

			File myFile = new File(fileName);

			// StreamResult console = new StreamResult(System.out);
			StreamResult filen = new StreamResult(myFile);

			// transf.transform(source, console);
			transf.transform(source, filen);
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	private static void editScriptToXML(ArrayList<XYZ> E, File original, File newFile, Node rootB, int distance)
			throws Exception {
		try {
			ArrayList<XYZ> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XML_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");

			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(),true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("Hash", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());
			
			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(),true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			
			file2.setAttribute("Hash", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			for (XYZ token : ES) {
				
				if (token.x == -1) {
					Element elt2 = document.createElement(token.a+ "");
					

					String op = token.b + "";
					op = op.substring(1); // removing B or Tree Name
					Node toUpdate = document.importNode(rootB, true);

					while (op.length() > 0) {
						toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
//					elt.setAttribute(token.a, toUpdate.getNodeName());
					// System.out.println("XX"+elt);
					elt2.setTextContent(toUpdate.getNodeName());
					eltu.appendChild(elt2);
					
					// ES.remove(token);
				}
				
			}
			if(eltu.hasChildNodes())
				es.appendChild(eltu);
			
			Element eltd;
			eltd = document.createElement("Delete");
			for (XYZ token : ES) {
				
				if (token.x != -1 && token.x < token.nx) {
					Element elt2 = document.createElement(token.a + token.nx + "");
					eltd.appendChild(elt2);
					// ES.remove(token);
				}
				
			}
			if(eltd.hasChildNodes())
				es.appendChild(eltd);
			Element elti;
			elti = document.createElement("Insert");
			for (XYZ token : ES) {
				
				if (token.x != -1 && !(token.x < token.nx)) {
					Element elt2;
					elt2 = document.createElement("A" + (token.b + token.ny + "").substring(1));
//					elt.setAttribute("at", );
					// elt.setTextContent(token.b+token.ny+"");

					String opc = token.b + token.ny + "";
					opc = opc.substring(1); // removing B or Tree Name
					Node toInsert = document.importNode(rootB, true);

					while (opc.length() > 0) {
						toInsert = toInsert.getChildNodes().item(Integer.parseInt("" + opc.charAt(0)) - 1);
						opc = opc.substring(1);
					}
					elt2.appendChild(toInsert);
					elti.appendChild(elt2);
					
					// ES.remove(token);

				}
				
			}
			if(elti.hasChildNodes())
				es.appendChild(elti);

			String fileName = "PATCH_" + original.getName() + "_" + newFile.getName() + "_" + version + ".xml";

			WriteXMLtoFile(root, fileName,false);
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}

	}

	
	// update then delete then insert
	private static void editScriptToXMLold(ArrayList<XYZ> E, File original, File newFile, Node rootB, int distance)
			throws Exception {
		try {
			ArrayList<XYZ> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XML_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");

			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(),true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);
			
			
			
			file1.setAttribute("Hash", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());
			
			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(),true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			
			file2.setAttribute("Hash", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			Collections.reverse(ES);

			for (XYZ token : ES) {

				if (token.x == -1) {
					Element elt;
					elt = document.createElement("Update");

					String op = token.b + "";
					op = op.substring(1); // removing B or Tree Name
					Node toUpdate = document.importNode(rootB, true);

					while (op.length() > 0) {
						toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					elt.setAttribute(token.a, toUpdate.getNodeName());
					// System.out.println("XX"+elt);
					es.appendChild(elt);
					// ES.remove(token);
				}
			}
			for (XYZ token : ES) {
				Element elt;
				if (token.x != -1 && token.x < token.nx) {
					elt = document.createElement("Delete");
					// elt.setTextContent(token.a+token.nx+"");
					elt.setAttribute("at", token.a + token.nx + "");
					es.appendChild(elt);
					// ES.remove(token);
				}
			}
			for (XYZ token : ES) {

				if (token.x != -1 && !(token.x < token.nx)) {
					Element elt;
					elt = document.createElement("Insert");
					elt.setAttribute("at", "A" + (token.b + token.ny + "").substring(1));
					// elt.setTextContent(token.b+token.ny+"");

					String opc = token.b + token.ny + "";
					opc = opc.substring(1); // removing B or Tree Name
					Node toInsert = document.importNode(rootB, true);

					while (opc.length() > 0) {
						toInsert = toInsert.getChildNodes().item(Integer.parseInt("" + opc.charAt(0)) - 1);
						opc = opc.substring(1);
					}
					elt.appendChild(toInsert);

					es.appendChild(elt);
					// ES.remove(token);

				}
			}

			String fileName = "PATCH_" + original.getName() + "_" + newFile.getName() + "_" + version + ".xml";

			WriteXMLtoFile(root, fileName,false);
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}

	}

	public static Node getRootNodeFromFile(String fileName) throws Exception {
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dcm = db.parse(file);
		dcm.getDocumentElement().normalize();
		Element root = dcm.getDocumentElement();
		clean(root);
		return root;
	}
	
	public static String applyPatchXML(String fileName, String ESXML) throws Exception {
		try {
			randomDelete ="A"+getAlphaNumericString(9);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(getRootNodeFromFile(fileName), true);
			doc.appendChild(rootC);

			Node ESroot = getRootNodeFromFile(ESXML);

			if (Double.parseDouble(((Element) ESroot).getAttributes().item(1).getNodeValue()) != version) {
				System.out.println("Error: Incompatible version");
				return "";
			}
			
			boolean fileNameMatches = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(1).getNodeValue().equals(fileName);

			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(0).getNodeValue();

			
			File tmpInput = File.createTempFile("nnj", ".tmp");
			WriteXMLtoFile(getRootNodeFromFile(fileName), tmpInput.getAbsolutePath(),true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);
			tmpInput.delete();

			boolean HashCodeMatches = hs1.equals(hashInput);

			if (!HashCodeMatches) {
				System.out.println("Error: Original File does not match!");
				return "";
			}
			if (!fileNameMatches) {
				// System.out.println(((Element)ESroot).getElementsByTagName("Original_File").item(0).getTextContent());
				// System.out.println(fileName);
				System.out.println("Warning: Original File Name does not match!");
			}
			String patchedName = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(1)
					.getNodeValue();
			String targetHash = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(0)
					.getNodeValue();

//			NodeList editScript = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);
			
			Node update = ((Element)es).getElementsByTagName("Update").item(0);
			Node delete = ((Element)es).getElementsByTagName("Delete").item(0);
			Node insert = ((Element)es).getElementsByTagName("Insert").item(0);
			
			if(update!=null) {
				NodeList upd = update.getChildNodes();
				for(int i = 0;i<upd.getLength();i++) {
					Node node = upd.item(i);
					String op1 = node.getNodeName();
					String op2 = node.getTextContent();
					op1 = op1.substring(1); // remove A or Tree name

					Node rc = rootC;

					while (op1.length() > 0) {
						rc = rc.getChildNodes().item(Integer.parseInt("" + op1.charAt(0)) - 1);
						op1 = op1.substring(1);
					}
					doc.renameNode(rc, null, op2);
					
				}
			}
			
			if(delete!=null) {
				NodeList del = delete.getChildNodes();
				for(int i = del.getLength()-1;i>=0;i--) {
					Node node = del.item(i);
					String op = node.getNodeName();
					op = op.substring(1); // removing A or Tree Name
					Node temp = rootC;
					while (op.length() > 1) {
						temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
//					doc.renameNode(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1), null, randomDelete);
					temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));
					
				}
			}
				
				if(insert!=null) {
					NodeList ins = insert.getChildNodes();
					for(int i = 0;i<ins.getLength();i++) {
						Node node = ins.item(i);
						
						Node toInsert = doc.importNode(node.getFirstChild(),true);

						String op = node.getNodeName();
						op = op.substring(1); // removing A or Tree Name
						Node temp = rootC;
						while (op.length() > 1) {
							temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
						if(temp.hasChildNodes()) {
						temp.insertBefore(toInsert,
								temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));
						}
						else {
							temp.appendChild(toInsert);
						}
					}	
			}
			
			
			String absPath = WriteXMLtoFile(rootC, patchedName,false);
			
			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(absPath), tmpInput2.getAbsolutePath(),true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			
			if (!targetHash.equals(hashInput2)) {
				System.out.println("Wrong Result Expected: Hash checksum does not match");
			}
			else {
				System.out.println("Patch successful, hash checksum matches!");
			}
			
			return absPath;

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}

	
	
	

	public static String applyPatchXMLold1(String fileName, String ESXML) throws Exception {
		try {
			randomDelete ="A"+getAlphaNumericString(8);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(getRootNodeFromFile(fileName), true);
			doc.appendChild(rootC);

			Node ESroot = getRootNodeFromFile(ESXML);

			if (Double.parseDouble(((Element) ESroot).getAttributes().item(1).getNodeValue()) != version) {
				System.out.println("Error: Incompatible version");
				return "";
			}
			
			boolean fileNameMatches = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(1).getNodeValue().equals(fileName);

			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(0).getNodeValue();

			
			File tmpInput = File.createTempFile("nnj", ".tmp");
			WriteXMLtoFile(getRootNodeFromFile(fileName), tmpInput.getAbsolutePath(),true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);
			tmpInput.delete();

			boolean HashCodeMatches = hs1.equals(hashInput);

			if (!HashCodeMatches) {
				System.out.println("Error: Original File does not match!");
				return "";
			}
			if (!fileNameMatches) {
				// System.out.println(((Element)ESroot).getElementsByTagName("Original_File").item(0).getTextContent());
				// System.out.println(fileName);
				System.out.println("Warning: Original File Name does not match!");
			}
			String patchedName = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(1)
					.getNodeValue();
			String targetHash = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(0)
					.getNodeValue();

			NodeList editScript = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			for (int i = 0; i < editScript.getLength(); i++) {
				Node script = editScript.item(i);
				if (script.getNodeName().equals("Update")) {
					String op1 = script.getAttributes().item(0).getNodeName();
					String op2 = script.getAttributes().item(0).getNodeValue();
					op1 = op1.substring(1); // remove A or Tree name

					Node rc = rootC;

					while (op1.length() > 1) {
						rc = rc.getChildNodes().item(Integer.parseInt("" + op1.charAt(0)) - 1);
						op1 = op1.substring(1);
					}
					doc.renameNode(rc, null, op2);
				} else {
					boolean aaa = true;
					if (script.getNodeName().equals("Delete")) {
						String op = script.getAttributes().item(0).getNodeValue();
						op = op.substring(1); // removing A or Tree Name
						Node temp = rootC;
						while (op.length() > 1) {
							temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
						// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
						doc.renameNode(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1), null, randomDelete);
//						temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));

					} else {
						if (script.getNodeName().equals("Insert")) {
							//delete all node from before with name randomDelete
							if(aaa) {
							NodeList deleteList = ((Element)rootC).getElementsByTagName(randomDelete);
							System.out.println(deleteList.getLength()+"size");
							for(int k =0;k<deleteList.getLength();k++) {
								try {
									Node a = doc.getDocumentElement();
//									while()
//									doc.d
//									doc.rem
//									temp.removeChild(deleteList.item(k));
									System.out.print(k+" ");
									System.out.println(rootC.removeChild(deleteList.item(k)));
									
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							aaa=false;
							}
							print(rootC,0);
							Node toInsert = doc.importNode(script.getFirstChild(), true);

							String op = script.getAttributes().item(0).getNodeValue();
							op = op.substring(1); // removing A or Tree Name
							System.out.println(op);
							Node temp = rootC;
							while (op.length() > 1) {
								System.out.print(op);
								temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
								op = op.substring(1);
								System.out.println(temp);
							}
							
							System.out.println(Integer.parseInt("" + op.charAt(0)) - 1);
							System.out.println(temp.hasChildNodes());
							if(temp.hasChildNodes()) {
							temp.insertBefore(toInsert,
									temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));
							}
							else {
								temp.appendChild(toInsert);
							}
//							temp.inser

						}
					}
				}
			}
			
			
			
			
			String absPath = WriteXMLtoFile(rootC, patchedName,false);
			
			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(absPath), tmpInput2.getAbsolutePath(),true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			
			if (!targetHash.equals(hashInput2)) {
				System.out.println("Wrong Result Expected: Hash checksum does not match");
			}
			else {
				System.out.println("Patch successful, hash checksum matches!");
			}
			
			return absPath;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	
	private static String applyPatchXMLold(String fileName, String ESXML) throws Exception {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(getRootNodeFromFile(fileName), true);
			doc.appendChild(rootC);

			Node ESroot = getRootNodeFromFile(ESXML);

			if (Double.parseDouble(((Element) ESroot).getAttributes().item(1).getNodeValue()) != version) {
				System.out.println("Error: Incompatible version");
				return "";
			}
			
			boolean fileNameMatches = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(1).getNodeValue().equals(fileName);

			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.item(0).getNodeValue();

			
			File tmpInput = File.createTempFile("nnj", ".tmp");
			WriteXMLtoFile(getRootNodeFromFile(fileName), tmpInput.getAbsolutePath(),true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);
			tmpInput.delete();

			boolean HashCodeMatches = hs1.equals(hashInput);

			if (!HashCodeMatches) {
				System.out.println("Error: Original File does not match!");
				return "";
			}
			if (!fileNameMatches) {
				// System.out.println(((Element)ESroot).getElementsByTagName("Original_File").item(0).getTextContent());
				// System.out.println(fileName);
				System.out.println("Warning: Original File Name does not match!");
			}
			String patchedName = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(1)
					.getNodeValue();
			String targetHash = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(0)
					.getNodeValue();

			NodeList editScript = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			for (int i = 0; i < editScript.getLength(); i++) {
				Node script = editScript.item(i);
				if (script.getNodeName().equals("Update")) {
					String op1 = script.getAttributes().item(0).getNodeName();
					String op2 = script.getAttributes().item(0).getNodeValue();
					op1 = op1.substring(1); // remove A or Tree name

					Node rc = rootC;

					while (op1.length() > 1) {
						rc = rc.getChildNodes().item(Integer.parseInt("" + op1.charAt(0)) - 1);
						op1 = op1.substring(1);
					}
					doc.renameNode(rc, null, op2);
				} else {
					if (script.getNodeName().equals("Delete")) {
						String op = script.getAttributes().item(0).getNodeValue();
						op = op.substring(1); // removing A or Tree Name
						Node temp = rootC;
						while (op.length() > 1) {
							temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
						// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
						temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));

					} else {
						if (script.getNodeName().equals("Insert")) {
							Node toInsert = doc.importNode(script.getFirstChild(), true);

							String op = script.getAttributes().item(0).getNodeValue();
							op = op.substring(1); // removing A or Tree Name

							Node temp = rootC;
							while (op.length() > 1) {
								temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
								op = op.substring(1);
							}
							temp.insertBefore(toInsert,
									temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));

						}
					}
				}
			}
			
			
			
			
			String absPath = WriteXMLtoFile(rootC, patchedName,false);
			
			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(absPath), tmpInput2.getAbsolutePath(),true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			
			if (!targetHash.equals(hashInput2)) {
				System.out.println("Wrong Result Expected: Hash checksum does not match");
			}
			else {
				System.out.println("Patch successful, hash checksum matches!");
			}
			
			return absPath;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
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
			if(!overwrite) {
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

	private static boolean containedIn(Node rootA, ArrayList<Node> TreesIn) {
		for (Node a : TreesIn) {
//			if(rootA.isEqualNode(a))
//				return true;
			if (containedIn2(rootA, a))
				return true;
		}
		return false;
	}
//
	public static boolean containedIn(Node rootA, Node rootB) {
		getTreesInB(rootB);
		return containedIn(rootA, TreesInB);
	}

	private static boolean containedIn2(Node rootA, Node rootB) {
		if(!rootA.getNodeName().equals(rootB.getNodeName()))
			return false;
		
		if(!rootA.hasChildNodes())
			return rootA.isEqualNode(rootB);
		
		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();
		boolean flag1 = true;
		for(int i = 0; i<listA.getLength();i++) {
			boolean flag = false;
			for(int j = 0; j<listB.getLength();j++) {
				if(listA.item(i).getNodeName().equals(listB.item(j).getNodeName()))
					flag = flag || containedIn2(listA.item(i),listB.item(j)); 
				if(flag)
					break;
			}
			flag1 = flag1 && flag;
			if(!flag1)
				return false;
		}
		return flag1;
	}


	private static void getTreesInA(Node node) {
		TreesInA.add(node);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			clean(list.item(i));
			getTreesInA(list.item(i));
		}
	}

	private static void getTreesInB(Node node) {
		TreesInB.add(node);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			clean(list.item(i));
			getTreesInB(list.item(i));
		}
	}

	private static int CostInsertTree(Node rootB) {
		if (rootB.getNodeType() == Node.ELEMENT_NODE) {
			if (containedIn(rootB, TreesInA)) {
				return insertContained;
			}
			return deleteOrInsertLeaf + ((Element) rootB).getElementsByTagName("*").getLength();
		}
		return deleteOrInsertLeaf;
	}

	private static int CostDeleteTree(Node rootA) {
		if (rootA.getNodeType() == Node.ELEMENT_NODE) {
			if (containedIn(rootA, TreesInB)) {
				return deleteContained;
			}
			return deleteOrInsertLeaf + ((Element) rootA).getElementsByTagName("*").getLength();
		}
		return deleteOrInsertLeaf;
	}

	private static int CostUpdateRoot(Node rootA, Node rootB) {
		if (rootA.getNodeName().equals(rootB.getNodeName()))
			return 0;
		else
			return updateRootCost;
	}

	private static Node applyPatch(ArrayList<String> ES, Node rootA, Node rootB) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(rootA, true);
			doc.appendChild(rootC);

			for (String es : ES) {
				Scanner scan = new Scanner(es);
				scan.next(); // removing "{ "
				String ins = scan.next();
				if (ins.equals("Update")) {
					String op1 = scan.next();
					String op2 = scan.next();
					op1 = op1.substring(3);
					op2 = op2.substring(3);

					Node rc = doc.getDocumentElement();

					while (op1.length() > 1) {
						rc = rc.getChildNodes().item(Integer.parseInt("" + op1.charAt(0)) - 1);
						op1 = op1.substring(1);
					}
					Node rb = rootB;
					while (op2.length() > 1) {
						rb = rb.getChildNodes().item(Integer.parseInt("" + op2.charAt(0)) - 1);
						op2 = op2.substring(1);
					}
					doc.renameNode(rc, rb.getNamespaceURI(), rb.getNodeName());

				}
			}
			for (String es : ES) {
				Scanner scan = new Scanner(es);
				scan.next(); // removing "{ "
				String ins = scan.next();

				if (ins.equals("Delete")) {
					String op = scan.next();
					op = op.substring(1); // removing A or Tree Name
					Node temp = rootC;
					while (op.length() > 1) {
						temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
					temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));
				}
			}

			for (String es : ES) {
				Scanner scan = new Scanner(es);
				scan.next(); // removing "{ "
				String ins = scan.next();

				if (ins.equals("Insert")) {
					String op = scan.next();
					op = op.substring(1); // removing A or Tree Name
					Node toInsert = doc.importNode(rootB, true);
					String opc = op;
					while (opc.length() > 0) {
						toInsert = toInsert.getChildNodes().item(Integer.parseInt("" + opc.charAt(0)) - 1);
						opc = opc.substring(1);
					}
					Node temp = rootC;
					while (op.length() > 1) {
						temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					temp.insertBefore(toInsert, temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));

				}

			}
			return rootC;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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

	public static long checksumBufferedInputStream(File file) throws IOException {
		
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

		CRC32 crc = new CRC32();

		int cnt;

		while ((cnt = inputStream.read()) != -1) {
			crc.update(cnt);
		}
		inputStream.close();
		return crc.getValue();
	}
	
	private static String getAlphaNumericString(int n) 
    { 
  
        // chose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz"; 
  
        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n); 
  
        for (int i = 0; i < n; i++) { 
  
            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index 
                = (int)(AlphaNumericString.length() 
                        * Math.random()); 
  
            // add Character one by one in end of sb 
            sb.append(AlphaNumericString 
                          .charAt(index)); 
        } 
  
        return sb.toString(); 
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

}
