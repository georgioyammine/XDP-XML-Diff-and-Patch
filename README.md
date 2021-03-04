# XDP - XML Diff and Patch

**XDP** is a tool in built in **Java** that compares two **XML** documents (structure and content), provides the **diff** file represented in an XML format and that highlights the **operations** that must be executed in order to transform a file into the other. This diff can be made **reversible** to provide A->B patch or B->A patch. Moreover, this tool can do the **patching** having as inputs the XML file and its corresponding diff file and transforms it to the target XML document. This tool has many applications: 
- data warehousing
- version control
- XML retrieval and much more.

_This Application was part of a project in the **Lebanese American University**, Course: Intelligent Data Processing and Applications (COE 543/743)._

## Authors
This Tool is built by **Georgio Yammine** and **Rami Naffah**.

## Tools Used
**Java 8**, **JavaFX** and **FXML** with **JFoenix** Library.

## Runnables and Project

* [Oracle JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) is required for running the app and can be downloaded from [here](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html). 
    * Be sure to have the path set to jdk 8. 
    * This can be checked by calling `java -version`

Project and **runnable jar** file created in **eclipse**.

exe created using **launch4j**.

## Tree Edit Distance
The tree edit distance algorithm utilized in our app is an adaptation of **Nierman and Jagadish’s** main edit distance algorithm [1]. However, in our case, we have to also take into consideration the content such as **text nodes, attributes and more**.

The variations we implemented were:
1. Updating root node changes the root label and root attributes.
2. Update is only possible if the 2 nodes are of the same type.
3. Text nodes are tokenized on space (“ “) and are processed in EDWords.
4. Element Nodes are updated using a call of TED.

_The report of our application is included in the repository and includes a more in depth explanation and information._

## Diff Format
Non-Reversible Diff to the left and Reversible Diff to the right with extra info highlighted in Yellow:

![Diff](/images/Diff-format.png)

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
