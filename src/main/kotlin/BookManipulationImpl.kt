import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.Executors
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

abstract class BookManipulationImpl() : BookManipulation {

    private val epubParser = EpubParser()
    private val txtParser = TxtParser()
    private val fb2Parser = Fb2Parser()

    val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    fun findBooksInDirectory(filesDirectory: String): Triple<List<File>, List<File>, List<File>> {
        val epubFiles = mutableListOf<File>()
        val txtFiles = mutableListOf<File>()
        val fb2Files = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            Triple(epubFiles, txtFiles, fb2Files)
        } else {
            files.forEach { file ->
                if (file.isDirectory) {
                    epubFiles.addAll(findBooksInDirectory(file.path).first
                        .filter {
                            it.extension == FileExtensions.EPUB.stringVal
                        })
                    txtFiles.addAll(findBooksInDirectory(file.path).second
                        .filter {
                            it.extension == FileExtensions.TXT.stringVal
                        })
                    fb2Files.addAll(findBooksInDirectory(file.path).third.filter {
                        it.extension == FileExtensions.FB2.stringVal
                    })
                } else {
                    when (file.extension) {
                        FileExtensions.EPUB.stringVal -> epubFiles.add(file)
                        FileExtensions.TXT.stringVal -> txtFiles.add(file)
                        FileExtensions.FB2.stringVal -> fb2Files.add(file)
                    }
                }
            }
            Triple(epubFiles, txtFiles, fb2Files)
        }
    }

    override fun getDataOfBook(filePath: String): BookData {
        val file = File(filePath)
        val fileExtension = file.extension
        val parsedBook = when (fileExtension) {
            FileExtensions.EPUB.stringVal -> epubParser.parseContent(filePath)
            FileExtensions.TXT.stringVal -> txtParser.parseContent(filePath)
            FileExtensions.FB2.stringVal -> fb2Parser.parseContent(filePath)
            else -> throw IllegalArgumentException("Unsupported file format: $fileExtension")
        }
        val uniqueWords = regex.findAll(parsedBook)
            .map { it.value }
            .toSet()
        val title = file.nameWithoutExtension
        return when (fileExtension) {
            FileExtensions.EPUB.stringVal -> {
                val author = epubParser.parseAuthorFromEPUB(filePath) ?: ""
                BookData(author, title, uniqueWords.size, filePath)
            }

            FileExtensions.TXT.stringVal -> {
                BookData("There is not author txt files", title, uniqueWords.size, filePath)
            }

            FileExtensions.FB2.stringVal -> {
                val author = fb2Parser.parseAuthorFromFb2(filePath)
                BookData(author, title, uniqueWords.size, filePath)
            }

            else -> throw IllegalArgumentException("Unsupported file format: $fileExtension")
        }
    }

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}
