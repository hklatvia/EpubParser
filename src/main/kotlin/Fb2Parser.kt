import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import javax.xml.parsers.DocumentBuilderFactory

class Fb2Parser : BookParser {

    fun parseAuthorFromFb2(file: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(file)

        val authorNodes = document.getElementsByTagName("author")
        if (authorNodes.length > 0) {
            val authorNode = authorNodes.item(0)
            return authorNode.textContent.trim().replace("\n", "")
        }

        return "No author"
    }

    override fun parseContent(path: String): String {
        val fb2File = File(path)
        val documentBuilder = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
        val document = documentBuilder.parse(fb2File)
        return parseNode(document.documentElement)
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
}

