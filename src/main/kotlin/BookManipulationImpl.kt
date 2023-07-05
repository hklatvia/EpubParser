import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader

class BookManipulationImpl(
   val cacheDirectory: String
) : BookManipulation {
    private val cacheFile = File(cacheDirectory)
    private val bookDataProcessor = BookDataProcessorImpl(this)
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    override fun printMetaBooksFromDirectory(directoryName: String) {
        bookDataProcessor.storeBookMetadata(directoryName)
        val result = FileReader(cacheFile).use { reader ->
            gson.fromJson(reader, BookList::class.java).books
        }
        result.forEach { println(it) }
    }
}
