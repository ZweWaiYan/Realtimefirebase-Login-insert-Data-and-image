package coder.mtk.text_3.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coder.mtk.text_3.R
import coder.mtk.text_3.model.Item
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_itemlist.view.*

class ItemsAdapter : RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> (){

    private var itemsList : ArrayList<Item> = ArrayList()

    private var clickListener : ClickListener? = null

    fun setOnClickListener (clickListener:ClickListener) {
        this.clickListener = clickListener
    }

    fun updateItemsList (itemsList : ArrayList<Item>){
        this.itemsList = itemsList
        notifyDataSetChanged()
    }

    inner class ItemsViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
        }

        lateinit var item: Item

        fun bind(item : Item){
            this.item = item
            itemView.txtItemName_ItemList.text = item.name
            itemView.txtQty_itemList.text = item.qty.toString()
            itemView.txtPriceKyt_itemList.text = item.price.toString()
            Picasso.get()
                .load(item.itemImageUrl)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(itemView.img_itemList)
        }

        override fun onClick(p0: View?) {
            clickListener?.onClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_itemlist,parent,false)
        return ItemsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
       holder.bind(itemsList[position])
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    interface ClickListener {
        fun onClick(item: Item)
    }
}