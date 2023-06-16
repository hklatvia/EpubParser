import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.concurrent.thread
import java.util.concurrent.CyclicBarrier

class EpubManipulation(
    cacheDirectory: String,
) : BookManipulation {
    private val cacheFile = File(cacheDirectory)
    private val epubParse = EpubParse()
    private val fileWriter = FileWriter(cacheFile, true)
    private val fileReader = FileReader(cacheFile)

    companion object {
        private val regex = Regex("\b[A-Za-z]+\b")
    }

    override fun findBook(filesDirectory: String): List<File> {
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

    override fun getDataOfBook(filePath: String): Pair<String, Int> {
        val parsedBook = epubParse.parseContent(filePath)
        val uniqueWords = mutableSetOf<String>()
        val matches = regex.findAll(parsedBook)
        matches.forEach {
            uniqueWords.add(it.value)
        }
        val file = File(filePath)
        return file.name to uniqueWords.size
    }

    override fun checkCache(epubFiles: List<File>): List<String> {
        val lines = fileReader.readLines()
        return epubFiles.flatMap { file ->
            lines.filter { line ->
                line.contains(file.name)
            }
        }
    }

    override fun writeCache(tempCache: List<String>) {
        if (tempCache.isEmpty()) return
        tempCache.forEach {
            fileWriter.write("$it\n")
        }
        fileWriter.close()
    }

    override fun cacheFiles(filesDirectory: String) {
        val epubFiles = findBook(filesDirectory)
        var i = 0
        val tempCache = mutableListOf<String>()
        val barrier = CyclicBarrier(epubFiles.size + 1)
        var remainingThreads = epubFiles.size
        val namesThatContains = checkCache(epubFiles)
        epubFiles.forEach { file ->
            if (!namesThatContains.any { it.contains(file.name) }) {
                thread {
                    val content = getDataOfBook(file.path)
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