import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class CacheManagerImpl(private val bookManipulation: BookManipulationImpl) : CacheManager {
    private val cacheFile: File = File(bookManipulation.cacheDirectory)
    private val fileCacheWriter: FileWriter = FileWriter(cacheFile, true)
    private val fileCacheReader: FileReader = FileReader(cacheFile)
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    override fun storeBookMetadata(filesDirectory: String) {
        val booksInStorage = bookManipulation.findBooksInDirectory(filesDirectory)
        val listOfBooks = mutableListOf<File>()
        listOfBooks.addAll(booksInStorage.epubFiles)
        listOfBooks.addAll(booksInStorage.txtFiles)
        listOfBooks.addAll(booksInStorage.fb2Files)
        val tempCache = mutableListOf<BookData>()
        val namesThatContains = loadCache(listOfBooks)
        val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
        val cacheLock = ReentrantLock()
        listOfBooks.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                executorService.execute {
                    val content = bookManipulation.getDataOfBook(file.path)
                    cacheLock.lock()
                    try {
                        tempCache.addAll(content)
                    } finally {
                        cacheLock.unlock()
                    }
                }
            }
        }
        executorService.shutdown()
        while (!executorService.isTerminated) {
            executorService.awaitTermination(0, TimeUnit.SECONDS)
        }
        writeCache(tempCache)
    }

    private fun loadCache(epubFiles: List<File>): List<String> {
        val lines = fileCacheReader.readLines()
        val result = epubFiles.flatMap { file ->
            lines.filter { line ->
                line.contains(file.name)
            }
        }
        return result
    }

    private fun writeCache(tempCache: List<BookData>) {
        if (tempCache.isEmpty()) return
        val bookList = BookList(tempCache)
        val json = gson.toJson(bookList)
        fileCacheWriter.write(json)
        fileCacheWriter.close()
    }
}