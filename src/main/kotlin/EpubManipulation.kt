import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread


class EpubManipulation(
    cacheDirectory: String,
) : BookManipulation {
    private val cacheFile = File(cacheDirectory)
    private val epubParser = EpubParser()
    private val fileCacheWriter = FileWriter(cacheFile, true)
    private val fileCacheReader = FileReader(cacheFile)

    override fun printMetaBooksFromDirectory(directoryName: String): List<String> {
        cachingFilesMetadata(directoryName)
        val result = FileReader(cacheFile).readLines()
        result.forEach() {
            println(it)
        }
        FileReader(cacheFile).close()
        return result
    }

    private fun findBooksInDirectory(filesDirectory: String): List<File> {
        val epubFiles = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            epubFiles
        } else {
            files.forEach {
                if (it.isDirectory) {
                    epubFiles.addAll(findBooksInDirectory(it.path))
                } else if (it.extension == FileExtensions.EPUB.stringVal) {
                    epubFiles.add(it)
                }
            }
            epubFiles
        }
    }

    private fun getDataOfBook(filePath: String): Pair<String, Int> {
        val parsedBook = epubParser.parseContent(filePath)
        val uniqueWords = regex.findAll(parsedBook)
            .map { it.value }
            .toSet()
        val file = File(filePath)
        return file.name to uniqueWords.size
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

    private fun writeCache(tempCache: List<String>) {
        if (tempCache.isEmpty()) return
        tempCache.forEach {
            fileCacheWriter.write("$it\n")
        }
        fileCacheWriter.close()
    }

    private fun cachingFilesMetadata(filesDirectory: String) {
        val epubFiles = findBooksInDirectory(filesDirectory)
        val tempCache = mutableListOf<String>()
        val namesThatContains = loadCache(epubFiles)
        val executorService = Executors.newCachedThreadPool()
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                val future = executorService.submit(Runnable {
                    val content = getDataOfBook(file.path)
                    tempCache.add("$content ${file.path}")
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