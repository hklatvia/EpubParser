import java.io.File

interface CacheManager {
    fun loadCache(epubFiles: List<File>): List<String>
    fun writeCache(tempCache: List<BookData>)
}