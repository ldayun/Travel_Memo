package ddwu.com.mobile.photomemo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memo_table")
    fun getAllMemos() : Flow<List<MemoDto>>

    @Query("SELECT * FROM memo_table WHERE id = :id")
    suspend fun getMemoById(id: Long) : List<MemoDto>

    @Insert
    suspend fun insertMemo(memo: MemoDto)

    @Update
    suspend fun updateMemo(memo: MemoDto)

    @Delete
    suspend fun deleteMemo(memo: MemoDto)
}