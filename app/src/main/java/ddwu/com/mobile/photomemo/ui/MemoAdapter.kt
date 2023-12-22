package ddwu.com.mobile.photomemo.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import ddwu.com.mobile.photomemo.R
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ListItemBinding

class MemoAdapter: RecyclerView.Adapter<MemoAdapter.MemoHolder>(){

    var memoList: List<MemoDto>? = null
    var itemClickListener: OnMemoItemClickListener? = null
    var itemLongClickListener: OnMemoItemLongClickListener? = null

    override fun getItemCount(): Int {
        return memoList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoHolder {
        val itemBinding = ListItemBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        return MemoHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MemoHolder, position: Int) {
        val dto = memoList?.get(position)
        holder.itemBinding.imageView.setImageResource(R.drawable.snowman)
        holder.itemBinding.tvData.text = dto?.title.toString()
        holder.itemBinding.tvData2.text = dto?.subTitle.toString()
        holder.itemBinding.tvData3.text = dto?.startDate.toString()
        holder.itemBinding.tvData4.text = dto?.endDate.toString()
        holder.itemBinding.clItem.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
        holder.itemBinding.clItem.setOnLongClickListener{
            itemLongClickListener?.onItemLongClick(position)
            true
        }
    }

    class MemoHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnMemoItemClickListener {
        fun onItemClick(position: Int)
    }
    interface OnMemoItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnMemoItemClickListener) {
        itemClickListener = listener
    }


    fun setOnItemLongClickListener(longListener: OnMemoItemLongClickListener){
        itemLongClickListener = longListener
    }

}