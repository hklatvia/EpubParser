import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.FileInputStream

class EpubParser : BookParser {
    private val epubReader = EpubReader()

    override fun parseContent(file: String): String {
        val book = loadEpub(file)
        return if (book == null) {
            "Empty book"
        } else {
            val bookReed = collectBookContent(book)
            val bookParsed = Jsoup.parse(bookReed)
            bookParsed.text()
        }
    }

    private fun loadEpub(bookPath: String): Book? {
        val epubStream = FileInputStream(bookPath)
        val result = epubReader.readEpub(epubStream)
        epubStream.close()
        return result
    }

    private fun collectBookContent(book: Book): String {
        return buildString {
            book.contents.forEach {
                append(it.reader.readText())
            }
        }
    }
}