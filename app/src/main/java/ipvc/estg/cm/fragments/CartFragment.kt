package ipvc.estg.cm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.CartAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_cart_fragment.view.*
import java.util.*

class CartFragment:Fragment(), CartAdapter.CartAdapterListener {
    private var productsList: MutableList<Cart> = ArrayList<Cart>()
    private var mAdapter: CartAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var cartViewModel: CartViewModel
    private var itemCounter:TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_cart_fragment, container, false)
        setClickListeners(view)
        setAdapter()
        declareItems(view)
        setRecyclerView(view)
        fillRecyclerView()
        return view
    }

    private fun declareItems(view: View){
        //itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView
    }

    private fun setClickListeners(view: View){
        view.close_cart!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_cart)
        mLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                activity,
                LinearLayoutManager.VERTICAL
            )
        )
        recyclerView?.itemAnimator = DefaultItemAnimator()
    }

    private fun fillRecyclerView(){
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.allProducts.observe(viewLifecycleOwner, { it ->
            productsList = it as MutableList<Cart>
            mAdapter = context?.let { CartAdapter(productsList, this, it) }
            mAdapter!!.setHasStableIds(true)
            mAdapter!!.notifyItemRangeInserted(0, productsList.size - 1)
            recyclerView!!.adapter = mAdapter
        })
    }

    private fun setAdapter() {
        mAdapter = context?.let { CartAdapter(productsList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    override fun onProductSelected(product: Cart?) {
        TODO("Not yet implemented")
    }

    override fun onFavoriteClick(product: Cart?) {
        TODO("Not yet implemented")
    }

    override fun onRemoveCartClick(product: Cart, image: ImageView, position: Int) {
        mAdapter!!.removeItem(position)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        //itemCounter!!.text = (((itemCounter!!.text).toString()).toInt() - product.quantity).toString()
        cartViewModel.deleteById(product.id!!)
    }

}