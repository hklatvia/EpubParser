interface BooksFinder {
    fun findBooksInDirectory(filesDirectory: String): BookStorage
}