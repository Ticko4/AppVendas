package ipvc.estg.cm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.Product

class EntitiesAdapter(
    EntityList: MutableList<Product>,
    listener: ProductsAdapter.ProductsAdapterListener,
    private var context: Context
) : RecyclerView.Adapter<ProductsAdapter.MyViewHolder>(), Filterable {
    var listener: ProductsByEntityListener? = null
    private lateinit var arrList : MutableList<Product>

    interface ProductsByEntityListener {
        fun onEntityPress(product: Product?, position: Int)
        fun onEntityLongPress(product: Product?,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsAdapter.MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.product_show, parent, false)
        return MyViewHolder(itemView)
    }

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView? = null

        init {
            title = view.findViewById(R.id.title_product)
        }
    }

    override fun onBindViewHolder(holder: ProductsAdapter.MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }

}