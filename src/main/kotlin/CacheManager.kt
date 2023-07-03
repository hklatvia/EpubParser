import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

abstract class CacheManager() : BookManipulationImpl() {

    val cacheFile = File("C:\\Users\\aleks\\IdeaProjects\\epubepub\\cache")
    private val fileCacheWriter = FileWriter(cacheFile, true)
    private val fileCacheReader = FileReader(cacheFile)

    fun storeBookMetadata(filesDirectory: String) {
        val tripleOfFiles = findBooksInDirectory(filesDirectory)
        val listFiles = mutableListOf<File>()
        listFiles.addAll(tripleOfFiles.first)
        listFiles.addAll(tripleOfFiles.second)
        listFiles.addAll(tripleOfFiles.third)
        val tempCache = mutableListOf<BookData>()
        val namesThatContains = loadCache(listFiles)
        val executorService = Executors.newFixedThreadPool(listFiles.size + 1)
        val cacheLock = ReentrantLock()
        listFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                executorService.execute {
                    val content = getDataOfBook(file.path)
                    cacheLock.lock()
                    try {
                        tempCache.add(content)
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