import com.google.gson.annotations.SerializedName

data class BookData(
    @SerializedName("author")
    val author: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("UniqueWordCount")
    val wordCount: Int,
    @SerializedName("bookPath")
    val bookPath: String
)