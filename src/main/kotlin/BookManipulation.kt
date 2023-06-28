import java.io.File

interface BookManipulation {
    fun printMetaBooksFromDirectory(directoryName: String): List<String>
}