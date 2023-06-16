import kotlin.system.measureTimeMillis

fun main() {
    val hui = "C:/Users/aleks/IdeaProjects/untitled11/src/main"
    println("Enter your epub book's path: ")
    val directoryOfFiles = readln()
    val cacheDirectory = "C:\\Users\\aleks\\IdeaProjects\\untitled11\\cache"
    val executionTime = measureTimeMillis {
        EpubManipulation(
            cacheDirectory = cacheDirectory,
        ).cacheFiles(directoryOfFiles)
    }
    println(executionTime)
}

