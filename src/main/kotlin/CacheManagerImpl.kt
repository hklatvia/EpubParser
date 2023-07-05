import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class CacheManagerImpl(private val bookManipulation: BookManipulationImpl) : CacheManager {
    private val cacheFile: File = File(bookManipulation.cacheDirectory)
    private val fileCacheWriter: FileWriter = FileWriter(cacheFile, true)
    private val fileCacheReader: FileReader = FileReader(cacheFile)
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

     override fun loadCache(epubFiles: List<File>): List<String> {
        val lines = fileCacheReader.readLines()
        val result = epubFiles.flatMap { file ->
            lines.filter { line ->
                line.contains(file.name)
            }
        }
        return result
    }

     override fun writeCache(tempCache: List<BookData>) {
        if (tempCache.isEmpty()) return
        val bookList = BookList(tempCache)
        val json = gson.toJson(bookList)
        fileCacheWriter.write(json)
        fileCacheWriter.close()
    }
}