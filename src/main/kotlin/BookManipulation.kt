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
        val nameOfBook = file.name
        val parsedBook = EpubParse().parseBook(file)
        var uniqueWords = mutableSetOf<String>()
        val regex = Regex("\\b[A-Za-z]+\\b")
        val matches = regex.findAll(parsedBook)
        matches.forEach {
            uniqueWords.add(it.value)
        }

        return nameOfBook to uniqueWords.size
    }

    override fun cachingFiles() {
        val epubFiles = findBook(filesDirectoryFile)
        val fileReader = FileReader(cacheFile)
        val lines = fileReader.readLines()
        var namesThatContains = mutableListOf<String>()
        epubFiles.forEach { file ->
            lines.forEach { line ->
                if (line.contains(file.name)) {
                    namesThatContains.add(line)
                }
            }
        }
        fileReader.close()
        var i = 0
        var tempCache = mutableListOf<String>()
        val lock = ReentrantLock()
        val barrier = CyclicBarrier(epubFiles.size + 1)
        var remainingThreads = epubFiles.size
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                val thread = thread {
                    val content = getDataOfBook(file)
                    println("$content ${file.path}")
                    lock.lock()
                    tempCache.add("${content.toString()} ${file.path}")
                    remainingThreads--
                    lock.unlock()
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

        val fileWriter = FileWriter(cacheFile, true)
        tempCache.forEach {
            fileWriter.write(it)
            fileWriter.write("\n")
        }
        fileWriter.close()
    }
}