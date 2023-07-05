import java.io.File
import java.io.FileInputStream

class TxtParser : BookParser {
    override fun parseContent(file: String): BookData {
        val fileOfBook = File(file)
        val bookData = FileInputStream(file).reader().use { it.readText() }
        val uniqueWords = regex.findAll(bookData)
            .map { it.value }
            .toSet()
        return BookData("No author in TXT files", fileOfBook.name, uniqueWords.size, file)
    }

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}