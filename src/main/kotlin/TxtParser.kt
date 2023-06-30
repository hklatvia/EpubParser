import java.io.FileInputStream

class TxtParser : BookParser {
    override fun parseContent(file: String): String {
        return FileInputStream(file).reader().use { it.readText() }
    }
}