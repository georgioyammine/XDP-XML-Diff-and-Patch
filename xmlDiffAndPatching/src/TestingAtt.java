
import java.util.ArrayList;

public class TestingAtt {

	public static void main(String[] args) throws Exception {
		String s1 = "abc elr wkeo akke";
		String s2 = "aee sje elr akke ww";
		
		System.out.println(s1);
		System.out.println(s2);
		System.out.println();
		
		String[] a1 = s1.split("\\s+");
		String[] a2 = s2.split("\\s+");
		
		ArrayList<Object> arl = XMLDiffAndPatchOld.EDNodeValue(a1, a2);
		System.out.println(arl.get(0));
		ArrayList<XMLDiffAndPatchOld.Info5> ES = 
				XMLDiffAndPatchOld.getESfromEDNodeOrAtt((XMLDiffAndPatchOld.Info5[][])arl.get(1));
		
		System.out.println(ES);
//		System.out.println(XMLDiffAndPatch.fo));
		System.out.println(XMLDiffAndPatchOld.formatESContent(ES,a2));
		
	}

}
