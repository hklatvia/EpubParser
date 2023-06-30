import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Executors

class Fb2Manipulation(
    cacheDirectory: String,
) : BookManipulation {
    private val fb2Parser = Fb2Parser()
    private val cacheFile = File(cacheDirectory)
    private val fileCacheWriter = FileWriter(cacheFile, true)
    private val fileCacheReader = FileReader(cacheFile)
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    override fun printMetaBooksFromDirectory(directoryName: String): List<String> {
        storeBookMetadata(directoryName)
        val result = FileReader(cacheFile).use { it.readLines() }
        result.forEach { println(it) }
        return result
    }

    private fun findBooksInDirectory(filesDirectory: String): List<File> {
        val fb2Files = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            fb2Files
        } else {
            files.forEach { file ->
                if (file.isDirectory) {
                    fb2Files.addAll(findBooksInDirectory(file.path))
                } else {
                    when (file.extension) {
                        FileExtensions.FB2.stringVal -> fb2Files.add(file)
                    }
                }
            }
            fb2Files
        }
    }

    private fun loadCache(fb2Files: List<File>): List<String> {
        val lines = fileCacheReader.readLines()
        val result = fb2Files.flatMap { file ->
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

    private fun getDataOfBook(filePath: String): BookData {
        val parsedBook = fb2Parser.parseContent(filePath)
        val uniqueWords = regex.findAll(parsedBook)
            .map { it.value }
            .toSet()
        val file = File(filePath)
        val author = fb2Parser.parseAuthorFromFb2(filePath)
        val title = file.nameWithoutExtension
        return BookData(author, title, uniqueWords.size, filePath)
    }

    private fun storeBookMetadata(filesDirectory: String) {
        val epubFiles = findBooksInDirectory(filesDirectory)
        val tempCache = mutableListOf<BookData>()
        val namesThatContains = loadCache(epubFiles)
        val executorService = Executors.newFixedThreadPool(epubFiles.size + 1)
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                val future = executorService.submit(Runnable {
                    val content = getDataOfBook(file.path)
                    tempCache.add(content)
                })
                future.get()
            }
        }
        executorService.shutdown()
        writeCache(tempCache)
    }

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}