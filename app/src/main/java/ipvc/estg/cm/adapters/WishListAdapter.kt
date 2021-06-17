package ipvc.estg.cm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.gson.Gson
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.entities.EntityProd
import ipvc.estg.cm.entities.Subcategory
import java.math.BigDecimal
import java.math.RoundingMode


class WishListAdapter(
    productList: MutableLiveData<MutableList<Cart>>,
    listener: WishListAdapterListener,
    private var context: Context
) : RecyclerView.Adapter<WishListAdapter.MyViewHolder>() {
    private var productList: MutableLiveData<MutableList<Cart>>
    private val listener: WishListAdapterListener
    private val binderHelper = ViewBinderHelper()

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
        var price: TextView = view.findViewById(R.id.price)
        var subcategory: TextView = view.findViewById(R.id.subcategory)
        var entity: TextView = view.findViewById(R.id.entity)
        var swipeRevealLayout: SwipeRevealLayout = view.findViewById(R.id.swipe_layout)
        var image: ImageView = view.findViewById(R.id.image)
        var imagecopy: ImageView = view.findViewById(R.id.imageCopy)
        var favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon)
        var favoriteCard: CardView = view.findViewById(R.id.favorite_card)
        var addCartIcon: ImageView = view.findViewById(R.id.add_cart)
        var addCartCard: CardView = view.findViewById(R.id.add_cart_card)
        var frontLayout: FrameLayout = view.findViewById(R.id.front_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_row_reveal, parent, false)
        binderHelper.setOpenOnlyOne(true)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList.value!![position]

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeRevealLayout, product.id.toString());
        holder.frontLayout.setOnClickListener { // send selected contact in callback
            listener.onProductSelected(productList.value!![position])
        }
        holder.title.text = product.name
        holder.price.text = context.resources.getString(
            R.string.price,
            BigDecimal(product.price.toString()).setScale(2, RoundingMode.HALF_EVEN).toString()
                .replace('.', ',')
        )
        val gson = Gson()
        val subcategory = gson.fromJson(product.subcategory, Subcategory::class.java)
        val entity = gson.fromJson(product.entity, EntityProd::class.java)
        holder.subcategory.text = subcategory.name
        holder.entity.text = entity.name

        holder.favoriteCard.setOnClickListener { // send selected contact in callback
            if (product.favorite) {
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_not_favorite
                    )
                )
                productList.value!![position].favorite = false
            } else {
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_favorite
                    )
                )
                productList.value!![position].favorite = true
            }

            listener.onFavoriteClick(productList.value!![position],position)
        }

        holder.addCartCard.setOnClickListener {
            listener.onAddCartClick(productList.value!![position], holder.image, position)
        }

        if (product.favorite) {
            holder.favoriteIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_favorite
                )
            )
        } else {
            holder.favoriteIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_not_favorite
                )
            )
        }

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val requestOptions = RequestOptions()
        requestOptions.placeholder(circularProgressDrawable)
        requestOptions.skipMemoryCache(true)
        requestOptions.fitCenter()

        Glide.with(context).load(product.image).apply(requestOptions).into(holder.image)
        Glide.with(context).load(product.image).apply(requestOptions).into(holder.imagecopy)
    }

    fun removeItem(position: Int) {
        productList.value!!.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, productList.value!!.size)
    }


    override fun getItemCount(): Int {
        if(productList.value == null){
            return 0
        }
        return productList.value!!.size
    }

    override fun getItemId(position: Int): Long {
        return productList.value!![position].id!!.toLong()
    }

    //para nao utilizar o event list de forma a ir buscar os dados, usamos o proprio adaptador (FF)
    fun getItem(position: Int): Cart {
        return productList.value!![position]
    }

    fun setQuantity(position: Int, quantity: Int, product: Cart) {
        productList.value!![position].quantity = quantity
        productList.value!![position].total = quantity * productList.value!![position].price
        notifyItemChanged(position, product)
    }

    interface WishListAdapterListener {
        fun onProductSelected(product: Cart?)
        fun onFavoriteClick(product: Cart?,position: Int)
        fun onAddCartClick(product: Cart, image: ImageView, position: Int)
    }

    init {
        setHasStableIds(true)
        this.listener = listener
        this.productList = productList
    }
}