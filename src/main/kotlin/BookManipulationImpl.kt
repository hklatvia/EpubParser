import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader

class BookManipulationImpl(
   val cacheDirectory: String
) : BookManipulation {
    private val cacheManager = CacheManagerImpl(this)
    private val epubParser = EpubParser()
    private val txtParser = TxtParser()
    private val fb2Parser = Fb2Parser()
    private val cacheFile = File(cacheDirectory)
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    fun printMetaBooksFromDirectory(directoryName: String): List<BookData> {
        cacheManager.storeBookMetadata(directoryName)
        val result = FileReader(cacheFile).use { reader ->
            gson.fromJson(reader, BookList::class.java).books
        }
        result.forEach { println(it) }
        return result
    }

    fun findBooksInDirectory(filesDirectory: String): BookStorage {
        val epubFiles = mutableListOf<File>()
        val txtFiles = mutableListOf<File>()
        val fb2Files = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            return BookStorage(epubFiles, txtFiles, fb2Files)
        } else {
            files.forEach { file ->
                if (file.isDirectory) {
                    epubFiles.addAll(findBooksInDirectory(file.path).epubFiles
                        .filter {
                            it.extension == FileExtensions.EPUB.stringVal
                        })
                    txtFiles.addAll(findBooksInDirectory(file.path).txtFiles
                        .filter {
                            it.extension == FileExtensions.TXT.stringVal
                        })
                    fb2Files.addAll(findBooksInDirectory(file.path).fb2Files
                        .filter {
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
            return BookStorage(epubFiles, txtFiles, fb2Files)
        }
    }

    override fun getDataOfBook(filePath: String): List<BookData> {
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

        val bookDataList = mutableListOf<BookData>()

        return when (fileExtension) {
            FileExtensions.EPUB.stringVal -> {
                val author = epubParser.parseAuthorFromEPUB(filePath) ?: ""
                bookDataList.add(BookData(author, title, uniqueWords.size, filePath))
                bookDataList
            }

            FileExtensions.TXT.stringVal -> {
                bookDataList.add(BookData("There is no author for txt files", title, uniqueWords.size, filePath))
                bookDataList
            }

            FileExtensions.FB2.stringVal -> {
                val author = fb2Parser.parseAuthorFromFb2(filePath)
                bookDataList.add(BookData(author, title, uniqueWords.size, filePath))
                bookDataList
            }

            else -> throw IllegalArgumentException("Unsupported file format: $fileExtension")
        }
    }

    companion object {
        val regex = Regex("\\b[A-Za-z]+\\b")
    }
}
