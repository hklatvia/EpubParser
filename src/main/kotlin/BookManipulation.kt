import java.io.File

open interface BookManipulation {
    fun findBook(filesDirectory: String): List<File>
    fun getDataOfBook(filePath: String): Pair<String, Int>
    fun cacheFiles(filesDirectory: String)
    fun checkCache(epubFiles: List<File>): List<String>
    fun writeCache(tempCache: List<String>)

}