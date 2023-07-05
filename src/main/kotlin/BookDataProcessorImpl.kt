import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BookDataProcessorImpl(private val bookManipulation: BookManipulationImpl) : BookDataProcessor {
    private val epubParser = bookManipulation.epubParser
    private val txtParser = bookManipulation.txtParser
    private val fb2Parser = bookManipulation.fb2Parser

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