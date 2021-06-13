package ipvc.estg.cm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
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


class CartAdapter(productList: MutableList<Cart>, listener: CartAdapterListener, private var context: Context) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    private var productList: MutableList<Cart>
    private val listener: CartAdapterListener
    private val binderHelper = ViewBinderHelper()

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
        var price: TextView = view.findViewById(R.id.price)
        var content: TextView = view.findViewById(R.id.content)
        var swipeRevealLayout:SwipeRevealLayout = view.findViewById(R.id.swipe_layout)
        var image: ImageView = view.findViewById(R.id.image)
        var imagecopy: ImageView = view.findViewById(R.id.imageCopy)
        var removeCartCard:CardView = view.findViewById(R.id.remove_cart_card)
        var quantity: TextView = view.findViewById(R.id.quantity)
        var quantityAddMode: TextView = view.findViewById(R.id.quantity_add_mode)
        var increment:CardView = view.findViewById(R.id.increment)
        var decrement:CardView = view.findViewById(R.id.decrement)
        var quantityAddModeLayout: LinearLayout = view.findViewById(R.id.quantity_add_mode_layout)
        var quantityCard: CardView = view.findViewById(R.id.quantity_card)
        var quantityAddModeCard: CardView = view.findViewById(R.id.quantity_add_mode_card)
        var frontLayout: FrameLayout = view.findViewById(R.id.front_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_row_reveal, parent, false)
        binderHelper.setOpenOnlyOne(true)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeRevealLayout, product.id.toString())
        holder.frontLayout.setOnClickListener { // send selected contact in callback
            listener.onProductSelected(productList[position])
        }
        holder.title.text = product.name
        holder.price.text = context.resources.getString(
            R.string.price, BigDecimal(product.price.toString()).setScale(
                2,
                RoundingMode.HALF_EVEN
            ).toString().replace('.', ',')
        )
        holder.content.text = product.subcategory

        holder.quantity.text = product.quantity.toString()
        holder.quantityAddMode.text = product.quantity.toString()

        holder.removeCartCard.setOnClickListener {
            listener.onRemoveCartClick(productList[position], holder.image, position)
        }

        holder.increment.setOnClickListener {
            listener.onIncrementClick(productList[position], position)
        }

        holder.decrement.setOnClickListener {
            listener.onDecrementClick(productList[position], position)
        }

        holder.quantityCard.setOnClickListener {
            val fadeIn = ScaleAnimation(
                0.3f,
                1f,
                0.4f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.9f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
            )
            fadeIn.duration = 500 // animation duration in milliseconds

            fadeIn.fillAfter =
                true // If fillAfter is true, the transformation that this animation performed will persist when it is finished.

            holder.quantityAddModeLayout.startAnimation(fadeIn)
            holder.quantityAddModeLayout.visibility = View.VISIBLE
        }

        holder.quantityAddModeCard.setOnClickListener {
            val fadeOut = ScaleAnimation(
                1f,
                0.3f,
                1f,
                0.4f,
                Animation.RELATIVE_TO_SELF,
                0.9f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
            )
            fadeOut.duration = 500 // animation duration in milliseconds

            fadeOut.fillAfter =
                false // If fillAfter is true, the transformation that this animation performed will persist when it is finished.

            holder.quantityAddModeLayout.startAnimation(fadeOut)
            holder.quantityAddModeLayout.visibility = View.GONE
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
        notifyItemRangeChanged(position, productList.size)
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

    fun setQuantity(position: Int, quantity: Int,product: Cart){
        productList[position].quantity = quantity
        productList[position].total = quantity * productList[position].price
        notifyItemChanged(position,product)
    }

    interface CartAdapterListener {
        fun onProductSelected(product: Cart?)
        fun onRemoveCartClick(product: Cart, image: ImageView, position: Int)
        fun onIncrementClick(product: Cart, position: Int)
        fun onDecrementClick(product: Cart, position: Int)
    }

    init {
        setHasStableIds(true)
        this.listener = listener
        this.productList = productList
    }
}