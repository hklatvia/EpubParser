import com.google.gson.GsonBuilder
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.FileReader

class EpubParser : BookParser {
    private val epubReader = EpubReader()

    override fun parseContent(file: String): BookData {
        val book = FileInputStream(file).use { fileInputStream ->
            epubReader.readEpub(fileInputStream)
        }
        val metadata = book.metadata
        val author = metadata?.authors?.firstOrNull()?.toString() ?: "Нет данных об авторе"
        val title = metadata?.titles?.firstOrNull()?.toString() ?: "Нет данных о названии"
        val bookContent = collectBookContent(book)
        val bookParsed = Jsoup.parse(bookContent)
        val uniqueWords = regex.findAll(bookParsed.text())
            .map {it.value}
            .toSet()
        return BookData(author, title, uniqueWords.size, file)
    }

    private fun collectBookContent(book: Book): String {
        return buildString {
            book.contents.forEach {
                append(it.reader.readText())
            }
        }
    }

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}