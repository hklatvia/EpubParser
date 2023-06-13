import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import java.util.concurrent.CyclicBarrier

class BookManipulation(
    private val cacheDirectory: File = File("C:\\Users\\aleks\\IdeaProjects\\untitled9\\cache"),
    val filesDirectory: File
) {


    fun findEpub(filesDirectory: File): List<File> {
        var epubFiles = mutableListOf<File>()
        var files = filesDirectory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    epubFiles.addAll(findEpub(file))
                } else if (file.extension == "epub") {
                    epubFiles.add(file)
                }
            }
        }
        return epubFiles
    }

    fun getDataOfBook(file: File): Pair<String, Int> {
        val nameOfBook = file.name
        val parsedBook = EpubParse().parseBook(file)
        var uniqueWords = mutableSetOf<String>()
        val regex = Regex("\\b[A-Za-z]+\\b")
        val matches = regex.findAll(parsedBook)
        for (match in matches) {
            uniqueWords.add(match.value)
        }

        return Pair(nameOfBook, uniqueWords.size)
    }

    fun cachingFiles() {
        val epubFiles = findEpub(filesDirectory)
        val fileReader = FileReader(cacheDirectory)
        val lines = fileReader.readLines()
        var namesThatContains = mutableListOf<String>()
        for (file in epubFiles) {
            for (line in lines) {
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
        for (file in epubFiles) {
            if (!namesThatContains.any { it.contains(file.name) }) {
                val thread = thread {
                    val content = getDataOfBook(file)
                    println("$content ${file.path}")
                    lock.lock()
                    tempCache.add(content.toString() + " " + file.path)
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

        val fileWriter = FileWriter(cacheDirectory, true)
        tempCache.forEach {
            fileWriter.write(it)
            fileWriter.write("\n")
        }
        fileWriter.close()
    }
}