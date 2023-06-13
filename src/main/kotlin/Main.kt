import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val hui = "C:/Users/aleks/IdeaProjects/untitled9/src/main"
    println("Enter your epub book's path: ")
    val directoryOfFiles=File(readln())
    val cacheFile = File("C:\\Users\\aleks\\IdeaProjects\\untitled8\\cache")
    val executionTime = measureTimeMillis {
        BookManipulation(filesDirectory = directoryOfFiles).cachingFiles()//изменить логику передавать параметр в объект
    }
    println(executionTime)
}
