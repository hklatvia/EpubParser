import java.io.File

class BooksFinderImpl : BooksFinder {
    override fun findBooksInDirectory(filesDirectory: String): BookStorage {
        val epubFiles = mutableListOf<File>()
        val txtFiles = mutableListOf<File>()
        val fb2Files = mutableListOf<File>()
        val files = File(filesDirectory).listFiles()
        return if (files == null) {
            return BookStorage(epubFiles, txtFiles, fb2Files)
        } else {
            files.forEach { file ->
                if (file.isDirectory) {
                    epubFiles.addAll(findBooksInDirectory(file.path).epubFiles
                        .filter {
                            it.extension == FileExtensions.EPUB.stringVal
                        })
                    txtFiles.addAll(findBooksInDirectory(file.path).txtFiles
                        .filter {
                            it.extension == FileExtensions.TXT.stringVal
                        })
                    fb2Files.addAll(findBooksInDirectory(file.path).fb2Files
                        .filter {
                            it.extension == FileExtensions.FB2.stringVal
                        })
                } else {
                    when (file.extension) {
                        FileExtensions.EPUB.stringVal -> epubFiles.add(file)
                        FileExtensions.TXT.stringVal -> txtFiles.add(file)
                        FileExtensions.FB2.stringVal -> fb2Files.add(file)
                    }
                }
            }
            return BookStorage(epubFiles, txtFiles, fb2Files)
        }
    }
}