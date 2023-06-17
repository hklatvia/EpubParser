import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.concurrent.thread
import java.util.concurrent.CyclicBarrier
import kotlin.system.measureTimeMillis

class EpubManipulation(
    cacheDirectory: String,
) : BookManipulation {
    private val cacheFile = File(cacheDirectory)
    private val epubParse = EpubParse()
    private val fileWriterCache = FileWriter(cacheFile, true)
    private val fileReaderCache = FileReader(cacheFile)

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }

    private fun findBook(filesDirectory: String): List<File> {
        val epubFiles = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            epubFiles
        } else {
            files.forEach {
                if (it.isDirectory) {
                    epubFiles.addAll(findBook(it.path))
                } else if (it.extension == Extentions.EPUB.stringVal) {
                    epubFiles.add(it)
                }
            }
            epubFiles
        }
    }

    private fun getDataOfBook(filePath: String): Pair<String, Int> {
        val parsedBook = epubParse.parseContent(filePath)
        val uniqueWords = regex.findAll(parsedBook)
            .map { it.value }
            .toSet()
        val file = File(filePath)
        return file.name to uniqueWords.size
    }

    private fun checkCache(epubFiles: List<File>): List<String> {
        val lines = fileReaderCache.readLines()
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
            fileWriterCache.write("$it\n")
        }
        fileWriterCache.close()
    }

    private fun cacheFiles(filesDirectory: String) {
        val epubFiles = findBook(filesDirectory)
        val tempCache = mutableListOf<String>()
        val barrier = CyclicBarrier(epubFiles.size + 1)
        var remainingThreads = epubFiles.size
        val namesThatContains = checkCache(epubFiles)
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                thread {
                    val content = getDataOfBook(file.path)
                    synchronized(tempCache) {
                        tempCache.add("$content ${file.path}")
                    }
                    remainingThreads--
                    barrier.await()
                }
            } else remainingThreads--
        }
        while (remainingThreads > 0) {
            barrier.await()
        }
        writeCache(tempCache)
    }

    override fun printMetaBooksFromDirectory(directoryName: String): List<String> {
        cacheFiles(directoryName)
        val result = FileReader(cacheFile).readLines()
        result.forEach() {
            println(it)
        }
        FileReader(cacheFile).close()
        return result
    }
}