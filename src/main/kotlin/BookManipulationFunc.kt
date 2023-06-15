import java.io.File

open interface BookManipulationFunc {
    fun findBook(filesDirectory: File): List<File>
    fun getDataOfBook(file: File): Pair<String, Int>
    fun cachingFiles()

}пше