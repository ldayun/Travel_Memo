package ddwu.com.mobile.photomemo.ui

import androidx.recyclerview.widget.RecyclerView
import ddwu.com.mobile.photomemo.data.Tour
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import ddwu.com.mobile.photomemo.R

class TourAdapter(var items : ArrayList<Tour>) : RecyclerView.Adapter<TourAdapter.ViewHolder>()  {
    // 뷰 홀더 만들어서 반환, 뷰릐 레이아웃은 list_item_tour.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tour, parent, false)
        return ViewHolder(itemView)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: TourAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    // 아이템 갯수 리턴
    override fun getItemCount() = items.count()

    // 뷰 홀더 설정
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun setItem(item : Tour) {
            val imgFirstImage = itemView.findViewById<ImageView>(R.id.imgFirstImage)    // 이미지
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)                 // 제목
            val tvAddr1 = itemView.findViewById<TextView>(R.id.tvAddr1)                 // 주소
            val tvAddr2 = itemView.findViewById<TextView>(R.id.tvAddr2)                 // 상세 주소

            Glide.with(imgFirstImage)
                .load(item.firstimage)
                .error(R.drawable.ic_launcher_foreground)                  // 오류 시 이미지
                .into(imgFirstImage)
            tvTitle.text = item.title
            tvAddr1.text = item.addr1
            tvAddr2.text = item.addr2

        }
    }
}