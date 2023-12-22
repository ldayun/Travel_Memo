package ddwu.com.mobile.photomemo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "memo_table")
data class MemoDto(
    @PrimaryKey (autoGenerate = true)
    val id: Long,
    var startDate: String,
    var endDate: String,
    var title: String,
    var subTitle: String,
    var location: String,
    var memo: String) : Serializable {
    override fun toString(): String {
        return "${id}  ${title} : (${startDate} ~ ${endDate})"
    }
}
