package com.georgioyammine.classes;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.zip.CRC32;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class XMLDiffAndPatch extends Observable {
	public static int updateRootName = 1;
	public static int insertContained = 1;
	public static int deleteContained = 1;
	public static int deleteOrInsertLeaf = 1; // 3
	public static int attributeNameCost = 1; // 2
	public static int attributeValueCost = 1;
	public static int contentTokenCost = 1;

	private static HashMap<Node, Integer> costsOfTrees = new HashMap<>();
	private static HashMap<Node, Integer> costsOfTreesBinA = new HashMap<>();
	private static HashMap<Node, Integer> costsOfTreesAinB = new HashMap<>();

	private static ArrayList<Node> TreesInA = new ArrayList<>();
	private static ArrayList<Node> TreesInB = new ArrayList<>();

	private static double version = 1.0;

	private static final DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
	private static final DoubleProperty progressPropertyReverse = new SimpleDoubleProperty(0.0);
	private static final DoubleProperty progressPropertyPatch = new SimpleDoubleProperty(0.0);

	public static DoubleProperty getProgressProperty() {
	        return progressProperty;
	}
	public static DoubleProperty getProgressReverseProperty() {
        return progressPropertyReverse;
	}
	public static DoubleProperty getProgressPatchProperty() {
        return progressPropertyPatch;
	}

	private static class Info7 {
		int x, y, nx, ny, z;
		String a, b;

		public Info7(String A, String B, int x, int y, int nx, int ny, int z) {
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

	private static class Info5 {
		int x, y, nx, ny, z;

		public Info5(int x, int y, int nx, int ny, int z) {
			this.x = x;
			this.y = y;
			this.nx = nx;
			this.ny = ny;
			this.z = z;
		}

		public String toString() {
			return "[" + x + ", " + y + ", " + nx + ", " + ny + ", " + z + "]";
		}
	}

	public static ArrayList<Object> TED(Node rootA, Node rootB, String R1, String R2, boolean print) {
		//returns [ES, dist, sim]

		if (rootA.isEqualNode(rootB)) {
			ArrayList<Object> arl = new ArrayList<>();
			arl.add(0);
			arl.add(new ArrayList<Object>());
			arl.add(1.0);
			return arl;
		}

		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();

		int m = listA.getLength();
		int n = listB.getLength();

		if (print) {
			System.out.println(m + " " + n);
		}

		int[][] dist = new int[m + 1][n + 1];
		@SuppressWarnings("unchecked")
		ArrayList<Object>[][] pointers = new ArrayList[m + 1][n + 1];
		ArrayList<Object> results = new ArrayList<>();
		// results.add(dist);
		// results.add(pointers);

		ArrayList<Object> updateRoot = CostUpdateRoot(rootA, rootB);
		dist[0][0] = (int) updateRoot.remove(0);

		pointers[0][0] = new ArrayList<>();
		pointers[0][0].add(new Info7(R1, R2, -1, -1, 0, 0, dist[0][0]));
		pointers[0][0].add(updateRoot);
		pointers[0][0].add("");

		for (int i = 1; i <= m; i++) {
			dist[i][0] = dist[i - 1][0] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
			ArrayList<Object> del = new ArrayList<>();
			del.add(new Info7(R1, R2, i - 1, 0, i, 0, dist[i][0]));
			pointers[i][0] = del;
		}

		if (print) {
//			System.out.println("Step 1 Done");
			progressProperty.set(0.1);
		}
		for (int j = 1; j <= n; j++) {
			dist[0][j] = dist[0][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
			ArrayList<Object> ins = new ArrayList<>();
			ins.add(new Info7(R1, R2, 0, j - 1, 0, j, dist[0][j]));
			pointers[0][j] = ins;
		}
		if (print) {
//			System.out.println("Step 2 Done");
			progressProperty.set(0.15);
		}
		for (int i = 1; i <= m; i++) {

			if (print) {
				System.out.println("Progress: " + i + "/" + m);
				progressProperty.set(0.15+0.75*(i/(m+0.0)));
			}
			for (int j = 1; j <= n; j++) {
				if (print && n>=1000 && j % 100 == 0)
					System.out.println("\tProgress: " + i + "/" + m + "- " + j + "/" + n);

				if (listA.item(i - 1).isEqualNode(listB.item(j - 1)) && (dist[i - 1][j - 1] <= dist[i][j - 1])
						&& dist[i - 1][j - 1] <= dist[i - 1][j]) {
					dist[i][j] = dist[i - 1][j - 1];
					ArrayList<Object> upd = new ArrayList<>();
					upd.add(new Info7(R1, R2, i - 1, j - 1, i, j, dist[i][j]));
					upd.add(new ArrayList<Object>());

					if (listA.item(i - 1).getNodeType() == Node.TEXT_NODE) {
						upd.add(true);
						upd.add(true);
					}

					pointers[i][j] = upd;

				} else {
					int update = Integer.MAX_VALUE / 2;
					boolean text = false;
					ArrayList<Object> updateInfo = new ArrayList<>();
					int delete = dist[i - 1][j] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
					int insert;
					if (delete <= dist[i - 1][j - 1]) {
						if (delete <= dist[i][j - 1]) {
							dist[i][j] = delete;
							ArrayList<Object> del = new ArrayList<>();
							del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
							pointers[i][j] = del;
						} else {
							insert = dist[i][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));

							if (insert < delete) {
								dist[i][j] = insert;
								ArrayList<Object> ins = new ArrayList<>();
								ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
								pointers[i][j] = ins;
							} else {
								dist[i][j] = delete;
								ArrayList<Object> del = new ArrayList<>();
								del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
								pointers[i][j] = del;
							}
						}
					} else {
						// update = Integer.MAX_VALUE;
						insert = dist[i][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
						if (insert <= dist[i - 1][j - 1]) {
							// no need for updating
							if (insert < delete) {
								dist[i][j] = insert;
								ArrayList<Object> ins = new ArrayList<>();
								ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
								pointers[i][j] = ins;

							} else {
								dist[i][j] = delete;
								ArrayList<Object> del = new ArrayList<>();
								del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
								pointers[i][j] = del;
							}
						} else {
							// checking if update is needed
							if (listA.item(i - 1).getNodeType() == listB.item(j - 1).getNodeType()) {
								// update is possible
								if (listA.item(i - 1).getNodeType() == Node.TEXT_NODE) {
									updateInfo = EDStrings(listA.item(i - 1).getTextContent().split("\\s+"),
											listB.item(j - 1).getTextContent().split("\\s+"), false);
									text = true;

								} else {

									updateInfo = TED(listA.item(i - 1), listB.item(j - 1), R1 + "." + i, R2 + "." + j,
											false);
								}
								update = dist[i - 1][j - 1] + (int) updateInfo.get(0);

								// compare all of them

								if (update < insert) {
									if (update < delete) {
										dist[i][j] = update;
										ArrayList<Object> upd = new ArrayList<>();
										upd.add(new Info7(R1, R2, i - 1, j - 1, i, j, dist[i][j]));
										upd.add(updateInfo.get(1));
										if (text) {
											upd.add(text);
											upd.add(text);
										}
										pointers[i][j] = upd;
									} else {
										// check inset and delete
										if (insert < delete) {
											dist[i][j] = insert;
											ArrayList<Object> ins = new ArrayList<>();
											ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
											pointers[i][j] = ins;

										} else {
											dist[i][j] = delete;
											ArrayList<Object> del = new ArrayList<>();
											del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
											pointers[i][j] = del;
										}
									}
								} else {
									// check inset and delete
									if (insert < delete) {
										dist[i][j] = insert;
										ArrayList<Object> ins = new ArrayList<>();
										ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
										pointers[i][j] = ins;

									} else {
										dist[i][j] = delete;
										ArrayList<Object> del = new ArrayList<>();
										del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
										pointers[i][j] = del;
									}
								}

							} else {
								insert = dist[i][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
								if (insert < delete) {
									dist[i][j] = insert;
									ArrayList<Object> ins = new ArrayList<>();
									ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
									pointers[i][j] = ins;

								} else {
									dist[i][j] = delete;
									ArrayList<Object> del = new ArrayList<>();
									del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
									pointers[i][j] = del;
								}
							}
						}
					}
				}
			}
		}
//		if (print && m < 20 && n <20) {
//			System.out.println(R1 + ":" + rootA + " " + R2 + ":" + rootB);
//			for (int i = 0; i <= m; i++) {
//				for (int j = 0; j <= n; j++) {
//					System.out.print(dist[i][j] + "\t");
//				}
//				System.out.println();
//			}
//			System.out.println();
//		}

		ArrayList<Object> ES = getEditScript(m, n, pointers, dist, R1, R2);
		results.add(0, ES);
		results.add(0, dist[m][n]);
		results.add(2, (dist[m][0] + dist[0][n] - dist[m][n]) / (0.0 + dist[m][0] + dist[0][n]));
		// results.add(2,fes);
		return results;
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Object> getEditScript(int m, int n, ArrayList<Object>[][] pointers, int[][] dist,
			String r1, String r2) {
		ArrayList<Object> reversedEditScript = new ArrayList<>();

		int a = m, b = n;
		while (a > -1 && b > -1 && ((Info7) (pointers[a][b].get(0))).z != 0) {
			ArrayList<Object> prev = pointers[a][b];
			if (prev.size() > 1) {
				if (prev.size() > 2) {
					// adding info of update root
					if (!((ArrayList<Object>) prev.get(1)).isEmpty()) {
						reversedEditScript.add(prev.get(1));
						reversedEditScript.add((Info7) prev.get(0));
					}

				} else {
					if (prev.size() > 3) {
						if (!((ArrayList<Object>) prev.get(1)).isEmpty()) {
							reversedEditScript.addAll((ArrayList<Object>) prev.get(1));
							reversedEditScript.add((Info7) prev.get(0));
						}
					} else {
						// adding info of recursive call
						reversedEditScript.addAll((ArrayList<Object>) prev.get(1));
					}
				}
			} else
				reversedEditScript.add((Info7) prev.get(0));

			a = (int) ((Info7) prev.get(0)).x;
			b = (int) ((Info7) prev.get(0)).y;

		}
		// Collections.reverse(reversedEditScript);
		return reversedEditScript;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> TEDandEditScript(String fileName1, String fileName2, boolean reversible)
			throws Exception {
		costsOfTrees     = new HashMap<>();
		costsOfTreesBinA = new HashMap<>();
		costsOfTreesAinB = new HashMap<>();

		TreesInA = new ArrayList<>();
		TreesInB = new ArrayList<>();

		progressProperty.set(0.0);
		File file = new File(fileName1);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(file);
//		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();

		File file2 = new File(fileName2);
		DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
		dbf2.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db2 = dbf2.newDocumentBuilder();
		Document document2 = db2.parse(file2);
//		document2.getDocumentElement().normalize();
		Element root2 = document2.getDocumentElement();

		clean(root);
		clean(root2);

		getTreesInA(root); // pre-processing
		getTreesInB(root2); // pre-processing
		progressProperty.set(0.05);
		System.out.println("Started TED");
		ArrayList<Object> ted = XMLDiffAndPatch.TED(root, root2, "A", "B", true);

		int distance = (Integer) ted.get(0);
		Collections.reverse((List<Object>) ted.get(1));
		double similarity = ((int) (10000 * (double) ted.get(2)) / 100.0);// 2 decimal places percentage
		String patchFIleName;
		if (!reversible)
			patchFIleName = editScriptToXML((ArrayList<ArrayList<Object>>) ted.get(1), file, file2, root, root2, distance, similarity);
		else
			patchFIleName = reversibleEditScriptToXML((ArrayList<ArrayList<Object>>) ted.get(1), file, file2, root, root2, distance,
					similarity);
		progressProperty.set(1);
		ArrayList<String> arl = new ArrayList<>();
		arl.add(distance +"");
		arl.add(similarity+"");
		arl.add(patchFIleName);
		return arl;
	}


	@SuppressWarnings("unchecked")
	public static ArrayList<Object> getDiffNode(String fileName1, String fileName2, boolean reversible)
			throws Exception {
		costsOfTrees     = new HashMap<>();
		costsOfTreesBinA = new HashMap<>();
		costsOfTreesAinB = new HashMap<>();

		TreesInA = new ArrayList<>();
		TreesInB = new ArrayList<>();

		progressProperty.set(0.0);
		File file = new File(fileName1);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(file);
//		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();

		File file2 = new File(fileName2);
		DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
		dbf2.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db2 = dbf2.newDocumentBuilder();
		Document document2 = db2.parse(file2);
//		document2.getDocumentElement().normalize();
		Element root2 = document2.getDocumentElement();

		clean(root);
		clean(root2);

		getTreesInA(root); // pre-processing
		getTreesInB(root2); // pre-processing
		progressProperty.set(0.05);
		System.out.println("Started TED");
		ArrayList<Object> ted = XMLDiffAndPatch.TED(root, root2, "A", "B", true);

		int distance = (Integer) ted.get(0);
		Collections.reverse((List<Object>) ted.get(1));
		double similarity = ((int) (10000 * (double) ted.get(2)) / 100.0);// 2 decimal places percentage
//		String patchFIleName;
		Node node;
		if (!reversible)
			node = editScriptToXMLNode((ArrayList<ArrayList<Object>>) ted.get(1), file, file2, root, root2, distance, similarity);
		else
			node = reversibleEditScriptToXMLNode((ArrayList<ArrayList<Object>>) ted.get(1), file, file2, root, root2, distance,
					similarity);
		progressProperty.set(1);
		ArrayList<Object> arl = new ArrayList<>();
		arl.add(node);
		arl.add(ted.get(2));

		return arl;

	}

	private static boolean containedIn(Node node, ArrayList<Node> TreesIn) {
		for (Node a : TreesIn) {
			if (node.isEqualNode(a))
				return true;
		}
		return false;
	}

	// public static boolean containedIn(Node rootA, Node rootB) {
	// getTreesInB(rootB);
	// return containedIn(rootA, TreesInB);
	// }

	private static void getTreesInA(Node node) {
		TreesInA.add(node);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			// clean(list.item(i));
			getTreesInA(list.item(i));
		}
	}

	private static void getTreesInB(Node node) {
		TreesInB.add(node);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			// clean(list.item(i));
			getTreesInB(list.item(i));
		}
	}

	private static int CostInsertTree(Node rootB) {
		if (costsOfTrees.containsKey(rootB))
			return costsOfTrees.get(rootB);
		if (costsOfTreesBinA.containsKey(rootB))
			return costsOfTreesBinA.get(rootB);
		if (containedIn(rootB, TreesInA)) {
			costsOfTreesBinA.put(rootB, insertContained);
			return insertContained;
		}
		if (rootB.getNodeType() != Node.TEXT_NODE) {

			int cost = deleteOrInsertLeaf;
			NodeList childs = rootB.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				cost += CostDeleteTree(childs.item(i));

			// cost of inserting attributes
			cost += (attributeNameCost + attributeValueCost) * rootB.getAttributes().getLength();

			costsOfTrees.put(rootB, cost);
			return cost;
		} else {
			int cost = contentTokenCost * rootB.getTextContent().split("\\s+").length;
			costsOfTrees.put(rootB, cost);
			return cost;
		}
	}

	private static int CostDeleteTree(Node rootA) {
		if (costsOfTrees.containsKey(rootA))
			return costsOfTrees.get(rootA);
		if (costsOfTreesAinB.containsKey(rootA))
			return costsOfTreesAinB.get(rootA);
		if (containedIn(rootA, TreesInB)) {
			costsOfTreesAinB.put(rootA, deleteContained);
			return deleteContained;
		}
		if (rootA.getNodeType() != Node.TEXT_NODE) {

			int cost = deleteOrInsertLeaf;
			NodeList childs = rootA.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				cost += CostDeleteTree(childs.item(i));

			// cost of inserting attributes
			cost += (attributeNameCost + attributeValueCost) * rootA.getAttributes().getLength();

			costsOfTrees.put(rootA, cost);
			return cost;
		} else {
			int cost = contentTokenCost * rootA.getTextContent().split("\\s+").length;
			costsOfTrees.put(rootA, cost);
			return cost;
		}
	}

	// private static int CostInsertTree(Node rootB) {
	// if (costsOfTrees.containsKey(rootB))
	// return costsOfTrees.get(rootB);
	//// if (costsOfTreesBinA.containsKey(rootB))
	//// return costsOfTreesBinA.get(rootB);
	// if (containedIn(rootB, TreesInA)) {
	//// costsOfTreesBinA.put(rootB, insertContained);
	// return insertContained;
	// }
	// if (rootB.getNodeType() != Node.TEXT_NODE) {
	// int cost = deleteOrInsertLeaf;
	// NodeList childs = rootB.getChildNodes();
	// for (int i = 0; i < childs.getLength(); i++)
	// cost += CostDeleteTree(childs.item(i));
	//
	// // cost of inserting attributes
	// cost += (attributeNameCost + attributeValueCost) *
	// rootB.getAttributes().getLength();
	//
	// costsOfTrees.put(rootB, cost);
	// return cost;
	// } else {
	// int cost = contentTokenCost * rootB.getTextContent().split("\\s+").length;
	//// costsOfTrees.put(rootB, cost);
	// return cost;
	// }
	// }
	//
	// private static int CostDeleteTree(Node rootA) {
	// if (costsOfTrees.containsKey(rootA))
	// return costsOfTrees.get(rootA);
	//// if (costsOfTreesAinB.containsKey(rootA))
	//// return costsOfTreesAinB.get(rootA);
	// if (containedIn(rootA, TreesInB)) {
	//// costsOfTreesAinB.put(rootA, deleteContained);
	// return deleteContained;
	// }
	// if (rootA.getNodeType() != Node.TEXT_NODE) {
	// int cost = deleteOrInsertLeaf;
	// NodeList childs = rootA.getChildNodes();
	// for (int i = 0; i < childs.getLength(); i++)
	// cost += CostDeleteTree(childs.item(i));
	//
	// // cost of inserting attributes
	// cost += (attributeNameCost + attributeValueCost) *
	// rootA.getAttributes().getLength();
	//
	// costsOfTrees.put(rootA, cost);
	// return cost;
	// } else {
	// int cost = contentTokenCost * rootA.getTextContent().split("\\s+").length;
	//// costsOfTrees.put(rootA, cost);
	// return cost;
	// }
	// }

	private static ArrayList<Object> CostUpdateRoot(Node rootA, Node rootB) {
		ArrayList<Object> update = new ArrayList<>();

		int cost = 0;

		// update root name
		if (!rootA.getNodeName().equals(rootB.getNodeName())) {
			cost += updateRootName;
			update.add(1);
		} else
			update.add(0);

		// update attributes
		ArrayList<Object> attr = EDAttr(Util.getArlFromNNM(rootA.getAttributes()),
				Util.getArlFromNNM(rootB.getAttributes()), false);
		cost += (int) (attr.get(0));
		update.add(attr.get(1));

		update.add(0, cost); // cost (int) - 1 or 0 (int) - ES attr (arrayList)

		return update;
	}

	public static ArrayList<Object> EDAttr(ArrayList<Node> attrA, ArrayList<Node> attrB, boolean print) {
		reorder(attrA, attrB);

		ArrayList<Object> arl = new ArrayList<>();
		int[][] distance = new int[attrA.size() + 1][attrB.size() + 1];
		distance[0][0] = 0;
		Info5[][] pointers = new Info5[attrA.size() + 1][attrB.size() + 1];
		pointers[0][0] = new Info5(-1, -1, 0, 0, 0);

		int insert = Integer.MAX_VALUE;
		int delete = Integer.MAX_VALUE;
		int update = Integer.MAX_VALUE;

		for (int i = 1; i <= attrA.size(); i++) {
			distance[i][0] = distance[i - 1][0] + attributeNameCost + attributeValueCost;
			pointers[i][0] = new Info5(i - 1, 0, i, 0, distance[i][0]);
		}
		for (int j = 1; j <= attrB.size(); j++) {
			distance[0][j] = distance[0][j - 1] + attributeNameCost + attributeValueCost;
			pointers[0][j] = new Info5(0, j - 1, 0, j, distance[0][j]);
		}
		for (int i = 1; i <= attrA.size(); i++) {
			for (int j = 1; j <= attrB.size(); j++) {
				update = distance[i - 1][j - 1] + CostUpdateAttr(attrA.get(i - 1), attrB.get(j - 1), attrA);
				if (update <= distance[i - 1][j]) {
					if (update <= distance[i][j - 1]) {
						distance[i][j] = update;
						pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
					} else {
						insert = distance[i][j - 1] + attributeNameCost + attributeValueCost;

						if (insert < update) {
							distance[i][j] = insert;
							pointers[i][j] = new Info5(i, j - 1, i, j, insert);
						} else {
							distance[i][j] = update;
							pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
						}
					}

				} else {
					delete = distance[i - 1][j] + attributeNameCost + attributeValueCost;
					if (delete <= distance[i][j - 1]) {
						if (delete <= update) {
							distance[i][j] = delete;
							pointers[i][j] = new Info5(i - 1, j, i, j, delete);
						} else {
							distance[i][j] = update;
							pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
						}
					} else {
						insert = distance[i][j - 1] + attributeNameCost + attributeValueCost;

						if (insert < update) {
							if (insert < delete) {
								distance[i][j] = insert;
								pointers[i][j] = new Info5(i, j - 1, i, j, insert);
							} else {
								distance[i][j] = delete;
								pointers[i][j] = new Info5(i - 1, j, i, j, delete);
							}
						} else {
							if (delete < update) {
								distance[i][j] = delete;
								pointers[i][j] = new Info5(i - 1, j, i, j, delete);
							} else {
								distance[i][j] = update;
								pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
							}
						}
					}
				}

			}
		}
		if (print) {
			System.out.println(attrA);
			System.out.println(attrB);
			for (int i = 0; i <= attrA.size(); i++) {
				for (int j = 0; j <= attrB.size(); j++)
					System.out.print(distance[i][j] + " ");
				System.out.println();
			}
		}
		arl.add(distance[attrA.size()][attrB.size()]);
		// arl.add(pointers);
		arl.add(getESfromEDNodeOrAtt(pointers));
		// return distance[attrA.getLength()][attrB.getLength()];
		return arl;
	}

	private static void reorder(ArrayList<Node> listA, ArrayList<Node> listB) {
		ArrayList<Node> newListA = new ArrayList<>();
		ArrayList<Node> newListB = new ArrayList<>();
		for (int i = 0; i < listA.size();) {
			int j = Util.contains(listA.get(i), listB);
			if (j != -1) {
				newListA.add(listA.get(i));
				newListB.add(listB.get(j));
				listA.remove(i);
				listB.remove(j);
			} else
				i++;
		}
		while (listA.size() > 0)
			newListA.add(listA.remove(0));

		while (listB.size() > 0)
			newListB.add(listB.remove(0));

		listA.addAll(newListA);
		listB.addAll(newListB);
	}

	private static Info5 getUpdateAttrScript(Info5 token, ArrayList<Node> attrA, ArrayList<Node> attrB) {
		int x = token.z;
		Node a = attrA.get(token.nx - 1);
		Node b = attrB.get(token.ny - 1);

		Info5 upd = new Info5(-2, -2, token.nx, token.ny, token.z);
		if (!a.getNodeName().equals(b.getNodeName())) {
			x--;
			upd.x = -5;
		}
		if (!a.getNodeValue().equals(b.getNodeValue() + "")) {
			x--;
			upd.y = -5;
		}
		if (x < 0)
			System.out.println("    !Expected error in getUpdateAttrScript!!!!  ");
		return upd;
	}

	private static ArrayList<Object> EDStrings(String[] rootAContent, String[] rootBContent, boolean print) {
		if (rootAContent.equals(rootBContent)) {
			ArrayList<Object> arl = new ArrayList<>();
			arl.add(0);
			arl.add(new ArrayList<>());
			return arl;
		}
		ArrayList<Object> arl = new ArrayList<>();
		int[][] distance = new int[rootAContent.length + 1][rootBContent.length + 1];
		distance[0][0] = 0;
		Info5[][] pointers = new Info5[rootAContent.length + 1][rootBContent.length + 1];
		pointers[0][0] = new Info5(-1, -1, 0, 0, 0);

		int delete, insert, update;
		for (int i = 1; i <= rootAContent.length; i++) {
			distance[i][0] = distance[i - 1][0] + contentTokenCost;
			pointers[i][0] = new Info5(i - 1, 0, i, 0, distance[i][0]);
		}
		for (int j = 1; j <= rootBContent.length; j++) {
			distance[0][j] = distance[0][j - 1] + contentTokenCost;
			pointers[0][j] = new Info5(0, j - 1, 0, j, distance[0][j]);
		}
		for (int i = 1; i <= rootAContent.length; i++) {
			for (int j = 1; j <= rootBContent.length; j++) {
				update = distance[i - 1][j - 1] + CostUpdateContent(rootAContent[i - 1], rootBContent[j - 1]);

				if (update <= distance[i - 1][j]) {
					if (update <= distance[i][j - 1]) {
						distance[i][j] = update;
						pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
					} else {
						insert = distance[i][j - 1] + contentTokenCost;
						if (insert < update) {
							distance[i][j] = insert;
							pointers[i][j] = new Info5(i, j - 1, i, j, insert);
						} else {
							distance[i][j] = update;
							pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
						}
					}

				} else {
					delete = distance[i - 1][j] + contentTokenCost;
					if (delete <= distance[i][j - 1]) {
						if (delete <= update) {
							distance[i][j] = delete;
							pointers[i][j] = new Info5(i - 1, j, i, j, delete);
						} else {
							distance[i][j] = update;
							pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
						}
					} else {
						insert = distance[i - 1][j] + contentTokenCost;
						if (insert < update) {
							if (insert < delete) {
								distance[i][j] = insert;
								pointers[i][j] = new Info5(i, j - 1, i, j, insert);
							} else {
								distance[i][j] = delete;
								pointers[i][j] = new Info5(i - 1, j, i, j, delete);
							}
						} else {
							if (delete < update) {
								distance[i][j] = delete;
								pointers[i][j] = new Info5(i - 1, j, i, j, delete);
							} else {
								distance[i][j] = update;
								pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
							}
						}
					}
				}

			}
		}
		if (print) {
			System.out.println(Arrays.toString(rootAContent));
			System.out.println(Arrays.toString(rootBContent));
			for (int i = 0; i <= rootAContent.length; i++) {
				for (int j = 0; j <= rootBContent.length; j++)
					System.out.print(distance[i][j] + " ");
				System.out.println();
			}
		}
		// return distance (int), pointers [][], ES ArrayList
		arl.add(distance[rootAContent.length][rootBContent.length]);
		// arl.add(pointers);
		arl.add(getESfromEDNodeOrAtt(pointers));
		return arl;
	}

	private static ArrayList<Info5> getESfromEDNodeOrAtt(Info5[][] pointers) {
		ArrayList<Info5> ESContent = new ArrayList<>();

		Info5 token = pointers[pointers.length - 1][pointers[0].length - 1];
		while (token.z != 0) {
			ESContent.add(token);
			token = pointers[token.x][token.y];
		}
		Collections.reverse(ESContent);
		int prev = 0;
		Info5 elt;
		for (int i = 0; i < ESContent.size();) {
			elt = ESContent.get(i);
			if (elt.z == prev) {
				ESContent.remove(i);
			} else {
				int tmp = prev;
				prev = elt.z;
				elt.z -= tmp;
				i++;
			}
		}
		return ESContent;
	}

	private static int CostUpdateContent(String rootAContent, String rootBContent) {
		if (rootAContent.equals(rootBContent))
			return 0;
		else
			return contentTokenCost;
	}

	private static int CostUpdateAttr(Node attrA, Node attrB, ArrayList<Node> attrA2) {

		if (attrA.getNodeName().equals(attrB.getNodeName())) {
			if (attrA.getTextContent().equals(attrB.getTextContent()))
				return 0;
			else
				return attributeValueCost;
		} else {
			if (Util.contains(attrB, attrA2) != -1) {
				return Integer.MAX_VALUE / 2;
			}
			if (attrA.getTextContent().equals(attrB.getTextContent()))
				return attributeNameCost;
			else
				return attributeNameCost + attributeValueCost;
		}

	}

	@SuppressWarnings("rawtypes")
	private static Node editScriptToXMLNode(ArrayList<ArrayList<Object>> E, File original, File newFile, Element rootA,
			Element rootB, int distance, double similarity) throws Exception {
		try {
			ArrayList<ArrayList<Object>> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XML_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");
			root.setAttribute("similarity", similarity + "%");
			root.setAttribute("reversible", "no");
			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(), true,true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("crc32", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(), true,true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			file2.setAttribute("crc32", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			// Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			Element eltd;
			eltd = document.createElement("Delete");
			Element elti;
			elti = document.createElement("Insert");

			for (int i = 0; i < ES.size(); i++) {
				Object token = ES.get(i);
				if (token.getClass() == Info7.class) {

					if (((Info7) token).x == -1) {
						ArrayList<Object> nextToken = ES.get(i + 1);
						Element elt2 = document.createElement(((Info7) token).a + "");
						// elt2.setAttribute("type", "node");
						if (((int) nextToken.get(0)) == 1) {
							// updating Labels
							Element label = document.createElement("Label");

							String op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digits = op.split("\\.");

								for (String a : digits) {
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
								}
							}
							label.setTextContent(toUpdate.getNodeName());
							elt2.appendChild(label);

						}
						if (!((ArrayList) nextToken.get(1)).isEmpty()) {
							// updating attributes

							Element attributes = document.createElement("Attributes");

							@SuppressWarnings("unchecked")
							ArrayList<Info5> tokens = (ArrayList<Info5>) nextToken.get(1);

							// getting node Axxx...
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							ArrayList<Node> attrA = Util.getArlFromNNM(toUpdateA.getAttributes());
							ArrayList<Node> attrB = Util.getArlFromNNM(toUpdate.getAttributes());

							reorder(attrA, attrB);

							Element updAtt = document.createElement("Update_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x + 1 == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Info5 updates = getUpdateAttrScript(tokenA, attrA, attrB);
									Element upd = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									Attr change = document.createAttribute("change");

									if (updates.x == -5) {
										if (updates.y == -5) {
											change.setValue("both");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											Attr newValue = document.createAttribute("newValue");
											newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
											upd.setAttributeNode(newValue);
											upd.setAttributeNode(newKey);
										} else {
											change.setValue("key");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											upd.setAttributeNode(newKey);
										}
									} else {
										change.setValue("value");
										Attr newValue = document.createAttribute("newValue");
										newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
										upd.setAttributeNode(newValue);
									}
									upd.setAttributeNode(change);
									updAtt.appendChild(upd);

									tokens.remove(k);
								} else
									k++;
							}
							if (updAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(updAtt);

							Element delAtt = document.createElement("Delete_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);

								if (tokenA.x + 1 == tokenA.nx && tokenA.y == tokenA.ny) {
									Element toDel = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									delAtt.appendChild(toDel);
									tokens.remove(k);
								} else
									k++;
							}
							if (delAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(delAtt);

							Element insAtt = document.createElement("Insert_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Node toIns = document.createElement(attrB.get(tokenA.ny - 1).getNodeName());
									toIns.setTextContent(attrB.get(tokenA.ny - 1).getNodeValue());
									insAtt.appendChild(toIns);
									tokens.remove(k);
								} else
									k++;
							}
							if (insAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(insAtt);

							elt2.appendChild(attributes);
						}
						eltu.appendChild(elt2);
					} else {
						// getting node Axxx...
						if (((Info7) token).x + 1 == ((Info7) token).nx
								&& ((Info7) token).y + 1 == ((Info7) token).ny) {
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							toUpdateA = toUpdateA.getChildNodes().item(((Info7) token).nx - 1);

							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							toUpdate = toUpdate.getChildNodes().item(((Info7) token).ny - 1);

							if (toUpdate.getNodeType() == Node.TEXT_NODE && toUpdateA.getNodeType() == Node.TEXT_NODE) {
								// updating text nodes;
								String[] nodeBWords = toUpdate.getTextContent().split("\\s+");

								Element nodenametext = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx);
								// nodenametext.setAttribute("type", "textNode");
								ArrayList<Object> tokens = (ES.get(i + 1));

								Element updWord = document.createElement("Update_Word");
								for (int k = 0; k < tokens.size(); k++) {

									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										updWord.appendChild(elt2);

									}
								}
								if (updWord.hasChildNodes())
									nodenametext.appendChild(updWord);

								Element delWord = document.createElement("Delete_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										delWord.appendChild(elt2);

									}
								}
								if (delWord.hasChildNodes())
									nodenametext.appendChild(delWord);

								Element insWord = document.createElement("Insert_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.ny);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										insWord.appendChild(elt2);

									}
								}
								if (insWord.hasChildNodes())
									nodenametext.appendChild(insWord);

								if (nodenametext.hasChildNodes())
									eltu.appendChild(nodenametext);

							}
						}

						else {
							if (((Info7) token).x != -1 && ((Info7) token).x + 1 == ((Info7) token).nx
									&& ((Info7) token).y == ((Info7) token).ny) {
								Element elt2 = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx + "");
								eltd.appendChild(elt2);
							} else {

								if (((Info7) token).x != -1 && (((Info7) token).y + 1 == ((Info7) token).ny)
										&& (((Info7) token).x == ((Info7) token).nx)) {
									// inserting nodes or trees

									Element elt2;
									elt2 = document.createElement(
											"A" + (((Info7) token).b + "." + ((Info7) token).ny).substring(1));

									String opc = ((Info7) token).b + "." + ((Info7) token).ny + "";
									Node toInsert = document.importNode(rootB, true);
									if (opc.length() > 1) {
										opc = opc.substring(2); // removing B or Tree Name

										String[] digits = opc.split("\\.");

										for (String a : digits) {
											toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
										}
									}
									if (containedIn(toInsert, TreesInA)) {
										// get the location in A
										String location = getLocationIn(toInsert, rootA, "A");
										elt2.setAttribute("containedAT", location);
									} else
										elt2.appendChild(toInsert);
									elti.appendChild(elt2);

								}
							}

						}
					}
				}

			}
			if (eltu.hasChildNodes())
				es.appendChild(eltu);

			if (eltd.hasChildNodes())
				es.appendChild(eltd);

			if (elti.hasChildNodes())
				es.appendChild(elti);


			return root;
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return null;
		}

	}


	@SuppressWarnings("rawtypes")
	private static String editScriptToXML(ArrayList<ArrayList<Object>> E, File original, File newFile, Element rootA,
			Element rootB, int distance, double similarity) throws Exception {
		try {
			ArrayList<ArrayList<Object>> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XML_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");
			root.setAttribute("similarity", similarity + "%");
			root.setAttribute("reversible", "no");
			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(), true,true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("crc32", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(), true,true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			file2.setAttribute("crc32", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			// Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			Element eltd;
			eltd = document.createElement("Delete");
			Element elti;
			elti = document.createElement("Insert");

			for (int i = 0; i < ES.size(); i++) {
				Object token = ES.get(i);
				if (token.getClass() == Info7.class) {

					if (((Info7) token).x == -1) {
						ArrayList<Object> nextToken = ES.get(i + 1);
						Element elt2 = document.createElement(((Info7) token).a + "");
						// elt2.setAttribute("type", "node");
						if (((int) nextToken.get(0)) == 1) {
							// updating Labels
							Element label = document.createElement("Label");

							String op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digits = op.split("\\.");

								for (String a : digits) {
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
								}
							}
							label.setTextContent(toUpdate.getNodeName());
							elt2.appendChild(label);

						}
						if (!((ArrayList) nextToken.get(1)).isEmpty()) {
							// updating attributes

							Element attributes = document.createElement("Attributes");

							@SuppressWarnings("unchecked")
							ArrayList<Info5> tokens = (ArrayList<Info5>) nextToken.get(1);

							// getting node Axxx...
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							ArrayList<Node> attrA = Util.getArlFromNNM(toUpdateA.getAttributes());
							ArrayList<Node> attrB = Util.getArlFromNNM(toUpdate.getAttributes());

							reorder(attrA, attrB);

							Element updAtt = document.createElement("Update_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x + 1 == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Info5 updates = getUpdateAttrScript(tokenA, attrA, attrB);
									Element upd = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									Attr change = document.createAttribute("change");

									if (updates.x == -5) {
										if (updates.y == -5) {
											change.setValue("both");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											Attr newValue = document.createAttribute("newValue");
											newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
											upd.setAttributeNode(newValue);
											upd.setAttributeNode(newKey);
										} else {
											change.setValue("key");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											upd.setAttributeNode(newKey);
										}
									} else {
										change.setValue("value");
										Attr newValue = document.createAttribute("newValue");
										newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
										upd.setAttributeNode(newValue);
									}
									upd.setAttributeNode(change);
									updAtt.appendChild(upd);

									tokens.remove(k);
								} else
									k++;
							}
							if (updAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(updAtt);

							Element delAtt = document.createElement("Delete_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);

								if (tokenA.x + 1 == tokenA.nx && tokenA.y == tokenA.ny) {
									Element toDel = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									delAtt.appendChild(toDel);
									tokens.remove(k);
								} else
									k++;
							}
							if (delAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(delAtt);

							Element insAtt = document.createElement("Insert_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Node toIns = document.createElement(attrB.get(tokenA.ny - 1).getNodeName());
									toIns.setTextContent(attrB.get(tokenA.ny - 1).getNodeValue());
									insAtt.appendChild(toIns);
									tokens.remove(k);
								} else
									k++;
							}
							if (insAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(insAtt);

							elt2.appendChild(attributes);
						}
						eltu.appendChild(elt2);
					} else {
						// getting node Axxx...
						if (((Info7) token).x + 1 == ((Info7) token).nx
								&& ((Info7) token).y + 1 == ((Info7) token).ny) {
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							toUpdateA = toUpdateA.getChildNodes().item(((Info7) token).nx - 1);

							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							toUpdate = toUpdate.getChildNodes().item(((Info7) token).ny - 1);

							if (toUpdate.getNodeType() == Node.TEXT_NODE && toUpdateA.getNodeType() == Node.TEXT_NODE) {
								// updating text nodes;
								String[] nodeBWords = toUpdate.getTextContent().split("\\s+");

								Element nodenametext = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx);
								// nodenametext.setAttribute("type", "textNode");
								ArrayList<Object> tokens = (ES.get(i + 1));

								Element updWord = document.createElement("Update_Word");
								for (int k = 0; k < tokens.size(); k++) {

									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										updWord.appendChild(elt2);

									}
								}
								if (updWord.hasChildNodes())
									nodenametext.appendChild(updWord);

								Element delWord = document.createElement("Delete_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										delWord.appendChild(elt2);

									}
								}
								if (delWord.hasChildNodes())
									nodenametext.appendChild(delWord);

								Element insWord = document.createElement("Insert_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.ny);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										insWord.appendChild(elt2);

									}
								}
								if (insWord.hasChildNodes())
									nodenametext.appendChild(insWord);

								if (nodenametext.hasChildNodes())
									eltu.appendChild(nodenametext);

							}
						}

						else {
							if (((Info7) token).x != -1 && ((Info7) token).x + 1 == ((Info7) token).nx
									&& ((Info7) token).y == ((Info7) token).ny) {
								Element elt2 = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx + "");
								eltd.appendChild(elt2);
							} else {

								if (((Info7) token).x != -1 && (((Info7) token).y + 1 == ((Info7) token).ny)
										&& (((Info7) token).x == ((Info7) token).nx)) {
									// inserting nodes or trees

									Element elt2;
									elt2 = document.createElement(
											"A" + (((Info7) token).b + "." + ((Info7) token).ny).substring(1));

									String opc = ((Info7) token).b + "." + ((Info7) token).ny + "";
									Node toInsert = document.importNode(rootB, true);
									if (opc.length() > 1) {
										opc = opc.substring(2); // removing B or Tree Name

										String[] digits = opc.split("\\.");

										for (String a : digits) {
											toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
										}
									}
									if (containedIn(toInsert, TreesInA)) {
										// get the location in A
										String location = getLocationIn(toInsert, rootA, "A");
										elt2.setAttribute("containedAT", location);
									} else
										elt2.appendChild(toInsert);
									elti.appendChild(elt2);

								}
							}

						}
					}
				}

			}
			if (eltu.hasChildNodes())
				es.appendChild(eltu);

			if (eltd.hasChildNodes())
				es.appendChild(eltd);

			if (elti.hasChildNodes())
				es.appendChild(elti);

			String fileName = "XDP."+version+"_Diff_" + original.getName() + "_to_" + newFile.getName() + ".xml";

			return WriteXMLtoFile(root, fileName, false,false);
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return "Error";
		}

	}

	@SuppressWarnings("rawtypes")
	private static String reversibleEditScriptToXML(ArrayList<ArrayList<Object>> E, File original, File newFile,
			Element rootA, Element rootB, int distance, double similarity) throws Exception {
		try {
			ArrayList<ArrayList<Object>> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XDP_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");
			root.setAttribute("similarity", similarity + "%");
			root.setAttribute("reversible", "yes");
			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(), true,true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("crc32", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(), true,true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			file2.setAttribute("crc32", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			// Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			Element eltd;
			eltd = document.createElement("Delete");
			Element elti;
			elti = document.createElement("Insert");

			for (int i = 0; i < ES.size(); i++) {
				Object token = ES.get(i);
				if (token.getClass() == Info7.class) {

					if (((Info7) token).x == -1) {
						ArrayList<Object> nextToken = ES.get(i + 1);
						Element elt2 = document.createElement(((Info7) token).a + "");
						elt2.setAttribute("with", ((Info7) token).b + "");

						// elt2.setAttribute("type", "node");
						if (((int) nextToken.get(0)) == 1) {
							// updating Labels
							Element label = document.createElement("Label");

							String op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digits = op.split("\\.");

								for (String a : digits) {
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
								}
							}
							op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// elt.setAttribute(token.a, toUpdate.getNodeName());
							label.setAttribute("oldValue", toUpdateA.getNodeName());
							label.setTextContent(toUpdate.getNodeName());
							elt2.appendChild(label);

						}
						if (!((ArrayList) nextToken.get(1)).isEmpty()) {
							// updating attributes

							Element attributes = document.createElement("Attributes");

							@SuppressWarnings("unchecked")
							ArrayList<Info5> tokens = (ArrayList<Info5>) nextToken.get(1);

							// getting node Axxx...
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digitsB = op.split("\\.");
								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							ArrayList<Node> attrA = Util.getArlFromNNM(toUpdateA.getAttributes());
							ArrayList<Node> attrB = Util.getArlFromNNM(toUpdate.getAttributes());

							reorder(attrA, attrB);

							Element updAtt = document.createElement("Update_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x + 1 == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Info5 updates = getUpdateAttrScript(tokenA, attrA, attrB);
									Element upd = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									Attr change = document.createAttribute("change");

									if (updates.x == -5) {
										if (updates.y == -5) {
											change.setValue("both");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											Attr newValue = document.createAttribute("newValue");
											newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
											upd.setAttributeNode(newValue);
											upd.setAttributeNode(newKey);
											// upd.setAttribute("oldKey", attrA.get(tokenA.nx - 1).getNodeName());
											upd.setAttribute("oldValue", attrA.get(tokenA.nx - 1).getNodeValue());
										} else {
											change.setValue("key");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											upd.setAttributeNode(newKey);
											// upd.setAttribute("oldKey", attrA.get(tokenA.nx - 1).getNodeName());
										}
									} else {
										change.setValue("value");
										Attr newValue = document.createAttribute("newValue");
										newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
										upd.setAttributeNode(newValue);
										upd.setAttribute("oldValue", attrA.get(tokenA.nx - 1).getNodeValue());
									}
									upd.setAttributeNode(change);
									updAtt.appendChild(upd);
									tokens.remove(k);
								} else
									k++;
							}
							if (updAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(updAtt);

							Element delAtt = document.createElement("Delete_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);

								if (tokenA.x + 1 == tokenA.nx && tokenA.y == tokenA.ny) {
									Element toDel = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									toDel.setTextContent(attrA.get(tokenA.nx - 1).getNodeValue());
									delAtt.appendChild(toDel);
									tokens.remove(k);
								} else
									k++;
							}
							if (delAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(delAtt);

							Element insAtt = document.createElement("Insert_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Node toIns = document.createElement(attrB.get(tokenA.ny - 1).getNodeName());
									toIns.setTextContent(attrB.get(tokenA.ny - 1).getNodeValue());
									insAtt.appendChild(toIns);
									tokens.remove(k);
								} else
									k++;
							}
							if (insAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(insAtt);

							elt2.appendChild(attributes);
						}
						eltu.appendChild(elt2);
					} else {

						if (((Info7) token).x + 1 == ((Info7) token).nx
								&& ((Info7) token).y + 1 == ((Info7) token).ny) {
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							toUpdateA = toUpdateA.getChildNodes().item(((Info7) token).nx - 1);

							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							toUpdate = toUpdate.getChildNodes().item(((Info7) token).ny - 1);

							if (toUpdate.getNodeType() == Node.TEXT_NODE && toUpdateA.getNodeType() == Node.TEXT_NODE) {
								// updating text nodes;
								String[] nodeBWords = toUpdate.getTextContent().split("\\s+");
								String[] nodeAWords = toUpdateA.getTextContent().split("\\s+");
								Element nodenametext = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx);
								// nodenametext.setAttribute("type", "textNode");
								nodenametext.setAttribute("with", ((Info7) token).b + "." + ((Info7) token).ny);

								ArrayList<Object> tokens = (ES.get(i + 1));

								Element updWord = document.createElement("Update_Word");
								for (int k = 0; k < tokens.size(); k++) {

									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setAttribute("with", "w" + textToken.ny);
										elt2.setAttribute("oldValue", nodeAWords[textToken.nx - 1]);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										updWord.appendChild(elt2);

									}
								}
								if (updWord.hasChildNodes())
									nodenametext.appendChild(updWord);

								Element delWord = document.createElement("Delete_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setTextContent(nodeAWords[textToken.nx - 1]);
										delWord.appendChild(elt2);

									}
								}
								if (delWord.hasChildNodes())
									nodenametext.appendChild(delWord);

								Element insWord = document.createElement("Insert_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.ny);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										insWord.appendChild(elt2);

									}
								}
								if (insWord.hasChildNodes())
									nodenametext.appendChild(insWord);

								if (nodenametext.hasChildNodes())
									eltu.appendChild(nodenametext);

							}
						}

						else {
							// delete
							if (((Info7) token).x != -1 && ((Info7) token).x + 1 == ((Info7) token).nx
									&& ((Info7) token).y == ((Info7) token).ny) {

								Element elt2 = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx + "");

								String opc = ((Info7) token).a + "." + ((Info7) token).nx + "";
								Node toInsert = document.importNode(rootA, true);
								if (opc.length() > 1) {
									opc = opc.substring(2); // removing A or Tree Name

									String[] digits = opc.split("\\.");

									for (String a : digits) {
										toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
									}
								}
								if (containedIn(toInsert, TreesInB)) {
									// get the location in A
									String location = getLocationIn(toInsert, rootB, "B");
									elt2.setAttribute("containedAT", location);
								} else
									elt2.appendChild(toInsert);

								eltd.appendChild(elt2);

							} else {
								// insert
								if (((Info7) token).x != -1 && (((Info7) token).y + 1 == ((Info7) token).ny)
										&& (((Info7) token).x == ((Info7) token).nx)) {
									// inserting nodes or trees

									Element elt2;
									elt2 = document.createElement(
											"A" + (((Info7) token).b + "." + ((Info7) token).ny).substring(1));

									String opc = ((Info7) token).b + "." + ((Info7) token).ny + "";
									Node toInsert = document.importNode(rootB, true);
									if (opc.length() > 1) {
										opc = opc.substring(2); // removing B or Tree Name

										String[] digits = opc.split("\\.");

										for (String a : digits) {
											toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
										}
									}
									if (containedIn(toInsert, TreesInA)) {
										// get the location in A
										String location = getLocationIn(toInsert, rootA, "A");
										elt2.setAttribute("containedAT", location);
									} else
										elt2.appendChild(toInsert);
									elti.appendChild(elt2);

								}
							}

						}
					}
				}

			}
			if (eltu.hasChildNodes())
				es.appendChild(eltu);

			if (eltd.hasChildNodes())
				es.appendChild(eltd);

			if (elti.hasChildNodes())
				es.appendChild(elti);

			String fileName = "XDP."+version+"_Diff_Reversible_" + original.getName() + "_to_" + newFile.getName() + ".xml";

			return WriteXMLtoFile(root, fileName, false,false);
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return "Error";
		}

	}

	@SuppressWarnings("rawtypes")
	private static Node reversibleEditScriptToXMLNode(ArrayList<ArrayList<Object>> E, File original, File newFile,
			Element rootA, Element rootB, int distance, double similarity) throws Exception {
		try {
			ArrayList<ArrayList<Object>> ES = new ArrayList<>(E);

			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("XDP_Patch");
			document.appendChild(root);
			root.setAttribute("version", "" + version);
			root.setAttribute("distance", distance + "");
			root.setAttribute("similarity", similarity + "%");
			root.setAttribute("reversible", "yes");
			Element file1 = document.createElement("Original_File");
			file1.setAttribute("name", original.getName());
			File tmpInput = File.createTempFile("nnj", ".tmp");
			tmpInput.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(), true,true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("crc32", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(), true,true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			file2.setAttribute("crc32", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

			// Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			Element eltd;
			eltd = document.createElement("Delete");
			Element elti;
			elti = document.createElement("Insert");

			for (int i = 0; i < ES.size(); i++) {
				Object token = ES.get(i);
				if (token.getClass() == Info7.class) {

					if (((Info7) token).x == -1) {
						ArrayList<Object> nextToken = ES.get(i + 1);
						Element elt2 = document.createElement(((Info7) token).a + "");
						elt2.setAttribute("with", ((Info7) token).b + "");
						elt2.setAttribute("type", "Element_Node");

						// elt2.setAttribute("type", "node");
						if (((int) nextToken.get(0)) == 1) {
							// updating Labels
							Element label = document.createElement("Label");

							String op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digits = op.split("\\.");

								for (String a : digits) {
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
								}
							}
							op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// elt.setAttribute(token.a, toUpdate.getNodeName());
							label.setAttribute("oldValue", toUpdateA.getNodeName());
							label.setTextContent(toUpdate.getNodeName());
							elt2.appendChild(label);

						}
						if (!((ArrayList) nextToken.get(1)).isEmpty()) {
							// updating attributes

							Element attributes = document.createElement("Attributes");

							@SuppressWarnings("unchecked")
							ArrayList<Info5> tokens = (ArrayList<Info5>) nextToken.get(1);

							// getting node Axxx...
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing A or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name
								String[] digitsB = op.split("\\.");
								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							ArrayList<Node> attrA = Util.getArlFromNNM(toUpdateA.getAttributes());
							ArrayList<Node> attrB = Util.getArlFromNNM(toUpdate.getAttributes());

							reorder(attrA, attrB);

							Element updAtt = document.createElement("Update_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x + 1 == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Info5 updates = getUpdateAttrScript(tokenA, attrA, attrB);
									Element upd = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									Attr change = document.createAttribute("change");

									if (updates.x == -5) {
										if (updates.y == -5) {
											change.setValue("both");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											Attr newValue = document.createAttribute("newValue");
											newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
											upd.setAttributeNode(newValue);
											upd.setAttributeNode(newKey);
											// upd.setAttribute("oldKey", attrA.get(tokenA.nx - 1).getNodeName());
											upd.setAttribute("oldValue", attrA.get(tokenA.nx - 1).getNodeValue());
										} else {
											change.setValue("key");
											Attr newKey = document.createAttribute("newKey");
											newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
											upd.setAttributeNode(newKey);
											// upd.setAttribute("oldKey", attrA.get(tokenA.nx - 1).getNodeName());
										}
									} else {
										change.setValue("value");
										Attr newValue = document.createAttribute("newValue");
										newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
										upd.setAttributeNode(newValue);
										upd.setAttribute("oldValue", attrA.get(tokenA.nx - 1).getNodeValue());
									}
									upd.setAttributeNode(change);
									updAtt.appendChild(upd);
									tokens.remove(k);
								} else
									k++;
							}
							if (updAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(updAtt);

							Element delAtt = document.createElement("Delete_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);

								if (tokenA.x + 1 == tokenA.nx && tokenA.y == tokenA.ny) {
									Element toDel = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
									toDel.setTextContent(attrA.get(tokenA.nx - 1).getNodeValue());
									delAtt.appendChild(toDel);
									tokens.remove(k);
								} else
									k++;
							}
							if (delAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(delAtt);

							Element insAtt = document.createElement("Insert_Attribute");
							for (int k = 0; k < tokens.size();) {
								Info5 tokenA = tokens.get(k);
								if (tokenA.x == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
									Node toIns = document.createElement(attrB.get(tokenA.ny - 1).getNodeName());
									toIns.setTextContent(attrB.get(tokenA.ny - 1).getNodeValue());
									insAtt.appendChild(toIns);
									tokens.remove(k);
								} else
									k++;
							}
							if (insAtt.getChildNodes().getLength() != 0)
								attributes.appendChild(insAtt);

							elt2.appendChild(attributes);
						}
						eltu.appendChild(elt2);
					} else {

						if (((Info7) token).x + 1 == ((Info7) token).nx
								&& ((Info7) token).y + 1 == ((Info7) token).ny) {
							String op = ((Info7) token).a + "";
							Node toUpdateA = document.importNode(rootA, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digits = op.split("\\.");

								for (String a : digits)
									toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt(a) - 1);
							}
							toUpdateA = toUpdateA.getChildNodes().item(((Info7) token).nx - 1);

							// getting node Bxxx...
							op = ((Info7) token).b + "";
							Node toUpdate = document.importNode(rootB, true);
							if (op.length() > 1) {
								op = op.substring(2); // removing B or Tree Name

								String[] digitsB = op.split("\\.");

								for (String a : digitsB)
									toUpdate = toUpdate.getChildNodes().item(Integer.parseInt(a) - 1);
							}

							toUpdate = toUpdate.getChildNodes().item(((Info7) token).ny - 1);

							if (toUpdate.getNodeType() == Node.TEXT_NODE && toUpdateA.getNodeType() == Node.TEXT_NODE) {
								// updating text nodes;
								String[] nodeBWords = toUpdate.getTextContent().split("\\s+");
								String[] nodeAWords = toUpdateA.getTextContent().split("\\s+");
								Element nodenametext = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx);
								// nodenametext.setAttribute("type", "textNode");
								nodenametext.setAttribute("with", ((Info7) token).b + "." + ((Info7) token).ny);
								nodenametext.setAttribute("type", "Text_Node");
								ArrayList<Object> tokens = (ES.get(i + 1));

								Element updWord = document.createElement("Update_Word");
								for (int k = 0; k < tokens.size(); k++) {

									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setAttribute("with", "w" + textToken.ny);
										elt2.setAttribute("oldValue", nodeAWords[textToken.nx - 1]);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										updWord.appendChild(elt2);

									}
								}
								if (updWord.hasChildNodes())
									nodenametext.appendChild(updWord);

								Element delWord = document.createElement("Delete_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x + 1 == textToken.nx && (textToken).y == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.nx);
										elt2.setTextContent(nodeAWords[textToken.nx - 1]);
										delWord.appendChild(elt2);

									}
								}
								if (delWord.hasChildNodes())
									nodenametext.appendChild(delWord);

								Element insWord = document.createElement("Insert_Word");
								for (int k = 0; k < tokens.size(); k++) {
									Info5 textToken = (Info5) tokens.get(k);
									if (textToken.x == textToken.nx && (textToken).y + 1 == (textToken).ny) {
										Element elt2 = document.createElement("w" + textToken.ny);
										elt2.setTextContent(nodeBWords[textToken.ny - 1]);
										insWord.appendChild(elt2);

									}
								}
								if (insWord.hasChildNodes())
									nodenametext.appendChild(insWord);

								if (nodenametext.hasChildNodes())
									eltu.appendChild(nodenametext);

							}
						}

						else {
							// delete
							if (((Info7) token).x != -1 && ((Info7) token).x + 1 == ((Info7) token).nx
									&& ((Info7) token).y == ((Info7) token).ny) {

								Element elt2 = document
										.createElement(((Info7) token).a + "." + ((Info7) token).nx + "");

								String opc = ((Info7) token).a + "." + ((Info7) token).nx + "";
								Node toInsert = document.importNode(rootA, true);
								if (opc.length() > 1) {
									opc = opc.substring(2); // removing A or Tree Name

									String[] digits = opc.split("\\.");

									for (String a : digits) {
										toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
									}
								}
								if (containedIn(toInsert, TreesInB)) {
									// get the location in A
									String location = getLocationIn(toInsert, rootB, "B");
									elt2.setAttribute("containedAT", location);
								} else
									elt2.appendChild(toInsert);

								eltd.appendChild(elt2);

							} else {
								// insert
								if (((Info7) token).x != -1 && (((Info7) token).y + 1 == ((Info7) token).ny)
										&& (((Info7) token).x == ((Info7) token).nx)) {
									// inserting nodes or trees

									Element elt2;
									elt2 = document.createElement(
											"A" + (((Info7) token).b + "." + ((Info7) token).ny).substring(1));

									String opc = ((Info7) token).b + "." + ((Info7) token).ny + "";
									Node toInsert = document.importNode(rootB, true);
									if (opc.length() > 1) {
										opc = opc.substring(2); // removing B or Tree Name

										String[] digits = opc.split("\\.");

										for (String a : digits) {
											toInsert = toInsert.getChildNodes().item(Integer.parseInt(a) - 1);
										}
									}
									if (containedIn(toInsert, TreesInA)) {
										// get the location in A
										String location = getLocationIn(toInsert, rootA, "A");
										elt2.setAttribute("containedAT", location);
									} else
										elt2.appendChild(toInsert);
									elti.appendChild(elt2);

								}
							}

						}
					}
				}

			}
			if (eltu.hasChildNodes())
				es.appendChild(eltu);

			if (eltd.hasChildNodes())
				es.appendChild(eltd);

			if (elti.hasChildNodes())
				es.appendChild(elti);

			return root;
		} catch (DOMException | IllegalArgumentException | ParserConfigurationException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String reverseXMLES(String ESfileName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element ESroot = (Element) doc.importNode(getRootNodeFromFile(ESfileName), true);

			if (ESroot.getAttribute("reversible").equals("no")) {
				System.out.println("File is not reversible!");
				progressPropertyReverse.set(1);
				return "";
			}
			progressPropertyReverse.set(0.1);
			String orgRandom = "O" + Util.getAlphaNumericString(10);
			doc.renameNode(ESroot.getElementsByTagName("Original_File").item(0), null, orgRandom);
			doc.renameNode(ESroot.getElementsByTagName("Patched_File").item(0), null, "Original_File");
			doc.renameNode(ESroot.getElementsByTagName(orgRandom).item(0), null, "Patched_File");
			progressPropertyReverse.set(0.2);

			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);

			String rndDelete = "E" + Util.getAlphaNumericString(10);

			if (((Element) es).getElementsByTagName("Delete").item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName("Delete").item(0), null, rndDelete);
			if (((Element) es).getElementsByTagName("Insert").item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName("Insert").item(0), null, "Delete");
			if (((Element) es).getElementsByTagName(rndDelete).item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName(rndDelete).item(0), null, "Insert");
			progressPropertyReverse.set(0.3);

			Node update = ((Element) es).getElementsByTagName("Update").item(0);

			if (update != null) {
				NodeList upd = update.getChildNodes();

				for (int i = 0; i < upd.getLength(); i++) {
					Node node = upd.item(i);
					String with = ((Element) node).getAttribute("with");
					((Element) node).setAttribute("with", node.getNodeName());
					doc.renameNode(node, null, with);
					progressPropertyReverse.set(0.4);

					String rndDeleteWord = "W" + Util.getAlphaNumericString(10);
					if (((Element) node).getElementsByTagName("Delete_Word").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Delete_Word").item(0), null,
								rndDeleteWord);
					if (((Element) node).getElementsByTagName("Insert_Word").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Insert_Word").item(0), null,
								"Delete_Word");
					if (((Element) node).getElementsByTagName(rndDeleteWord).item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName(rndDeleteWord).item(0), null,
								"Insert_Word");
					progressPropertyReverse.set(0.5);

					Node label = ((Element) node).getElementsByTagName("Label").item(0);
					if (label != null) {
						String newLabel = label.getTextContent();
						String oldLabel = ((Element) label).getAttribute("oldValue");

						label.setTextContent(oldLabel);
						((Element) label).setAttribute("oldValue", newLabel);

					}
					progressPropertyReverse.set(0.6);
					Node updateWord = ((Element) node).getElementsByTagName("Update_Word").item(0);

					if (updateWord != null) {
						NodeList updWordChilds = updateWord.getChildNodes();
						for (int f = 0; f < updWordChilds.getLength(); f++) {
							Node word = updWordChilds.item(f);
							String oldLocation = word.getNodeName();
							doc.renameNode(word, null, ((Element) word).getAttribute("with"));
							((Element) word).setAttribute("with", oldLocation);
							String newWord = word.getTextContent();
							String oldWord = ((Element) word).getAttribute("oldValue");
							word.setTextContent(oldWord);
							((Element) word).setAttribute("oldValue", newWord);
						}
					}
					progressPropertyReverse.set(0.7);

					String rndDeleteAtt = "A" + Util.getAlphaNumericString(10);
					if (((Element) node).getElementsByTagName("Delete_Attribute").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Delete_Attribute").item(0), null,
								rndDeleteAtt);
					if (((Element) node).getElementsByTagName("Insert_Attribute").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Insert_Attribute").item(0), null,
								"Delete_Attribute");
					if (((Element) node).getElementsByTagName(rndDeleteAtt).item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName(rndDeleteAtt).item(0), null,
								"Insert_Attribute");
					progressPropertyReverse.set(0.8);

					Node updateAtt = ((Element) node).getElementsByTagName("Update_Attribute").item(0);

					if (updateAtt != null) {
						NodeList updAtt = updateAtt.getChildNodes();
						for (int f = 0; f < updAtt.getLength(); f++) {
							Node updElt = updAtt.item(f);
							String change = updElt.getAttributes().getNamedItem("change").getNodeValue();
							if (change.equals("value")) {
								String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
								String oldVal = updElt.getAttributes().getNamedItem("oldValue").getNodeValue();
								((Element) updElt).setAttribute("newValue", oldVal);
								((Element) updElt).setAttribute("oldValue", newVal);
							} else {
								if (change.equals("key")) {
									String newKey = updElt.getAttributes().getNamedItem("newKey").getNodeValue();
									String oldKey = updElt.getNodeName();
									((Element) updElt).setAttribute("newKey", oldKey);
									doc.renameNode(updElt, null, newKey);
								} else {
									if (change.equals("both")) {
										String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
										String newKey = updElt.getAttributes().getNamedItem("newKey").getNodeValue();

										String oldVal = updElt.getAttributes().getNamedItem("oldValue").getNodeValue();
										String oldKey = updElt.getNodeName();
										((Element) updElt).setAttribute("newValue", oldVal);
										((Element) updElt).setAttribute("oldValue", newVal);
										((Element) updElt).setAttribute("newKey", oldKey);
										doc.renameNode(updElt, null, newKey);

									}
								}
							}
						}
					}
					progressPropertyReverse.set(0.9);
				}

			}

			String original = ESroot.getElementsByTagName("Original_File").item(0).getAttributes().getNamedItem("name").getNodeValue();
			String newFile = ESroot.getElementsByTagName("Patched_File").item(0).getAttributes().getNamedItem("name").getNodeValue();
			String fileName = "XDP."+version+"_Diff_Reversible_" +original+ "_to_" +  newFile+ ".xml";
			String absPath = WriteXMLtoFile(ESroot, fileName, false, false);
			progressPropertyReverse.set(1.0);
			return absPath;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return "";
		}

	}

	public static Node reverseXMLESNode(String ESfileName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element ESroot = (Element) doc.importNode(getRootNodeFromFile(ESfileName), true);

			if (ESroot.getAttribute("reversible").equals("no")) {
				System.out.println("File is not reversible!");
//				progressPropertyReverse.set(1);
				return null;
			}
//			progressPropertyReverse.set(0.1);
			String orgRandom = "O" + Util.getAlphaNumericString(10);
			doc.renameNode(ESroot.getElementsByTagName("Original_File").item(0), null, orgRandom);
			doc.renameNode(ESroot.getElementsByTagName("Patched_File").item(0), null, "Original_File");
			doc.renameNode(ESroot.getElementsByTagName(orgRandom).item(0), null, "Patched_File");
//			progressPropertyReverse.set(0.2);

			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);

			String rndDelete = "E" + Util.getAlphaNumericString(10);

			if (((Element) es).getElementsByTagName("Delete").item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName("Delete").item(0), null, rndDelete);
			if (((Element) es).getElementsByTagName("Insert").item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName("Insert").item(0), null, "Delete");
			if (((Element) es).getElementsByTagName(rndDelete).item(0) != null)
				doc.renameNode(((Element) es).getElementsByTagName(rndDelete).item(0), null, "Insert");
//			progressPropertyReverse.set(0.3);

			Node update = ((Element) es).getElementsByTagName("Update").item(0);

			if (update != null) {
				NodeList upd = update.getChildNodes();

				for (int i = 0; i < upd.getLength(); i++) {
					Node node = upd.item(i);
					String with = ((Element) node).getAttribute("with");
					((Element) node).setAttribute("with", node.getNodeName());
					doc.renameNode(node, null, with);
//					progressPropertyReverse.set(0.4);

					String rndDeleteWord = "W" + Util.getAlphaNumericString(10);
					if (((Element) node).getElementsByTagName("Delete_Word").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Delete_Word").item(0), null,
								rndDeleteWord);
					if (((Element) node).getElementsByTagName("Insert_Word").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Insert_Word").item(0), null,
								"Delete_Word");
					if (((Element) node).getElementsByTagName(rndDeleteWord).item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName(rndDeleteWord).item(0), null,
								"Insert_Word");
//					progressPropertyReverse.set(0.5);

					Node label = ((Element) node).getElementsByTagName("Label").item(0);
					if (label != null) {
						String newLabel = label.getTextContent();
						String oldLabel = ((Element) label).getAttribute("oldValue");

						label.setTextContent(oldLabel);
						((Element) label).setAttribute("oldValue", newLabel);

					}
//					progressPropertyReverse.set(0.6);
					Node updateWord = ((Element) node).getElementsByTagName("Update_Word").item(0);

					if (updateWord != null) {
						NodeList updWordChilds = updateWord.getChildNodes();
						for (int f = 0; f < updWordChilds.getLength(); f++) {
							Node word = updWordChilds.item(f);
							String oldLocation = word.getNodeName();
							doc.renameNode(word, null, ((Element) word).getAttribute("with"));
							((Element) word).setAttribute("with", oldLocation);
							String newWord = word.getTextContent();
							String oldWord = ((Element) word).getAttribute("oldValue");
							word.setTextContent(oldWord);
							((Element) word).setAttribute("oldValue", newWord);
						}
					}
//					progressPropertyReverse.set(0.7);

					String rndDeleteAtt = "A" + Util.getAlphaNumericString(10);
					if (((Element) node).getElementsByTagName("Delete_Attribute").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Delete_Attribute").item(0), null,
								rndDeleteAtt);
					if (((Element) node).getElementsByTagName("Insert_Attribute").item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName("Insert_Attribute").item(0), null,
								"Delete_Attribute");
					if (((Element) node).getElementsByTagName(rndDeleteAtt).item(0) != null)
						doc.renameNode(((Element) node).getElementsByTagName(rndDeleteAtt).item(0), null,
								"Insert_Attribute");
//					progressPropertyReverse.set(0.8);

					Node updateAtt = ((Element) node).getElementsByTagName("Update_Attribute").item(0);

					if (updateAtt != null) {
						NodeList updAtt = updateAtt.getChildNodes();
						for (int f = 0; f < updAtt.getLength(); f++) {
							Node updElt = updAtt.item(f);
							String change = updElt.getAttributes().getNamedItem("change").getNodeValue();
							if (change.equals("value")) {
								String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
								String oldVal = updElt.getAttributes().getNamedItem("oldValue").getNodeValue();
								((Element) updElt).setAttribute("newValue", oldVal);
								((Element) updElt).setAttribute("oldValue", newVal);
							} else {
								if (change.equals("key")) {
									String newKey = updElt.getAttributes().getNamedItem("newKey").getNodeValue();
									String oldKey = updElt.getNodeName();
									((Element) updElt).setAttribute("newKey", oldKey);
									doc.renameNode(updElt, null, newKey);
								} else {
									if (change.equals("both")) {
										String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
										String newKey = updElt.getAttributes().getNamedItem("newKey").getNodeValue();

										String oldVal = updElt.getAttributes().getNamedItem("oldValue").getNodeValue();
										String oldKey = updElt.getNodeName();
										((Element) updElt).setAttribute("newValue", oldVal);
										((Element) updElt).setAttribute("oldValue", newVal);
										((Element) updElt).setAttribute("newKey", oldKey);
										doc.renameNode(updElt, null, newKey);

									}
								}
							}
						}
					}
//					progressPropertyReverse.set(0.9);
				}

			}


			return ESroot;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

	}

	private static String getLocationIn(Node toInsert, Node rootA, String treeName) {
		Node elt = rootA;
		StringBuilder toReturn = new StringBuilder(treeName);
		if (elt.isEqualNode(toInsert)) {
			return toReturn.toString();
		} else {
			NodeList a = elt.getChildNodes();
			for (int i = 0; i < a.getLength(); i++) {
				String str = getLocationIn(toInsert, a.item(i), treeName + "." + (i + 1));
				if (!str.equals("-1")) {
					return str;
				}
			}
			return "-1";
		}
	}

	public static Node getRootNodeFromFile(String fileName) throws Exception {
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dcm = db.parse(file);
//		dcm.getDocumentElement().normalize();
		Element root = dcm.getDocumentElement();
		clean(root);
		return root;
	}

	public static ArrayList<String> applyPatchXML(String fileName, String ESXML, boolean bypass) throws Exception {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(getRootNodeFromFile(fileName), true);
			doc.appendChild(rootC);

			Node ESroot = getRootNodeFromFile(ESXML);
			progressPropertyPatch.set(0.05);
			if (!bypass) {
				if (Double.parseDouble(
						((Element) ESroot).getAttributes().getNamedItem("version").getNodeValue()) != version) {
					System.out.println("Error: Incompatible version");
					ArrayList<String> arl= new ArrayList<>();
					arl.add("Error: Incompatible version");
					arl.add("");
					progressPropertyPatch.set(1);
					return arl;
				}
			}

			boolean fileNameMatches = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes()
					.getNamedItem("name").getNodeValue().equals(fileName.substring(fileName.lastIndexOf("\\") + 1));

			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes().getNamedItem("crc32")
					.getNodeValue();

			if (!bypass) {
				File tmpInput = File.createTempFile("nnj", ".tmp");
				WriteXMLtoFile(getRootNodeFromFile(fileName), tmpInput.getAbsolutePath(), true,true);
				long crc = checksumBufferedInputStream(tmpInput);
				String hashInput = Long.toHexString(crc);
				tmpInput.delete();

				boolean HashCodeMatches = hs1.equals(hashInput);

				if (!HashCodeMatches) {
					System.out.println("Error: Original File does not match!");
					ArrayList<String> arl= new ArrayList<>();
					arl.add("Error: Original File does not match!");
					arl.add("");
					progressPropertyPatch.set(1);
					return arl;
				}
			}
			if (!fileNameMatches) {
				System.out.println("Warning: Original File Name does not match!");
			}
			String patchedName = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(1)
					.getNodeValue();
			String targetHash = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(0)
					.getNodeValue();

			// NodeList editScript = ((Element)
			// ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);

			Node update = null;
			Node delete = null;
			Node insert = null;
			NodeList childs = ((Element) es).getChildNodes();
			for(int i = 0; i< childs.getLength();i++) {
				if(childs.item(i).getNodeName().equals("Update"))
					update = childs.item(i);
				if(childs.item(i).getNodeName().equals("Delete"))
					delete = childs.item(i);
				if(childs.item(i).getNodeName().equals("Insert"))
					insert = childs.item(i);
			}
			progressPropertyPatch.set(0.1);

			if (update != null) {
				NodeList upd = update.getChildNodes();
				for (int i = 0; i < upd.getLength(); i++) {
					progressPropertyPatch.set(0.1+0.45*i/0.0+upd.getLength());
					Node node = upd.item(i);
					String op1 = node.getNodeName();
					String[] op = op1.split("\\.");
					Node rc = rootC; // node to handle
					for (int f = 1; f < op.length; f++) {
						rc = rc.getChildNodes().item(Integer.parseInt(op[f]) - 1);
					}
					if (rc.getNodeType() == Node.TEXT_NODE) {
						Node updateWord = ((Element) node).getElementsByTagName("Update_Word").item(0);
						Node deleteWord = ((Element) node).getElementsByTagName("Delete_Word").item(0);
						Node insertWord = ((Element) node).getElementsByTagName("Insert_Word").item(0);

						if (updateWord != null) {
							NodeList updWordChilds = updateWord.getChildNodes();
							String[] wordsInNode = rc.getTextContent().split("\\s+");
							for (int uw = 0; uw < updWordChilds.getLength(); uw++) {
								Node word = updWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));

								String newWord = word.getTextContent();
								wordsInNode[wordNumber - 1] = newWord;
							}
							StringBuilder sb = new StringBuilder();
							for (String str : wordsInNode) {
								sb.append(str + " ");
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}
						if (deleteWord != null) {
							NodeList delWordChilds = deleteWord.getChildNodes();
							String[] wordsInNode = rc.getTextContent().split("\\s+");
							for (int uw = 0; uw < delWordChilds.getLength(); uw++) {
								Node word = delWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));
								wordsInNode[wordNumber - 1] = new String();

							}
							StringBuilder sb = new StringBuilder();
							for (String str : wordsInNode) {
								if (!str.isEmpty())
									sb.append(str + " ");
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}
						if (insertWord != null) {
							NodeList insWordChilds = insertWord.getChildNodes();
							String[] wordsInNodeArr = rc.getTextContent().split("\\s+");

							ArrayList<String> wordsInNode = new ArrayList<>(Arrays.asList(wordsInNodeArr));

							for (int uw = 0; uw < insWordChilds.getLength(); uw++) {
								Node word = insWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));
								String newWord = word.getTextContent();
								wordsInNode.add(wordNumber - 1, newWord);
							}
							StringBuilder sb = new StringBuilder();
							for (int f = 0; f < wordsInNode.size(); f++) {
								sb.append((wordsInNode.get(f) + " "));
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}

					} else {
						// Node or Element
						Node label = ((Element) node).getElementsByTagName("Label").item(0);
						if (label != null)
							doc.renameNode(rc, null, label.getTextContent());

						Node Attributes = ((Element) node).getElementsByTagName("Attributes").item(0);
						if (Attributes != null) {
							Node updateAtt = ((Element) Attributes).getElementsByTagName("Update_Attribute").item(0);
							Node deleteAtt = ((Element) Attributes).getElementsByTagName("Delete_Attribute").item(0);
							Node insertAtt = ((Element) Attributes).getElementsByTagName("Insert_Attribute").item(0);

							// do delete, update, insert
							if (deleteAtt != null) {
								NodeList delAtt = deleteAtt.getChildNodes();
								for (int f = 0; f < delAtt.getLength(); f++) {
									Node delElt = delAtt.item(f);
									((Element) rc).removeAttribute(delElt.getNodeName());
								}
							}

							if (updateAtt != null) {
								NodeList updAtt = updateAtt.getChildNodes();
								for (int f = 0; f < updAtt.getLength(); f++) {
									Node updElt = updAtt.item(f);
									String change = updElt.getAttributes().getNamedItem("change").getNodeValue();
									if (change.equals("value")) {
										String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
										((Element) rc).setAttribute(updElt.getNodeName(), newVal);
									} else {
										if (change.equals("key")) {
											String newKey = updElt.getAttributes().getNamedItem("newKey")
													.getNodeValue();
											doc.renameNode(rc.getAttributes().getNamedItem(updElt.getNodeName()), null,
													newKey);
										} else {
											if (change.equals("both")) {
												String newVal = updElt.getAttributes().getNamedItem("newValue")
														.getNodeValue();
												String newKey = updElt.getAttributes().getNamedItem("newKey")
														.getNodeValue();
												((Element) rc).setAttribute(updElt.getNodeName(), newVal);
												doc.renameNode(rc.getAttributes().getNamedItem(updElt.getNodeName()),
														null, newKey);

											}
										}
									}
								}
							}
							if (insertAtt != null) {
								NodeList insAtt = insertAtt.getChildNodes();
								for (int f = 0; f < insAtt.getLength(); f++) {
									Node insElt = insAtt.item(f);
									String key = insElt.getNodeName();
									String value = "" + insElt.getTextContent();
									((Element) rc).setAttribute(key, value);
								}
							}
						}
					}

				}
			}

			if (delete != null) {
				NodeList del = delete.getChildNodes();
				for (int i = del.getLength() - 1; i >= 0; i--) {
					progressPropertyPatch.set(0.55+0.2*(del.getLength()-i-1)/(0.0+del.getLength()));
					Node node = del.item(i);
					String[] op = node.getNodeName().split("\\.");
					Node temp = rootC;
					for (int f = 1; f < op.length - 1; f++) // get its parent
						temp = temp.getChildNodes().item(Integer.parseInt("" + op[f]) - 1);

					// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
					// doc.renameNode(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0))
					// - 1), null, randomDelete);
					temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op[op.length - 1]) - 1));

				}
			}

			if (insert != null) {
				NodeList ins = insert.getChildNodes();
				for (int i = 0; i < ins.getLength(); i++) {
					progressPropertyPatch.set(0.75+0.2*(i/(0.0+ins.getLength())));
					Node node = ins.item(i);
					if (node.hasAttributes() && node.getAttributes().getNamedItem("containedAT") != null) {
						String[] op = node.getAttributes().getNamedItem("containedAT").getNodeValue().split("\\.");
						Node rootA = doc.importNode(getRootNodeFromFile(fileName), true);
						for (int f = 1; f < op.length; f++) {
							rootA = rootA.getChildNodes().item(Integer.parseInt(op[f]) - 1);
						}

						String[] opc = node.getNodeName().split("\\.");
						Node rc = rootC;
						for (int f = 1; f < opc.length - 1; f++) {
							rc = rc.getChildNodes().item(Integer.parseInt(opc[f]) - 1);
						}
						rc.insertBefore(rootA, rc.getChildNodes().item(Integer.parseInt(opc[opc.length - 1]) - 1));

					} else {

						Node toInsert = doc.importNode(node.getFirstChild(), true);

						String[] opc = node.getNodeName().split("\\.");
						Node rc = rootC;
						for (int f = 1; f < opc.length - 1; f++) {
							rc = rc.getChildNodes().item(Integer.parseInt(opc[f]) - 1);
						}
						rc.insertBefore(toInsert, rc.getChildNodes().item(Integer.parseInt(opc[opc.length - 1]) - 1));

					}
				}
			}

			String absPath = WriteXMLtoFile(rootC, patchedName, false,false);

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(absPath), tmpInput2.getAbsolutePath(), true,true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();
			ArrayList<String> arl= new ArrayList<>();


			if (!targetHash.equals(hashInput2)) {
				System.out.println("Wrong Result Expected: Hash checksum does not match");
				arl.add("Wrong Result Expected: Hash checksum does not match");
			} else {
				System.out.println("Patch successful, hash checksum matches!");
				arl.add("Patch successful, hash checksum matches!");
			}
			progressPropertyPatch.set(1);
			arl.add(absPath);
			return arl;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ArrayList<String> arl= new ArrayList<>();
			arl.add("Error Exception: "+e.toString());
			arl.add("");
			progressPropertyPatch.set(1);
			return arl;
		}

	}

	public static Node applyPatchXMLNode(Node prevFile, String ESXML, boolean bypass) throws Exception {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Node rootC = doc.importNode(prevFile,true);
			doc.appendChild(rootC);

			Node ESroot = getRootNodeFromFile(ESXML);
//			progressPropertyPatch.set(0.05);
			if (!bypass) {
				if (Double.parseDouble(
						((Element) ESroot).getAttributes().getNamedItem("version").getNodeValue()) != version) {
					System.out.println("Error: Incompatible version");
//					ArrayList<String> arl= new ArrayList<>();
					throw new Exception("Error: Incompatible version");
//					progressPropertyPatch.set(1);
//					return arl;
				}
			}


			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes().getNamedItem("crc32")
					.getNodeValue();

			if (!bypass) {
				File tmpInput = File.createTempFile("nnj", ".tmp");
				WriteXMLtoFile(prevFile, tmpInput.getAbsolutePath(), true,true);
				long crc = checksumBufferedInputStream(tmpInput);
				String hashInput = Long.toHexString(crc);
				tmpInput.delete();

				boolean HashCodeMatches = hs1.equals(hashInput);

				if (!HashCodeMatches) {
					System.out.println("Error: Original File does not match!");
					throw new Exception("Error: Original File does not match!");

//					System.out.println("Error: ");
//					ArrayList<String> arl= new ArrayList<>();
//					arl.add("Error: Original File does not match!");
//					arl.add("");
//					progressPropertyPatch.set(1);
//					return arl;
				}
			}

//			String patchedName = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(1)
//					.getNodeValue();
//			String targetHash = ((Element) ESroot).getElementsByTagName("Patched_File").item(0).getAttributes().item(0)
//					.getNodeValue();

			// NodeList editScript = ((Element)
			// ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);
			
			Node update = null;
			Node delete = null;
			Node insert = null;
			NodeList childs = ((Element) es).getChildNodes();
			for(int i = 0; i< childs.getLength();i++) {
				if(childs.item(i).getNodeName().equals("Update"))
					update = childs.item(i);
				if(childs.item(i).getNodeName().equals("Delete"))
					delete = childs.item(i);
				if(childs.item(i).getNodeName().equals("Insert"))
					insert = childs.item(i);
			}
		
			progressPropertyPatch.set(0.1);

			if (update != null) {
				NodeList upd = update.getChildNodes();
				for (int i = 0; i < upd.getLength(); i++) {
					progressPropertyPatch.set(0.1+0.45*i/0.0+upd.getLength());
					Node node = upd.item(i);
					String op1 = node.getNodeName();
					String[] op = op1.split("\\.");
					Node rc = rootC; // node to handle
					for (int f = 1; f < op.length; f++) {
						rc = rc.getChildNodes().item(Integer.parseInt(op[f]) - 1);
					}
					if (rc.getNodeType() == Node.TEXT_NODE) {
						Node updateWord = ((Element) node).getElementsByTagName("Update_Word").item(0);
						Node deleteWord = ((Element) node).getElementsByTagName("Delete_Word").item(0);
						Node insertWord = ((Element) node).getElementsByTagName("Insert_Word").item(0);

						if (updateWord != null) {
							NodeList updWordChilds = updateWord.getChildNodes();
							String[] wordsInNode = rc.getTextContent().split("\\s+");
							for (int uw = 0; uw < updWordChilds.getLength(); uw++) {
								Node word = updWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));

								String newWord = word.getTextContent();
								wordsInNode[wordNumber - 1] = newWord;
							}
							StringBuilder sb = new StringBuilder();
							for (String str : wordsInNode) {
								sb.append(str + " ");
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}
						if (deleteWord != null) {
							NodeList delWordChilds = deleteWord.getChildNodes();
							String[] wordsInNode = rc.getTextContent().split("\\s+");
							for (int uw = 0; uw < delWordChilds.getLength(); uw++) {
								Node word = delWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));
								wordsInNode[wordNumber - 1] = new String();

							}
							StringBuilder sb = new StringBuilder();
							for (String str : wordsInNode) {
								if (!str.isEmpty())
									sb.append(str + " ");
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}
						if (insertWord != null) {
							NodeList insWordChilds = insertWord.getChildNodes();
							String[] wordsInNodeArr = rc.getTextContent().split("\\s+");

							ArrayList<String> wordsInNode = new ArrayList<>(Arrays.asList(wordsInNodeArr));

							for (int uw = 0; uw < insWordChilds.getLength(); uw++) {
								Node word = insWordChilds.item(uw);
								int wordNumber = Integer.parseInt("" + word.getNodeName().substring(1));
								String newWord = word.getTextContent();
								wordsInNode.add(wordNumber - 1, newWord);
							}
							StringBuilder sb = new StringBuilder();
							for (int f = 0; f < wordsInNode.size(); f++) {
								sb.append((wordsInNode.get(f) + " "));
							}
							sb.delete(sb.length() - 1, sb.length()); // deleting extra " ";
							rc.setTextContent(sb.toString());
						}

					} else {
						// Node or Element
						Node label = ((Element) node).getElementsByTagName("Label").item(0);
						if (label != null)
							doc.renameNode(rc, null, label.getTextContent());

						Node Attributes = ((Element) node).getElementsByTagName("Attributes").item(0);
						if (Attributes != null) {
							Node updateAtt = ((Element) Attributes).getElementsByTagName("Update_Attribute").item(0);
							Node deleteAtt = ((Element) Attributes).getElementsByTagName("Delete_Attribute").item(0);
							Node insertAtt = ((Element) Attributes).getElementsByTagName("Insert_Attribute").item(0);

							// do delete, update, insert
							if (deleteAtt != null) {
								NodeList delAtt = deleteAtt.getChildNodes();
								for (int f = 0; f < delAtt.getLength(); f++) {
									Node delElt = delAtt.item(f);
									((Element) rc).removeAttribute(delElt.getNodeName());
								}
							}

							if (updateAtt != null) {
								NodeList updAtt = updateAtt.getChildNodes();
								for (int f = 0; f < updAtt.getLength(); f++) {
									Node updElt = updAtt.item(f);
									String change = updElt.getAttributes().getNamedItem("change").getNodeValue();
									if (change.equals("value")) {
										String newVal = updElt.getAttributes().getNamedItem("newValue").getNodeValue();
										((Element) rc).setAttribute(updElt.getNodeName(), newVal);
									} else {
										if (change.equals("key")) {
											String newKey = updElt.getAttributes().getNamedItem("newKey")
													.getNodeValue();
											doc.renameNode(rc.getAttributes().getNamedItem(updElt.getNodeName()), null,
													newKey);
										} else {
											if (change.equals("both")) {
												String newVal = updElt.getAttributes().getNamedItem("newValue")
														.getNodeValue();
												String newKey = updElt.getAttributes().getNamedItem("newKey")
														.getNodeValue();
												((Element) rc).setAttribute(updElt.getNodeName(), newVal);
												doc.renameNode(rc.getAttributes().getNamedItem(updElt.getNodeName()),
														null, newKey);

											}
										}
									}
								}
							}
							if (insertAtt != null) {
								NodeList insAtt = insertAtt.getChildNodes();
								for (int f = 0; f < insAtt.getLength(); f++) {
									Node insElt = insAtt.item(f);
									String key = insElt.getNodeName();
									String value = "" + insElt.getTextContent();
									((Element) rc).setAttribute(key, value);
								}
							}
						}
					}

				}
			}

			if (delete != null) {
				NodeList del = delete.getChildNodes();
				for (int i = del.getLength() - 1; i >= 0; i--) {
//					progressPropertyPatch.set(0.55+0.2*(del.getLength()-i-1)/(0.0+del.getLength()));
					Node node = del.item(i);
					String[] op = node.getNodeName().split("\\.");
					Node temp = rootC;
					for (int f = 1; f < op.length - 1; f++) // get its parent
						temp = temp.getChildNodes().item(Integer.parseInt("" + op[f]) - 1);

					// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
					// doc.renameNode(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0))
					// - 1), null, randomDelete);
					temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op[op.length - 1]) - 1));

				}
			}

			if (insert != null) {
				NodeList ins = insert.getChildNodes();
				for (int i = 0; i < ins.getLength(); i++) {
//					progressPropertyPatch.set(0.75+0.2*(i/(0.0+ins.getLength())));
					Node node = ins.item(i);
					if (node.hasAttributes() && node.getAttributes().getNamedItem("containedAT") != null) {
						String[] op = node.getAttributes().getNamedItem("containedAT").getNodeValue().split("\\.");
						Node rootA = doc.importNode(prevFile, true);
						for (int f = 1; f < op.length; f++) {
							rootA = rootA.getChildNodes().item(Integer.parseInt(op[f]) - 1);
						}

						String[] opc = node.getNodeName().split("\\.");
						Node rc = rootC;
						for (int f = 1; f < opc.length - 1; f++) {
							rc = rc.getChildNodes().item(Integer.parseInt(opc[f]) - 1);
						}
						rc.insertBefore(rootA, rc.getChildNodes().item(Integer.parseInt(opc[opc.length - 1]) - 1));

					} else {

						Node toInsert = doc.importNode(node.getFirstChild(), true);

						String[] opc = node.getNodeName().split("\\.");
						Node rc = rootC;
						for (int f = 1; f < opc.length - 1; f++) {
							rc = rc.getChildNodes().item(Integer.parseInt(opc[f]) - 1);
						}
						rc.insertBefore(toInsert, rc.getChildNodes().item(Integer.parseInt(opc[opc.length - 1]) - 1));

					}
				}
			}

//			String absPath = WriteXMLtoFile(rootC, patchedName, false,false);

//			File tmpInput2 = File.createTempFile("nnj", ".tmp");
//			tmpInput2.deleteOnExit();
//			WriteXMLtoFile(rootC, tmpInput2.getAbsolutePath(), true,true);
//			long crc2 = checksumBufferedInputStream(tmpInput2);
//			String hashInput2 = Long.toHexString(crc2);
//			tmpInput2.delete();
//			ArrayList<String> arl= new ArrayList<>();
//
//
//			if (!targetHash.equals(hashInput2)) {
//				System.out.println("Wrong Result Expected: Hash checksum does not match");
//			} else {
//				System.out.println("Patch successful, hash checksum matches!");
//			}
//			progressPropertyPatch.set(1);
			return rootC;



	}

	public static String WriteXMLtoFile(Node root, String fileName, boolean overwrite, boolean temp) {
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
			String fileName2 = fileName;

			if(!temp) {
				new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\output\\").mkdirs();
				fileName2 = Paths.get(".").toAbsolutePath().normalize().toString()+"\\output\\"+fileName;
			}

			int index = 1;

			if (!overwrite) {
				while (new File(fileName2).exists()) {
					index++;
					fileName2 = fileName2.substring(0, fileName2.lastIndexOf('.')) + "_" + index
							+ fileName2.substring(fileName2.lastIndexOf('.'));
				}
			}

//			File directory  = new File(Paths.get(".").toAbsolutePath().normalize().toString());
//			File myFile = new File(fileName2+".xml");
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



	public static String WriteXMLtoFile2(Node root, String fileName,String folderName ,boolean overwrite) {
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
			String fileName2 = folderName+File.separator+fileName;


			int index = 1;

			if (!overwrite) {
				while (new File(fileName2).exists()) {
					index++;
					fileName2 = fileName2.substring(0, fileName2.lastIndexOf('.')) + "_" + index
							+ fileName2.substring(fileName2.lastIndexOf('.'));
				}
			}

//			File directory  = new File(Paths.get(".").toAbsolutePath().normalize().toString());
//			File myFile = new File(fileName2+".xml");
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



	public static void clean(Node node) {
		node.normalize();
		NodeList childNodes = node.getChildNodes();

		for (int n = childNodes.getLength() - 1; n >= 0; n--) {
			Node child = childNodes.item(n);
			short nodeType = child.getNodeType();

			if (nodeType == Node.ELEMENT_NODE)
				clean(child);
			else if (nodeType == Node.TEXT_NODE) {
				String trimmedNodeVal = child.getNodeValue().trim();
				trimmedNodeVal = trimmedNodeVal.replaceAll("\\s+", " "); // remove extra spaces
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

}
