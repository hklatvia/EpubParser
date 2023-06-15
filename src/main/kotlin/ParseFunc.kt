import nl.siegmann.epublib.domain.Book
import java.io.File

open interface ParseFunc {
    fun parseBook(file: File): String
    fun loadEpub(book: File): Book?
    fun collectBookContent(book: Book): String
}