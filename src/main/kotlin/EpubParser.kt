import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream

class EpubParse : ParseFunc {
    override fun loadEpub(book: File): Book? {
        val epubFile = FileInputStream(book.path)
        return EpubReader().readEpub(epubFile)
    }

    override fun collectBookContent(book: Book): String {
        return buildString {
            book.contents.forEach {
                append(it.reader.readText())
            }
        }
    }

    override fun parseBook(file: File): String {
        val result: String
        val book = loadEpub(file)
        return if (book == null) {
            "Empty book"
        } else {
            val bookReaded = collectBookContent(book)
            val bookParsed = Jsoup.parse(bookReaded)
            result = bookParsed.text()
            result
        }
    }
}