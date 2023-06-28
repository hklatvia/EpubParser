import kotlin.system.measureTimeMillis

fun main() {
    val hui = "C:/Users/aleks/IdeaProjects/epubepub/src/main"
    println("Enter your epub book's path: ")
    val directoryOfFiles = readln()
    val cacheDirectory = "C:\\Users\\aleks\\IdeaProjects\\epubepub\\cache"
    val start = System.currentTimeMillis()
        EpubManipulation(
            cacheDirectory = cacheDirectory,
        ).printMetaBooksFromDirectory(directoryOfFiles)
    val end = System.currentTimeMillis()
    println(end - start)
}

