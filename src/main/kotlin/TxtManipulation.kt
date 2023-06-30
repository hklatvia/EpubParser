import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Executors

class TxtManipulation(
    cacheDirectory: String,
) : BookManipulation {
    private val txtParser = TxtParser()
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
        val txtFiles = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            txtFiles
        } else {
            files.forEach { file ->
                if (file.isDirectory) {
                    txtFiles.addAll(findBooksInDirectory(file.path))
                } else {
                    when (file.extension) {
                        FileExtensions.TXT.stringVal -> txtFiles.add(file)
                    }
                }
            }
            txtFiles
        }
    }

    private fun getDataOfBook(filePath: String): BookData {
        val parsedBook = txtParser.parseContent(filePath)
        val uniqueWords = regex.findAll(parsedBook)
            .map { it.value }
            .toSet()
        val file = File(filePath)
        val title = file.nameWithoutExtension
        return BookData("There is not author txt files", title, uniqueWords.size, filePath)
    }

    private fun loadCache(txtFiles: List<File>): List<String> {
        val lines = fileCacheReader.readLines()
        val result = txtFiles.flatMap { file ->
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

    private fun storeBookMetadata(filesDirectory: String) {
        val txtFiles = findBooksInDirectory(filesDirectory)
        val tempCache = mutableListOf<BookData>()
        val namesThatContains = loadCache(txtFiles)
        val executorService = Executors.newFixedThreadPool(txtFiles.size + 1)
        txtFiles.forEach { file ->
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