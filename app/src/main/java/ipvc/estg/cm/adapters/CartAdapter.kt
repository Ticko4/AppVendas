package ipvc.estg.cm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.Cart
import java.math.BigDecimal
import java.math.RoundingMode


public class CartAdapter(
    productList: MutableList<Cart>,
    listener: CartAdapterListener,
    private var context: Context
) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    private var productList: MutableList<Cart>
    private val listener: CartAdapterListener
    private val binderHelper = ViewBinderHelper()

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
        var price: TextView = view.findViewById(R.id.price)
        var content: TextView = view.findViewById(R.id.content)
        var swipeRevealLayout:SwipeRevealLayout = view.findViewById(R.id.swipe_layout)
       /* var viewBackground: RelativeLayout = view.findViewById(R.id.view_background)
        var viewForeground: RelativeLayout = view.findViewById(R.id.view_foreground)*/
        var image: ImageView = view.findViewById(R.id.image)
        var imagecopy: ImageView = view.findViewById(R.id.imageCopy)
        var favoriteIcon:ImageView = view.findViewById(R.id.favorite_icon)
        var favoriteCard:CardView = view.findViewById(R.id.favorite_card)
        var removeCartIcon:ImageView = view.findViewById(R.id.remove_cart)
        var removeCartCard:CardView = view.findViewById(R.id.remove_cart_card)
        /*var favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon)*/

        init {
            view.setOnClickListener { // send selected contact in callback
                listener.onProductSelected(productList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_row_reveal, parent, false)
        binderHelper.setOpenOnlyOne(true);
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeRevealLayout, product.id.toString());

        holder.title.text = product.name
        holder.price.text = product.quantity.toString() + " x " + context.resources.getString(R.string.price,BigDecimal(product.price.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
        holder.content.text = product.subcategory

        holder.favoriteCard.setOnClickListener { // send selected contact in callback
            if(product.favorite){
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_not_favorite
                    )
                )
                productList[position].favorite = false
            }else{
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_favorite
                    )
                )
                productList[position].favorite = true
            }

            listener.onFavoriteClick(productList[position])
        }

        holder.removeCartCard.setOnClickListener {
            listener.onRemoveCartClick(productList[position], holder.image, position)
        }

        if(product.favorite){
            holder.favoriteIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_favorite
                )
            )
        }else{
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
        productList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, productList.size);
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun getItemId(position: Int): Long {
        return productList[position].id!!.toLong()
    }

    //para nao utilizar o event list de forma a ir buscar os dados, usamos o proprio adaptador (FF)
    fun getItem(position: Int): Cart {
        return productList[position]
    }

    fun setQuantity(position: Int, quantity: Int){
        productList[position].quantity = quantity
        productList[position].price = quantity * productList[position].price
    }

    interface CartAdapterListener {
        fun onProductSelected(product: Cart?)
        fun onFavoriteClick(product: Cart?)
        fun onRemoveCartClick(product: Cart, image: ImageView, position: Int)
    }

    init {
        setHasStableIds(true)
        this.listener = listener
        this.productList = productList
    }
}