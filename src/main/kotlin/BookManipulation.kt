import java.io.File

open interface BookManipulation {
    fun printMetaBooksFromDirectory(directoryName: String): List<String>

}