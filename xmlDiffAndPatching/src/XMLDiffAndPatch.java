
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
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

public class XMLDiffAndPatch {
	final static int updateRootName = 1;
	final static int insertContained = 1;
	final static int deleteContained = 1;
	final static int deleteOrInsertLeaf = 1; // 3
	final static int attributeNameCost = 1; // 2
	final static int attributeValueCost = 1;
	final static int contentTokenCost = 1;
	static HashMap<Node, Integer> costsOfTrees = new HashMap<>();

	private static ArrayList<Node> TreesInA = new ArrayList<>();
	private static ArrayList<Node> TreesInB = new ArrayList<>();
	// private static String randomDelete = "jci8ElHvLDr6DKejR7ng";
	static double version = 0.1;

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

	public static class Info5 {
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
		// results.add(dist);
		// results.add(pointers);

		ArrayList<Object> updateRoot = CostUpdateRoot(rootA, rootB);
		dist[0][0] = (int) updateRoot.remove(0);

		pointers[0][0] = new ArrayList<>();
		pointers[0][0].add(new Info7(R1, R2, -1, -1, 0, 0, dist[0][0]));
		pointers[0][0].add(updateRoot);
		pointers[0][0].add("");
		System.out.println("UR" + updateRoot);

		for (int i = 1; i <= m; i++) {
			dist[i][0] = dist[i - 1][0] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
			ArrayList<Object> del = new ArrayList<>();
			del.add(new Info7(R1, R2, i - 1, 0, i, 0, dist[i][0]));
			pointers[i][0] = del;
		}
		for (int j = 1; j <= n; j++) {
			dist[0][j] = dist[0][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
			ArrayList<Object> ins = new ArrayList<>();
			ins.add(new Info7(R1, R2, 0, j - 1, 0, j, dist[0][j]));
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

				int update = Integer.MAX_VALUE / 2;
				boolean text = false;
				ArrayList<Object> updateInfo = new ArrayList<>();
				if (listA.item(i - 1).getNodeType() == listB.item(j - 1).getNodeType()) {
					// update is possible
					if (listA.item(i - 1).getNodeType() == Node.TEXT_NODE) {
						updateInfo = EDStrings(listA.item(i - 1).getTextContent().split("\\s+"),
								listB.item(j - 1).getTextContent().split("\\s+"));
						text = true;

					} else {
						updateInfo = TED(listA.item(i - 1), listB.item(j - 1), R1 + i, R2 + j, false);
					}
					update = dist[i - 1][j - 1] + (int) updateInfo.get(0);
				}

				int delete = dist[i - 1][j] + CostDeleteTree(rootA.getChildNodes().item(i - 1));
				int insert = dist[i][j - 1] + CostInsertTree(rootB.getChildNodes().item(j - 1));
				if (update < delete) {
					if (update < insert) {
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
						dist[i][j] = insert;
						ArrayList<Object> ins = new ArrayList<>();
						ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
						pointers[i][j] = ins;
					}
				} else {
					if (delete < insert) {
						dist[i][j] = delete;
						ArrayList<Object> del = new ArrayList<>();
						del.add(new Info7(R1, R2, i - 1, j, i, j, dist[i][j]));
						pointers[i][j] = del;
					} else {
						dist[i][j] = insert;
						ArrayList<Object> ins = new ArrayList<>();
						ins.add(new Info7(R1, R2, i, j - 1, i, j, dist[i][j]));
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

		ArrayList<Object> ES = getEditScript(m, n, pointers, dist, R1, R2);
		results.add(0, ES);
		results.add(0, dist[m][n]);
		// results.add(2,fes);
		System.out.println();
		return results;
	}

	private static ArrayList<Object> getEditScript(int m, int n, ArrayList<Object>[][] pointers, int[][] dist,
			String r1, String r2) {
		ArrayList<Object> reversedEditScript = new ArrayList<>();

		int a = m, b = n;
		while (a >-1  && b > -1 && ((Info7) (pointers[a][b].get(0))).z != 0) {
			ArrayList<Object> prev = pointers[a][b];
			if (prev.size() > 1) {
				if (prev.size() > 2) {
					// adding info of update root
					if(!((ArrayList<Object>) prev.get(1)).isEmpty()) {
					reversedEditScript.add(prev.get(1));
					reversedEditScript.add((Info7) prev.get(0));
					}

				} else {
					if (prev.size() > 3) {
						if(!((ArrayList<Object>) prev.get(1)).isEmpty()) {
						reversedEditScript.addAll((ArrayList<Object>) prev.get(1));
//						((Info7) prev.get(0)).x;
//						((Info7) prev.get(0)).y;
						reversedEditScript.add((Info7) prev.get(0));
						}
					} else {
						// adding info of recursive call

						reversedEditScript.addAll((ArrayList<Object>) prev.get(1));

						// System.out.println(((int[][])(prev.get(1)))[0][0]);
						// reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));

						// reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));
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

	private static ArrayList<String> formatEditScipt(ArrayList<Info7> E) {
		ArrayList<Info7> ES = new ArrayList<>(E);
		ArrayList<String> fes = new ArrayList<>();
		Collections.reverse(ES);
		for (Info7 token : ES) {
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

	public static ArrayList<Object> TEDandEditScript(String fileName1, String fileName2) throws Exception {

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

		ArrayList<Object> ted = XMLDiffAndPatch.TED(root, root2, "A", "B", true);

		int distance = (Integer) ted.get(0);
		Collections.reverse((List<Object>) ted.get(1));
		editScriptToXML((ArrayList<ArrayList<Object>>) ted.get(1), file, file2,root, root2, distance);
		return ted;
	}

	private static boolean containedIn(Node rootA, ArrayList<Node> TreesIn) {
		for (Node a : TreesIn) {
			if (rootA.isEqualNode(a))
				return true;
			// if (containedIn2(rootA, a))
			// return true;
		}
		return false;
	}

	//
	public static boolean containedIn(Node rootA, Node rootB) {
		getTreesInB(rootB);
		return containedIn(rootA, TreesInB);
	}

	private static boolean containedIn2(Node rootA, Node rootB) {
		if (!rootA.getNodeName().equals(rootB.getNodeName()))
			return false;

		if (!rootA.hasChildNodes())
			return rootA.isEqualNode(rootB);

		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();
		boolean flag1 = true;
		for (int i = 0; i < listA.getLength(); i++) {
			boolean flag = false;
			for (int j = 0; j < listB.getLength(); j++) {
				if (listA.item(i).getNodeName().equals(listB.item(j).getNodeName()))
					flag = flag || containedIn2(listA.item(i), listB.item(j));
				if (flag)
					break;
			}
			flag1 = flag1 && flag;
			if (!flag1)
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
		if (costsOfTrees.containsKey(rootB))
			return costsOfTrees.get(rootB);
		if (rootB.getNodeType() == Node.ELEMENT_NODE) {
			if (containedIn(rootB, TreesInA)) {
				return insertContained;
			}
			int cost = deleteOrInsertLeaf;
			NodeList childs = rootB.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				cost += CostDeleteTree(childs.item(i));

			// cost of inserting attributes
			cost += (attributeNameCost + attributeValueCost) * rootB.getAttributes().getLength();

			costsOfTrees.put(rootB, cost);
			return cost;
		} else {
			return contentTokenCost * rootB.getTextContent().split("\\s+").length;
		}
	}

	private static int CostDeleteTree(Node rootA) {
		if (costsOfTrees.containsKey(rootA))
			return costsOfTrees.get(rootA);
		if (rootA.getNodeType() == Node.ELEMENT_NODE) {
			if (containedIn(rootA, TreesInB)) {
				return insertContained;
			}
			int cost = deleteOrInsertLeaf;
			NodeList childs = rootA.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				cost += CostDeleteTree(childs.item(i));

			// cost of inserting attributes
			cost += (attributeNameCost + attributeValueCost) * rootA.getAttributes().getLength();

			costsOfTrees.put(rootA, cost);
			return cost;
		} else {
			return contentTokenCost * rootA.getTextContent().split("\\s+").length;
		}
	}

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
				Util.getArlFromNNM(rootB.getAttributes()));
		cost += (int) (attr.get(0));
		update.add(attr.get(1));

		update.add(0, cost); // cost (int) - 1 or 0 (int) - ES attr (arrayList)

		return update;
	}

	// public static String getFirstLevelTextContent(Node node) {
	// NodeList list = node.getChildNodes();
	// StringBuilder textContent = new StringBuilder();
	// for (int i = 0; i < list.getLength(); ++i) {
	// Node child = list.item(i);
	// if (child.getNodeType() == Node.TEXT_NODE)
	// textContent.append(child.getTextContent()+" ");
	// }
	// System.out.println("YXT"+textContent.toString());
	// return textContent.toString();
	// }

	private static ArrayList<Object> getESNode(ArrayList<Object>[][] pointers) {
		// ArrayList<Info5> reversedEditScript = new ArrayList<>();
		//
		// int a = m, b = n;
		// int A = 10;
		// while (a != -1 && b != -1 && ((Info5) (pointers[a][b].get(0))).z != 0) {
		// ArrayList<Object> prev = pointers[a][b];
		// if (prev.size() > 1) {
		// // System.out.println(((int[][])(prev.get(1)))[0][0]);
		// // reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));
		// reversedEditScript.addAll((ArrayList<Info5>) prev.get(1));
		// // reversedEditScript.add(new XYZ("X", "X", 0, 0, 0));
		// } else
		// reversedEditScript.add((Info7) prev.get(0));
		//
		// a = (int) ((Info7) prev.get(0)).x;
		// b = (int) ((Info7) prev.get(0)).y;
		//
		// }
		// return reversedEditScript;

		ArrayList<ArrayList<Object>> ESNode = new ArrayList<>();
		ArrayList<Object> toReturn = new ArrayList<>();

		ArrayList<Object> token = pointers[pointers.length - 1][pointers[0].length - 1];
		while (((Info5) (token.get(0))).z != 0) {
			ESNode.add(token);
			token = pointers[((Info5) (token.get(0))).x][((Info5) (token.get(0))).y];
		}
		Collections.reverse(ESNode);
		int prev = 0;
		// System.out.println(ESContent);
		for (int i = 0; i < ESNode.size();) {

			Info5 elt = (Info5) (ESNode.get(i).get(0));
			// System.out.println("i: "+i+ " "+prev+" "+elt.z);

			if (elt.z == prev) {
				ESNode.remove(i);
			} else {
				toReturn.add(elt);
				if (ESNode.get(i).size() > 1)
					toReturn.add(ESNode.get(i).get(1));

				int tmp = prev;
				prev = elt.z;
				elt.z -= tmp;
				i++;
			}

		}
		return toReturn;
	}

	public static ArrayList<Object> EDAttr(ArrayList<Node> attrA, ArrayList<Node> attrB) {
		reorder(attrA, attrB);
		System.out.println(attrA);
		System.out.println(attrB);
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
		for (int i = 0; i <= attrA.size(); i++) {
			for (int j = 0; j <= attrB.size(); j++)
				System.out.print(distance[i][j] + " ");
			System.out.println();
		}
		arl.add(distance[attrA.size()][attrB.size()]);
		// arl.add(pointers);
		arl.add(getESfromEDNodeOrAtt(pointers));
		// return distance[attrA.getLength()][attrB.getLength()];
		return arl;
	}

	public static void reorder(ArrayList<Node> listA, ArrayList<Node> listB) {
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

	public static ArrayList<String> formatEDAttr(ArrayList<Info5> tokens, ArrayList<Node> attrA,
			ArrayList<Node> attrB) {
		ArrayList<String> arl = new ArrayList<>();
		for (Info5 token : tokens) {
			String str = "";
			if (token.x + 1 == token.nx && token.y + 1 == token.ny) {
				// Info5 updates = getUpdateAttrScript(token,attrA,attrB);
				// str = "Update at attribute "+token.nx+" exanchge "+
				// (updates.x==-5?"Name to "+attrB.item(token.ny-1).getNodeName()+" " :" ")+
				// (updates.y==-5?"Value to "+attrB.item(token.ny-1).getNodeValue():" ")
				// + "with attribute from" + token.ny;

				Info5 updates = getUpdateAttrScript(token, attrA, attrB);
				str = "Update attribute " + attrA.get(token.nx - 1).getNodeName() + " exanchge "
						+ (updates.x == -5 ? "Name to " + attrB.get(token.ny - 1).getNodeName() + " " : " ")
						+ (updates.y == -5 ? "Value to " + attrB.get(token.ny - 1).getNodeValue() + " " : " ");

			} else {
				if (token.x + 1 == token.nx && token.y == token.ny) {
					str = "Delete " + token.nx;
				} else {
					if (token.x == token.nx && token.y + 1 == token.ny) {
						str = "Insert " + " at " + token.ny + " " + attrB.get(token.ny - 1) + " ";
					}
				}
			}
			arl.add(str);
		}
		return arl;

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

	public static ArrayList<Object> EDStrings(String[] rootAContent, String[] rootBContent) {
		System.out.println(Arrays.toString(rootAContent));
		System.out.println(Arrays.toString(rootBContent));
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
						// System.out.println("Update " + i + " " + j);
					} else {
						insert = distance[i][j - 1] + contentTokenCost;
						// System.out.println("Insert at "+ (i-1) + " " + j + " " + distance[i-1][j]);
						if (insert < update) {
							distance[i][j] = insert;
							pointers[i][j] = new Info5(i, j - 1, i, j, insert);
						} else {
							distance[i][j] = update;
							pointers[i][j] = new Info5(i - 1, j - 1, i, j, update);
							// System.out.println("Update " + i + " " + j);
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
		for (int i = 0; i <= rootAContent.length; i++) {
			for (int j = 0; j <= rootBContent.length; j++)
				System.out.print(distance[i][j] + " ");
			System.out.println();
		}
		// return distance (int), pointers [][], ES ArrayList
		arl.add(distance[rootAContent.length][rootBContent.length]);
		// arl.add(pointers);
		arl.add(getESfromEDNodeOrAtt(pointers));
		return arl;
	}

	public static ArrayList<Info5> getESfromEDNodeOrAtt(Info5[][] pointers) {
		ArrayList<Info5> ESContent = new ArrayList<>();

		Info5 token = pointers[pointers.length - 1][pointers[0].length - 1];
		while (token.z != 0) {
			ESContent.add(token);
			token = pointers[token.x][token.y];
		}
		Collections.reverse(ESContent);
		int prev = 0;
		// System.out.println(ESContent);
		for (int i = 0; i < ESContent.size();) {

			Info5 elt = ESContent.get(i);
			// System.out.println("i: "+i+ " "+prev+" "+elt.z);

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

	public static ArrayList<String> formatESContent(ArrayList<Info5> ESContent, String[] a2) {
		ArrayList<String> arl = new ArrayList<>();
		for (Info5 token : ESContent) {
			String str = "";
			if (token.x + 1 == token.nx && token.y + 1 == token.ny) {
				str = "Update " + token.nx + " with " + a2[token.ny - 1] + " ";
			} else {
				if (token.x + 1 == token.nx && token.y == token.ny) {
					str = "Delete " + token.nx;
				} else {
					if (token.x == token.nx && token.y + 1 == token.ny) {
						str = "Insert " + " at " + token.ny + " " + a2[token.ny - 1] + " ";
					}
				}
			}
			arl.add(str);
		}
		return arl;
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

	private static void editScriptToXML(ArrayList<ArrayList<Object>> E, File original, File newFile, Element rootA, Element rootB, int distance)
			throws Exception {
		try {
			ArrayList<ArrayList<Object>> ES = new ArrayList<>(E);

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
			WriteXMLtoFile(getRootNodeFromFile(original.getAbsolutePath()), tmpInput.getAbsolutePath(), true);
			long crc = checksumBufferedInputStream(tmpInput);
			String hashInput = Long.toHexString(crc);

			file1.setAttribute("Hash", "" + hashInput);

			// hashCode is used to verify if the input file is correct and compatible
			// file1.setTextContent(original.getName());

			Element file2 = document.createElement("Patched_File");
			file2.setAttribute("name", newFile.getName());

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(newFile.getAbsolutePath()), tmpInput2.getAbsolutePath(), true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			file2.setAttribute("Hash", "" + hashInput2);

			// hash is used to verify if the output is correct

			root.appendChild(file1);
			root.appendChild(file2);

			Element es = document.createElement("Edit_Script");

			root.appendChild(es);

//			Collections.reverse(ES);
			Element eltu;
			eltu = document.createElement("Update");
			Element eltd;
			eltd = document.createElement("Delete");
			Element elti;
			elti = document.createElement("Insert");
			
			
			for (int i = 0;i<ES.size();i++) {
				Object token = ES.get(i);
				if(token.getClass()==Info7.class) {
					
				if (((Info7)token).x == -1) {
					ArrayList<Object> nextToken = ES.get(i+1);
					Element elt2 = document.createElement(((Info7)token).a + "");
					elt2.setAttribute("type", "node");
					if(((int)nextToken.get(0))==1) {
						//updating Labels
						Element label = document.createElement("Label");
						
						
						String op = ((Info7)token).b + "";
						op = op.substring(1); // removing B or Tree Name
						Node toUpdate = document.importNode(rootB, true);

						while (op.length() > 0) {
							toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
					// elt.setAttribute(token.a, toUpdate.getNodeName());
					label.setTextContent(toUpdate.getNodeName());
					elt2.appendChild(label);
					eltu.appendChild(elt2);
					}
					if(!((ArrayList)nextToken.get(1)).isEmpty()) {
						// updating attributes
						
						Element attributes = document.createElement("Attributes");
						elt2.appendChild(attributes);
						ArrayList<Info5> tokens= (ArrayList<Info5>) nextToken.get(1);
						
						// getting node Axxx...
						String op = ((Info7)token).a + "";
						op = op.substring(1); // removing A or Tree Name
						Node toUpdateA = document.importNode(rootA, true);

						while (op.length() > 0) {
							toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
						
						// getting node Bxxx...
						op = ((Info7)token).b + "";
						op = op.substring(1); // removing B or Tree Name
						Node toUpdate = document.importNode(rootB, true);

						while (op.length() > 0) {
							toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
							op = op.substring(1);
						}
						ArrayList<Node> attrA = Util.getArlFromNNM(toUpdateA.getAttributes());
						ArrayList<Node> attrB = Util.getArlFromNNM(toUpdate.getAttributes());
						
						reorder(attrA, attrB);
						
						Element updAtt = document.createElement("update_Attribute");
						for (int k=0;k<tokens.size();) {
							Info5 tokenA = tokens.get(k);
							if (tokenA.x + 1 == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
								Info5 updates = getUpdateAttrScript(tokenA, attrA, attrB);
								Element upd = document.createElement(attrA.get(tokenA.nx - 1).getNodeName());
								Attr change = document.createAttribute("change");
			
								if(updates.x == -5) {
									if(updates.y == -5) {
										change.setValue("both");
										Attr newKey = document.createAttribute("newKey");
										newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
										Attr newValue = document.createAttribute("newValue");
										newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
										upd.setAttributeNode(newValue);
										upd.setAttributeNode(newKey);
									}
									else {
										change.setValue("key");
										Attr newKey = document.createAttribute("newKey");
										newKey.setNodeValue(attrB.get(tokenA.ny - 1).getNodeName());
										upd.setAttributeNode(newKey);
									}
								}
								else {
									change.setValue("value");
									Attr newValue = document.createAttribute("newValue");
									newValue.setNodeValue(attrB.get(tokenA.ny - 1).getNodeValue());
									upd.setAttributeNode(newValue);
								}
								upd.setAttributeNode(change);
								updAtt.appendChild(upd);
								
//								upd.setAttributeNodeNS(
//								str = "Update attribute " + attrA.get(tokenA.nx - 1).getNodeName() + " exanchge "
//										+ (updates.x == -5 ? "Name to " + attrB.get(tokenA.ny - 1).getNodeName() + " " : " ")
//										+ (updates.y == -5 ? "Value to " + attrB.get(tokenA.ny - 1).getNodeValue() + " " : " ");
//								
								
								tokens.remove(k);
							}
							else
								k++;
						}
						if(updAtt.getChildNodes().getLength()!=0) 
							attributes.appendChild(updAtt);
						
						Element delAtt = document.createElement("Delete_Attribute");
						for (int k=0;k<tokens.size();) {
							Info5 tokenA = tokens.get(k);

							if (tokenA.x + 1 == tokenA.nx && tokenA.y == tokenA.ny) {
								Element toDel = document.createElement(attrA.get(tokenA.nx).getNodeName());
//									str = "Delete " + attrA.get(tokenA.nx).getNodeName();
								delAtt.appendChild(toDel);
									tokens.remove(k);
							} 
							else
								k++;
						}
						if(delAtt.getChildNodes().getLength()!=0) 
							attributes.appendChild(delAtt);
						
						Element insAtt = document.createElement("Insert_Attribute");
						for (int k=0;k<tokens.size();) {
							Info5 tokenA = tokens.get(k);
								if (tokenA.x == tokenA.nx && tokenA.y + 1 == tokenA.ny) {
//										str = "Insert " + " at " + tokenA.ny + " " + attrB.get(tokenA.ny - 1) + " ";
										Node toIns = document.createElement(attrB.get(tokenA.ny - 1).getNodeName());
										toIns.setTextContent(attrB.get(tokenA.ny - 1).getNodeValue());
										insAtt.appendChild(toIns);
										tokens.remove(k);
								}
								else
									k++;
						}
						if(insAtt.getChildNodes().getLength()!=0) 
							attributes.appendChild(insAtt);
						
					}
				}
				else {
					// getting node Axxx...
					if(((Info7)token).x+1 == ((Info7)token).nx && ((Info7)token).y+1==((Info7)token).ny){
					String op = ((Info7)token).a + "";
					op = op.substring(1); // removing B or Tree Name
					Node toUpdateA = document.importNode(rootA, true);

					while (op.length() > 0) {
						toUpdateA = toUpdateA.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					toUpdateA = toUpdateA.getChildNodes().item(((Info7)token).nx-1);
					
					// getting node Bxxx...
					op = ((Info7)token).b + "";
					op = op.substring(1); // removing B or Tree Name
					Node toUpdate = document.importNode(rootB, true);

					while (op.length() > 0) {
						toUpdate = toUpdate.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					toUpdate = toUpdate.getChildNodes().item(((Info7)token).ny-1);
					
					if(toUpdate.getNodeType()==Node.TEXT_NODE && toUpdateA.getNodeType()==Node.TEXT_NODE ) {
						// updating text nodes;
						String[] nodeBWords = toUpdate.getTextContent().split("\\s+");
						
						Element nodenametext = document.createElement(((Info7)token).a + "");
						nodenametext.setAttribute("type", "textNode");
						ArrayList<Object> tokens = (ES.get(i+1));
						
						Element updWord = document.createElement("Update_Word");
						for(int k =0;k<tokens.size();k++) {

							Info5 textToken = (Info5) tokens.get(k);
							if (textToken.x + 1 ==textToken.nx && (textToken).y +1 ==(textToken).ny) {
								Element elt2 = document.createElement("w"+textToken.nx);
								elt2.setTextContent(nodeBWords[textToken.ny-1]);
								updWord.appendChild(elt2);
								// ES.remove(token);
							}
						}
						if(updWord.hasChildNodes()) 
							nodenametext.appendChild(updWord);
						
						Element delWord = document.createElement("Delete_Word");
						for(int k =0;k<tokens.size();k++) {
							Info5 textToken = (Info5) tokens.get(k);
							if (textToken.x + 1 ==textToken.nx && (textToken).y ==(textToken).ny) {
								Element elt2 = document.createElement("w"+textToken.nx);
								delWord.appendChild(elt2);
								// ES.remove(token);
//								tokens.remove(k);
							}	
						}
						if(delWord.hasChildNodes()) 
							nodenametext.appendChild(delWord);
						
						Element insWord = document.createElement("Insert_Word");
						for(int k =0;k<tokens.size();k++) {
							Info5 textToken = (Info5) tokens.get(k);
							if (textToken.x  ==textToken.nx && (textToken).y +1 ==(textToken).ny) {
								Element elt2 = document.createElement("w"+textToken.nx);
								elt2.setTextContent(nodeBWords[textToken.ny-1]);
								insWord.appendChild(elt2);
								// ES.remove(token);
//								tokens.remove(k);
							}	
						}
						if(insWord.hasChildNodes()) 
							nodenametext.appendChild(insWord);
						
						if(nodenametext.hasChildNodes()) 
							eltu.appendChild(nodenametext);
						
					}
					}		
				
				else {
					if (((Info7)token).x != -1 && ((Info7)token).x + 1 ==((Info7)token).nx && ((Info7)token).y ==((Info7)token).ny) {
						Element elt2 = document.createElement(((Info7)token).a + ((Info7)token).nx + "");
						eltd.appendChild(elt2);
					}
					else {
						if (((Info7)token).x != -1 && (((Info7)token).y + 1 == ((Info7)token).ny)  && (((Info7)token).x == ((Info7)token).nx)) {
							Element elt2;
							elt2 = document.createElement("A" + (((Info7)token).b + ((Info7)token).ny + "").substring(1));
							// elt.setAttribute("at", );
							// elt.setTextContent(token.b+token.ny+"");

							String opc = ((Info7)token).b + ((Info7)token).ny + "";
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
						
				}
				}
				}

			}
			if (eltu.hasChildNodes())
				es.appendChild(eltu);
//
//			Element eltd;
//			eltd = document.createElement("Delete");
//			for (Object token : ES) {
//				if(token.getClass()==Info7.class) {
//
//				if (((Info7)token).x != -1 && ((Info7)token).x + 1 ==((Info7)token).nx && ((Info7)token).y ==((Info7)token).ny) {
//					Element elt2 = document.createElement(((Info7)token).a + ((Info7)token).nx + "");
//					eltd.appendChild(elt2);
//					// ES.remove(token);
//				}
//				}
//			}
			if (eltd.hasChildNodes())
				es.appendChild(eltd);
//			Element elti;
//			elti = document.createElement("Insert");
//			for (Object token : ES) {
//				if(token.getClass()==Info7.class) {
//				if (((Info7)token).x != -1 && (((Info7)token).y + 1 == ((Info7)token).ny)  && (((Info7)token).x == ((Info7)token).nx)) {
//					Element elt2;
//					elt2 = document.createElement("A" + (((Info7)token).b + ((Info7)token).ny + "").substring(1));
//					// elt.setAttribute("at", );
//					// elt.setTextContent(token.b+token.ny+"");
//
//					String opc = ((Info7)token).b + ((Info7)token).ny + "";
//					opc = opc.substring(1); // removing B or Tree Name
//					Node toInsert = document.importNode(rootB, true);
//
//					while (opc.length() > 0) {
//						toInsert = toInsert.getChildNodes().item(Integer.parseInt("" + opc.charAt(0)) - 1);
//						opc = opc.substring(1);
//					}
//					elt2.appendChild(toInsert);
//					elti.appendChild(elt2);
//
//					// ES.remove(token);
//
//				}
//			}
//			}
			if (elti.hasChildNodes())
				es.appendChild(elti);

			String fileName = "______PATCH_" + original.getName() + "_" + newFile.getName() + "_" + version + ".xml";

			WriteXMLtoFile(root, fileName, false);
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
			// randomDelete = "A" + getAlphaNumericString(9);
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

			String hs1 = ((Element) ESroot).getElementsByTagName("Original_File").item(0).getAttributes().item(0)
					.getNodeValue();

			File tmpInput = File.createTempFile("nnj", ".tmp");
			WriteXMLtoFile(getRootNodeFromFile(fileName), tmpInput.getAbsolutePath(), true);
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

			// NodeList editScript = ((Element)
			// ESroot).getElementsByTagName("Edit_Script").item(0).getChildNodes();
			Node es = ((Element) ESroot).getElementsByTagName("Edit_Script").item(0);

			Node update = ((Element) es).getElementsByTagName("Update").item(0);
			Node delete = ((Element) es).getElementsByTagName("Delete").item(0);
			Node insert = ((Element) es).getElementsByTagName("Insert").item(0);

			if (update != null) {
				NodeList upd = update.getChildNodes();
				for (int i = 0; i < upd.getLength(); i++) {
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

			if (delete != null) {
				NodeList del = delete.getChildNodes();
				for (int i = del.getLength() - 1; i >= 0; i--) {
					Node node = del.item(i);
					String op = node.getNodeName();
					op = op.substring(1); // removing A or Tree Name
					Node temp = rootC;
					while (op.length() > 1) {
						temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					// temp.setNodeValue("BxvD8Xdlq0O8ejTS"); // value for delete
					// doc.renameNode(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0))
					// - 1), null, randomDelete);
					temp.removeChild(temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));

				}
			}

			if (insert != null) {
				NodeList ins = insert.getChildNodes();
				for (int i = 0; i < ins.getLength(); i++) {
					Node node = ins.item(i);

					Node toInsert = doc.importNode(node.getFirstChild(), true);

					String op = node.getNodeName();
					op = op.substring(1); // removing A or Tree Name
					Node temp = rootC;
					while (op.length() > 1) {
						temp = temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1);
						op = op.substring(1);
					}
					if (temp.hasChildNodes()) {
						temp.insertBefore(toInsert, temp.getChildNodes().item(Integer.parseInt("" + op.charAt(0)) - 1));
					} else {
						temp.appendChild(toInsert);
					}
				}
			}

			String absPath = WriteXMLtoFile(rootC, patchedName, false);

			File tmpInput2 = File.createTempFile("nnj", ".tmp");
			tmpInput2.deleteOnExit();
			WriteXMLtoFile(getRootNodeFromFile(absPath), tmpInput2.getAbsolutePath(), true);
			long crc2 = checksumBufferedInputStream(tmpInput2);
			String hashInput2 = Long.toHexString(crc2);
			tmpInput2.delete();

			if (!targetHash.equals(hashInput2)) {
				System.out.println("Wrong Result Expected: Hash checksum does not match");
			} else {
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
				scan.close();
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
				scan.close();
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
				scan.close();
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

}
