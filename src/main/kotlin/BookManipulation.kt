import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import java.util.concurrent.CyclicBarrier

class BookManipulation(
    cacheDirectory: String,
    filesDirectory: String,
) : BookManipulationFunc {
    private val cacheFile = File(cacheDirectory)
    private val filesDirectoryFile = File(filesDirectory)
    private val epubParse = EpubParse()
    override fun findBook(filesDirectory: File): List<File> {
        var epubFiles = mutableListOf<File>()
        var files = filesDirectory.listFiles()
        return if (files == null) {
            epubFiles
        } else {
            files.forEach {
                if (it.isDirectory) {
                    epubFiles.addAll(findBook(it))
                } else if (it.extension == Extentions.EPUB.type) {
                    epubFiles.add(it)
                }
            }
            epubFiles
        }
    }

    override fun getDataOfBook(file: File): Pair<String, Int> {
        val parsedBook = epubParse.parseBook(file)
        var uniqueWords = mutableSetOf<String>()
        val wordTemplate = "\\b[A-Za-z]+\\b"
        val regex = Regex(wordTemplate)
        val matches = regex.findAll(parsedBook)
        matches.forEach {
            uniqueWords.add(it.value)
        }
        return file.name to uniqueWords.size
    }

    private val epubFiles = findBook(filesDirectoryFile)
    private val fileReader = FileReader(cacheFile)
    override fun checkCache(): List<String> {
        val lines = fileReader.readLines()
        return epubFiles.flatMap { file ->
            lines.filter { line ->
                line.contains(file.name)
            }
        }
    }

    private val fileWriter = FileWriter(cacheFile, true)
    override fun writeCache(tempCache: List<String>) {
        if (tempCache.isEmpty()) return
        else {
            tempCache.forEach {
                fileWriter.write("$it\n")
            }
        }
        fileWriter.close()
    }

    private val namesThatContains = checkCache()

    override fun cacheFiles() {
        var i = 0
        var tempCache = mutableListOf<String>()
        val barrier = CyclicBarrier(epubFiles.size + 1)
        var remainingThreads = epubFiles.size
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                val thread = thread {
                    val content = getDataOfBook(file)
                    println("$content ${file.path}")
                    synchronized(tempCache) {
                        tempCache.add("$content ${file.path}")
                        remainingThreads--
                    }
                    barrier.await()
                }
            } else {
                println(namesThatContains[i])
                remainingThreads--
                i++
            }
        }
        while (remainingThreads > 0) {
            barrier.await()
        }
        writeCache(tempCache)
    }
}