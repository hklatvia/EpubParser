import java.io.File

interface BookDataProcessor {
    fun collectNewBooksMetadata(listOfBooks: List<File>, namesThatContains: List<String>): List<BookData>
}