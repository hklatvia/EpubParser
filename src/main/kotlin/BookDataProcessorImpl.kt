import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BookDataProcessorImpl(private val bookManipulation: BookManipulationImpl): BookDataProcessor {
    private val bookFinder = BooksFinderImpl()
    private val epubParser = EpubParser()
    private val txtParser = TxtParser()
    private val fb2Parser = Fb2Parser()
    private val cacheManager = CacheManagerImpl(bookManipulation)

    override fun storeBookMetadata(filesDirectory: String) {
        val listOfBooks = collectListOfBooks(filesDirectory)
        val namesThatContains = cacheManager.loadCache(listOfBooks)
        val tempCache = collectNewBooksMetadata(listOfBooks, namesThatContains)
        cacheManager.writeCache(tempCache)
    }

    override fun collectNewBooksMetadata(listOfBooks: List<File>, namesThatContains: List<String>): List<BookData> {
        val tempCache = mutableListOf<BookData>()
        val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
        listOfBooks.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                executorService.execute {
                    val content = getDataOfBooks(file.path)
                    synchronized(tempCache) {
                        tempCache.addAll(content)
                    }
                }
            }
        }
        executorService.shutdown()
        while (!executorService.isTerminated) {
            executorService.awaitTermination(0, TimeUnit.SECONDS)
        }
        return tempCache
    }

    private fun collectListOfBooks(filesDirectory: String): List<File> {
        val booksInStorage = bookFinder.findBooksInDirectory(filesDirectory)
        val listOfBooks = mutableListOf<File>()
        listOfBooks.addAll(booksInStorage.epubFiles)
        listOfBooks.addAll(booksInStorage.txtFiles)
        listOfBooks.addAll(booksInStorage.fb2Files)
        return listOfBooks
    }

    private fun getDataOfBooks(filePath: String): List<BookData> {
        val file = File(filePath)
        val listBooks = mutableListOf<BookData>()
        when (val fileExtension = file.extension) {
            FileExtensions.EPUB.stringVal -> listBooks.add(epubParser.parseContent(filePath))
            FileExtensions.TXT.stringVal -> listBooks.add(txtParser.parseContent(filePath))
            FileExtensions.FB2.stringVal -> listBooks.add(fb2Parser.parseContent(filePath))
            else -> throw IllegalArgumentException("Unsupported file format: $fileExtension")
        }
        return listBooks
    }
}