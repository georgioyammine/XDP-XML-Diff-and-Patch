
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import 

public class NNJ {
	final static int updateRootCost  = 1;
	final static int insertContained = 1;
	final static int deleteContained = 1;
	private static HashSet

	public static int[][] NNJ(Element rootA, Element rootB ){
		
		clean(rootA);
		NodeList listA = rootA.getChildNodes();
		NodeList listB = rootB.getChildNodes();
		
		int m = listA.getLength();
		int n = listB.getLength();
		
		int[][] dist = new int[m+1][n+1];
		dist[0][0] =  CostUpdateRoot(rootA,rootB);
		
		for(int i = 1; i<= m;i++) {
			dist[i][0] = dist[i-1][0] + CostDeleteTree(rootA);
		}
		for(int i = 1; i<= n;i++) {
			dist[0][i] = dist[0][i-1] + CostInsertTree(rootB);
		}
		
		for(int i = 1;i<=m;i++) {
			for(int j=1;j<n;j++) {
				dist[i][j] = Math.min(Math.min(dist[i-1][j-1] + NNJ(listA.item(i),listA.item(j)),dist[i-1][j] + CostDeleteTree(rootA)), dist[i][j-1] + CostInsertTree(rootB)); 
			}
		}
		
		return dist[m][n];
	}

	private static int CostInsertTree(Node rootB) {
		return rootB.getElementsByTagName("*").getLength();
	}

	private static int CostDeleteTree(Node rootA) {
		return rootA.getElementsByTagName("*").getLength();
	}
	
	private static boolean containedIn(Node in, Node tree) {
		
		
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
