# XDP - XML Diff and Patching

**XDP** is a tool in built in **Java** that compares two **XML** documents (structure and content), provides the **diff** file represented in an XML format and that highlights the **operations** that must be executed in order to transform a file into the other. This diff can be made **reversible** to provide A->B patch or B->A patch. Moreover, this tool can do the **patching** having as inputs the XML file and its corresponding diff file and transforms it to the target XML document. This tool has many applications: 
- data warehousing
- version control
- XML retrieval and much more.

This Application is part of a project in the **Lebanese American University**, Course: Intelligent Data Processing and Applications (COE 543/743).

## Authors
This Tool is built by **Georgio Yammine** and **Rami Naffah**.

## Tools Used
**Java 8**, **JavaFX** and **FXML** with **JFoenix** Library.

## Runnables and Project
Project and **runnable jar** file created in **eclipse**.

exe created using **launch4j**.

## Tree Edit Distance
The tree edit distance algorithm utilized in our study is an adaptation of **Nierman and Jagadish’s** main edit distance algorithm [1]. However, in our case, we have to also take into consideration the content such as **text nodes, attributes and more**.

The variations we implemented were:
1. Updating root node changes the root label and root attributes.
2. Update is only possible if the 2 nodes are of the same type.
3. Text nodes are tokenized on space (“ “) and are processed in EDWords.
4. Element Nodes are updated using a call of TED.

**The report of our application is included in the report and includes more in depth explanation.**

## Patch Format
Non-Reversible Patch:

![non-reversible](/images/diffFormatNotReversible.PNG)

Reversible Patch with extra info highlighted in Yellow.

![reversible](/images/diffFormatReversible.PNG)

## Preview
![Get Diff](/images/getDiff.png)
![reverseDiff](/images/reverseDiff.png)
![reverseDiff Not reversible file ](/images/reverseDiffFail.png)
![apply patch](/images/applyPatch.png)
![apply patch wrong file](/images/applyPatchFail.png)
![About Info](/images/about.PNG)
![About Settings](/images/aboutChangeCosts.PNG)
![About Help](/images/aboutHelp.PNG)

## References
[1] A. Nierman and H. V. Jagadish. Evaluating structural similarity in XML documents. In Proceedings of the 5th ACM SIGMOD International Workshop on the Web and Databases (WebDB), (2002) pp. 61-66.
