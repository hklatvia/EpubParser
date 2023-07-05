import java.io.File

interface BookDataProcessor {
    fun storeBookMetadata(filesDirectory: String)
    fun collectNewBooksMetadata(listOfBooks: List<File>, namesThatContains: List<String>): List<BookData>
}