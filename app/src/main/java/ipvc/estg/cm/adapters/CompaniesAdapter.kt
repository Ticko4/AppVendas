package ipvc.estg.cm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.ViewBinderHelper
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.Company
import java.util.*

class CompaniesAdapter(
    entityList: MutableList<Company>,
    listener: EntitiesAdapterListener,
    private var context: Context
) : RecyclerView.Adapter<CompaniesAdapter.MyViewHolder>(), Filterable {
    private val entityList: MutableList<Company>
    private var entitiesListFiltered: MutableList<Company>
    private val listener: EntitiesAdapterListener
    private val binderHelper = ViewBinderHelper()

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title_entity)
        var image: ImageView = view.findViewById(R.id.image_entity)
        var frontLayout: FrameLayout = view.findViewById(R.id.view_foreground_entity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.entity_row, parent, false)
        binderHelper.setOpenOnlyOne(true);
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CompaniesAdapter.MyViewHolder, position: Int) {
        val entity = entitiesListFiltered[position]

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data

        /*binderHelper.bind(holder.swipeRevealLayout, entity.id.toString());*/

        holder.frontLayout.setOnClickListener { // send selected contact in callback
            listener.onEntitySelected(entitiesListFiltered[position])
        }

        holder.title.text = entity.name

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val requestOptions = RequestOptions()
        requestOptions.placeholder(circularProgressDrawable)
        requestOptions.skipMemoryCache(true)
        requestOptions.fitCenter()

        Glide.with(context).load(entity.image).apply(requestOptions).into(holder.image)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                entitiesListFiltered = if (charString.isEmpty()) {
                    entityList
                } else {
                    val filteredList: MutableList<Company> =
                        ArrayList()
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = entitiesListFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                entitiesListFiltered = filterResults.values as MutableList<Company>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return entitiesListFiltered.size
    }

    override fun getItemId(position: Int): Long {
        return entitiesListFiltered[position].id.toLong()
    }

    fun getItem(position: Int): Company {
        return entitiesListFiltered[position]
    }

    interface EntitiesAdapterListener {
        fun onEntitySelected(entity: Company?)
    }

    init {
        setHasStableIds(true)
        this.listener = listener
        this.entityList = entityList
        entitiesListFiltered = entityList
    }
}