package ddwu.com.mobile.photomemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.photomemo.data.MemoDao
import ddwu.com.mobile.photomemo.data.MemoDatabase
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ActivityMainBinding
import ddwu.com.mobile.photomemo.ui.MemoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val mainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val memoDB : MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao : MemoDao by lazy {
        memoDB.memoDao()
    }

    val adapter : MemoAdapter by lazy {
        MemoAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        mainBinding.rvMemo.adapter = adapter
        mainBinding.rvMemo.layoutManager = LinearLayoutManager(this)

        mainBinding.btnAdd.setOnClickListener {
            val intent = Intent (this, AddMemoActivity::class.java)
            startActivity(intent)
        }

        mainBinding.btnAdd2.setOnClickListener {
            val intent = Intent (this, RecommendActivity::class.java)
            startActivity(intent)
        }

        adapter.setOnItemClickListener(object: MemoAdapter.OnMemoItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent (this@MainActivity, ShowMemoActivity::class.java )
                intent.putExtra("memoDto", adapter.memoList?.get(position))
                startActivity(intent)
            }
        })
        showAllMemo()
    }

    fun showAllMemo() {
        CoroutineScope(Dispatchers.Main).launch {
            memoDao.getAllMemos().collect { memos ->
                adapter.memoList = memos
                adapter.notifyDataSetChanged()
            }
        }
    }

}