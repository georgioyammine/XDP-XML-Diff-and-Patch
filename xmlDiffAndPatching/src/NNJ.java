
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class NNJ {
	final static int updateRootCost  = 1;
	final static int insertContained = 1;
	final static int deleteContained = 1;
	final static int deleteOrInsertLeaf = 1;
	private static ArrayList<Node> TreesInA = new ArrayList<>();
	private static ArrayList<Node> TreesInB = new ArrayList<>();
	

	public static int TED(Node rootA, Node rootB){
		clean(rootA);
		clean(rootB);
		
		getTreesInA(rootA);	// pre-processing
		getTreesInB(rootB);	// pre-processing
		
		
		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();
		
		int m = listA.getLength();
		int n = listB.getLength();
		
		int[][] dist = new int[m+1][n+1];
		dist[0][0] =  CostUpdateRoot(rootA,rootB);
		
		for(int i = 1; i<= m;i++) {
			dist[i][0] = dist[i-1][0] + CostDeleteTree(rootA.getChildNodes().item(i-1));
		}
		for(int i = 1; i<= n;i++) {
			dist[0][i] = dist[0][i-1] + CostInsertTree(rootB.getChildNodes().item(i-1));
		}
		
		for(int i = 1;i<=m;i++) {
			for(int j=1;j<=n;j++) {
//				System.out.println(rootA.getChildNodes().item(i-1));
//				System.out.println(rootB.getChildNodes().item(j-1));
//				System.out.println(i+" "+j+" "+
//			rootA+" "+rootB+
//			" "+(CostDeleteTree(rootA))
//			+" "+(CostInsertTree(rootB))
//			 );
				
				
				dist[i][j] = Math.min(Math.min(dist[i-1][j-1] + TED(listA.item(i-1),
						listB.item(j-1)),
						dist[i-1][j] + CostDeleteTree(rootA.getChildNodes().item(i-1))), dist[i][j-1] + CostInsertTree(rootB.getChildNodes().item(j-1))); 
			}
		}
		System.out.println(rootA+" "+rootB);
		for(int i =0;i<=m;i++) {
			System.out.println(Arrays.toString(dist[i]));
			
		}
		System.out.println();
		
		return dist[m][n];
	}
	
	private static boolean containedIn(Node rootA, ArrayList<Node> TreesIn) {
		for(Node a: TreesIn) {
			if(a.isEqualNode(rootA))
				return true;
		}
		return false;
	}
	
	public static boolean containedIn(Node rootA, Node rootB,boolean f) {
		clean(rootA);
		clean(rootB);
		
		getTreesInB(rootB);	// pre-processing
		
		System.out.println(TreesInB);
		for(Node a: TreesInB) {
			if(a.isEqualNode(rootA))
				return true;
		}
		return false;
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
		if(true||rootB.getNodeType()==Node.ELEMENT_NODE) {
			if(containedIn(rootB, TreesInA)) {
				return insertContained;
			}
			return deleteOrInsertLeaf+((Element) rootB).getElementsByTagName("*").getLength();
		}
		return deleteOrInsertLeaf;
	}

	private static int CostDeleteTree(Node rootA) {
		if(rootA.getNodeType()==Node.ELEMENT_NODE) {
			if(containedIn(rootA, TreesInB)) {
				return deleteContained;
			}
			return deleteOrInsertLeaf+((Element) rootA).getElementsByTagName("*").getLength();
		}
		return deleteOrInsertLeaf;
	}
	

	private static int CostUpdateRoot(Node rootA, Node rootB) {
		if(rootA.getNodeName().equals(rootB.getNodeName()))
			return 0;
		else
			return updateRootCost;
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
