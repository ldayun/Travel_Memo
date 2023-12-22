package ddwu.com.mobile.photomemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ddwu.com.mobile.photomemo.data.MemoDao
import ddwu.com.mobile.photomemo.data.MemoDatabase
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ActivityAddMemoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddMemoActivity : AppCompatActivity() {


    val addMemoBinding by lazy {
        ActivityAddMemoBinding.inflate(layoutInflater)
    }

    val memoDB: MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao: MemoDao by lazy {
        memoDB.memoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(addMemoBinding.root)

        /*DB 에 memoDto 저장*/
        addMemoBinding.btnAdd.setOnClickListener {
                val startDate = addMemoBinding.tvStartDate.text.toString()
                val endDate = addMemoBinding.tvEndDate.text.toString()
                val memo = addMemoBinding.tvAddMemo2.text.toString()
                val title = addMemoBinding.tvTitle.text.toString()
                val subTitle = addMemoBinding.tvSubTitle.text.toString()
                val location = addMemoBinding.tvAddLocation.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.insertMemo(MemoDto(0, startDate, endDate, title, subTitle, location, memo))
                }

                Toast.makeText(this@AddMemoActivity, "New memo is added!", Toast.LENGTH_SHORT)
                    .show()

        }


        addMemoBinding.btnCancel.setOnClickListener {
            finish()
        }
    }



}