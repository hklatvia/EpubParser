import java.io.File

interface BookManipulation {
    fun getDataOfBook(filePath: String): BookData
}