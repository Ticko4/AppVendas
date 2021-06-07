package ipvc.estg.cm.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.Product
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


public class ProductsAdapter(
    productList: MutableList<Product>,
    listener: ProductsAdapterListener,
    private var context: Context
) : RecyclerView.Adapter<ProductsAdapter.MyViewHolder>(), Filterable {
    private val productList: MutableList<Product>
    private var productsListFiltered: MutableList<Product>
    private val listener: ProductsAdapterListener
    private val binderHelper = ViewBinderHelper()

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
        var price: TextView = view.findViewById(R.id.price)
        var content: TextView = view.findViewById(R.id.content)
        var swipeRevealLayout:SwipeRevealLayout = view.findViewById(R.id.swipe_layout)
        var image: ImageView = view.findViewById(R.id.image)
        var imagecopy: ImageView = view.findViewById(R.id.imageCopy)
        var favoriteIcon:ImageView = view.findViewById(R.id.favorite_icon)
        var favoriteCard:CardView = view.findViewById(R.id.favorite_card)
        var addCartIcon:ImageView = view.findViewById(R.id.add_cart)
        var addCartCard:CardView = view.findViewById(R.id.add_cart_card)
        var frontLayout: FrameLayout = view.findViewById(R.id.front_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_row_reveal, parent, false)
        binderHelper.setOpenOnlyOne(true);
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productsListFiltered[position]

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeRevealLayout, product.id.toString());
        holder.frontLayout.setOnClickListener { // send selected contact in callback
            listener.onProductSelected(productsListFiltered[position])
        }
        holder.title.text = product.name
        holder.price.text = context.resources.getString(R.string.price, BigDecimal(product.price.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
        holder.content.text = product.subcategory

        holder.favoriteCard.setOnClickListener { // send selected contact in callback
            if(product.favorite){
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_not_favorite
                    )
                )
                productsListFiltered[position].favorite = false
            }else{
                holder.favoriteIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_favorite
                    )
                )
                productsListFiltered[position].favorite = true
            }

            listener.onFavoriteClick(productsListFiltered[position])
        }

        holder.addCartCard.setOnClickListener {
            listener.onAddCartClick(productsListFiltered[position],holder.image,position)
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

    //filtrar o adaptador(FF)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                productsListFiltered = if (charString.isEmpty()) {
                    productList
                } else {
                    val filteredList: MutableList<Product> =
                        ArrayList()
                    for (row in productList) {
                        if (row.subcategory.toLowerCase(Locale.getDefault()).contains(charSequence)||row.name.toLowerCase().contains(
                                charSequence
                            ) || row.price.toString().toLowerCase().contains(charSequence)) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = productsListFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                productsListFiltered = filterResults.values as MutableList<Product>
                notifyDataSetChanged()
            }
        }
    }

    fun removeItem(position: Int) {
        productsListFiltered.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return productsListFiltered.size
    }

    override fun getItemId(position: Int): Long {
        return productsListFiltered[position].id.toLong()
    }

    //para nao utilizar o event list de forma a ir buscar os dados, usamos o proprio adaptador (FF)
    fun getItem(position: Int): Product {
        return productsListFiltered[position]
    }

    fun setQuantity(position: Int,quantity:Int){
        productList[position].quantity = quantity
        productsListFiltered[position].quantity = quantity
        productList[position].total = quantity * productList[position].price
        productsListFiltered[position].total = quantity * productList[position].price
    }

    interface ProductsAdapterListener {
        fun onProductSelected(product: Product?)
        fun onFavoriteClick(product: Product?)
        fun onAddCartClick(product: Product, image: ImageView,position: Int)
    }

    init {
        setHasStableIds(true)
        this.listener = listener
        this.productList = productList
        productsListFiltered = productList
    }
}