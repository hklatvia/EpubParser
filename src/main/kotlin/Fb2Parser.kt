import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class Fb2Parser : BookParser {

    override fun parseContent(path: String): BookData {
        val fb2File = File(path)
        val documentBuilder = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
        val document = documentBuilder.parse(fb2File)
        val bookContent=parseNode(document.documentElement)
        val uniqueWords = regex.findAll(bookContent)
            .map {it.value}
            .toSet()
        return BookData(parseAuthorFromFb2(path), fb2File.name, uniqueWords.size, fb2File.toString())
    }

    private fun parseNode(node: org.w3c.dom.Node): String {
        val contentBuilder = StringBuilder()
        if (node.nodeType == org.w3c.dom.Node.TEXT_NODE) {
            contentBuilder.append(node.textContent)
        } else {
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                val childNode = childNodes.item(i)
                contentBuilder.append(parseNode(childNode))
            }
        }
        return contentBuilder.toString()
    }

    private fun parseAuthorFromFb2(file: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(file)
        val authorNodes = document.getElementsByTagName("author")
        if (authorNodes.length > 0) {
            val authorNode = authorNodes.item(0)
            return authorNode.textContent.trim().replace("\n", "")
        }
        return "No author"
    }
    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}

