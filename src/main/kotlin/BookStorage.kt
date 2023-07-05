import java.io.File

data class BookStorage(
    val epubFiles: MutableList<File>,
    val txtFiles: MutableList<File>,
    val fb2Files: MutableList<File>,
)