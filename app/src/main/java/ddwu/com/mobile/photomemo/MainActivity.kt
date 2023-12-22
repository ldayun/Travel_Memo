package ddwu.com.mobile.photomemo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.photomemo.data.MemoDao
import ddwu.com.mobile.photomemo.data.MemoDatabase
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ActivityMainBinding
import ddwu.com.mobile.photomemo.ui.MemoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val TAG = "MemoActivityTag"
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
                Log.d(TAG, "${adapter.memoList?.get(position)!!.id}")
                startActivity(intent)
            }
        })

        adapter.setOnItemLongClickListener(object : MemoAdapter.OnMemoItemLongClickListener{
            override fun onItemLongClick(position: Int) {
                Log.d(TAG, "longclick")
                AlertDialog.Builder(this@MainActivity).run{
                    setTitle("메모 삭제")
                    setMessage("${adapter.memoList?.get(position)!!.title} 를 삭제하시겠습니까?")

                    setPositiveButton("삭제", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int){
                            //movies.removeAt(position)
                            adapter.notifyDataSetChanged()
                            CoroutineScope(Dispatchers.IO).launch {
                                memoDao.deleteMemo(adapter.memoList?.get(position)!!.id)
                            }
                        }
                    })
                    setNegativeButton("취소", null)
                    show()
                }
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