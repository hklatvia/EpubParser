import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader

class BookManipulationImpl(
    val cacheDirectory: String,
) : BookManipulation {
    val fb2Parser: BookParser = Fb2Parser()
    val epubParser: BookParser = EpubParser()
    val txtParser: BookParser = TxtParser()
    private val cacheFile = File(cacheDirectory)
    private val bookDataProcessor: BookDataProcessor = BookDataProcessorImpl(this)
    private val cacheManager: CacheManager = CacheManagerImpl(this)
    private val bookFinder: BooksFinder = BooksFinderImpl()
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    override fun printMetaBooksFromDirectory(directoryName: String) {
        storeBookMetadata(directoryName)
        val result = FileReader(cacheFile).use { reader ->
            gson.fromJson(reader, BookList::class.java).books
        }
        result.forEach { println(it) }
    }

    private fun storeBookMetadata(filesDirectory: String) {
        val listOfBooks = collectListOfBooks(filesDirectory)
        val namesThatContains = cacheManager.loadCache(listOfBooks)
        val tempCache = bookDataProcessor.collectNewBooksMetadata(listOfBooks, namesThatContains)
        cacheManager.writeCache(tempCache)
    }

    private fun collectListOfBooks(filesDirectory: String): List<File> {
        val booksInStorage = bookFinder.findBooksInDirectory(filesDirectory)
        val listOfBooks = mutableListOf<File>()
        listOfBooks.addAll(booksInStorage.epubFiles)
        listOfBooks.addAll(booksInStorage.txtFiles)
        listOfBooks.addAll(booksInStorage.fb2Files)
        return listOfBooks
    }
}
