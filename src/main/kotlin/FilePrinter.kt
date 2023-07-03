import java.io.FileReader

class FilePrinter : CacheManager() {

    fun printMetaBooksFromDirectory(directoryName: String): List<BookData> {
        storeBookMetadata(directoryName)
        val result = FileReader(cacheFile).use { reader ->
            gson.fromJson(reader, BookList::class.java).books
        }
        result.forEach { println(it) }
        return result
    }

}