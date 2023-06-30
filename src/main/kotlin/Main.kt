import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    val hui = "C:/Users/aleks/IdeaProjects/epubepub/src/main"
    println("Enter your epub book's path: ")
    val directoryOfFiles = readln()
    val cacheDirectory = "C:\\Users\\aleks\\IdeaProjects\\epubepub\\cache"
    val start = System.currentTimeMillis()
    EpubManipulation(
        cacheDirectory = cacheDirectory,
    ).printMetaBooksFromDirectory("C:/Users/aleks/IdeaProjects/epubepub/src/main")

////    val txtBook = "C:\\Users\\aleks\\IdeaProjects\\epubepub\\src\\main\\resources\\txt.txt"
////    println( TxtParser().parseContent(txtBook))
//    TxtManipulation(cacheDirectory).printMetaBooksFromDirectory(directoryOfFiles)
    val fb2path = "C:\\Users\\aleks\\IdeaProjects\\epubepub\\src\\main\\resources\\fb2.fb2"
    val result =
        Fb2Manipulation(cacheDirectory).printMetaBooksFromDirectory("C:/Users/aleks/IdeaProjects/epubepub/src/main")
    val end = System.currentTimeMillis()
    println(end - start)
}

