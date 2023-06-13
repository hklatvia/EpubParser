import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.lang.StringBuilder

class EpubParse {
    fun loadEpub(book: File): Book? {
        val epubFile = FileInputStream(book.path)
        return EpubReader().readEpub(epubFile)
    }

    fun collectBookContent(book: Book): StringBuilder {
        val plainText = StringBuilder()
        for (resourse in book.contents) {
            plainText.append(resourse.reader.readText())
        }
        return plainText
    }
    fun parseBook(file: File): String {
        var result = ""
        val book = loadEpub(file)
        if (book != null) {
            val bookReaded = collectBookContent(book).toString()
            val bookParsed = Jsoup.parse(bookReaded)
            result = bookParsed.text()
        }
        return result
    }
}