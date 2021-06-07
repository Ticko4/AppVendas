package ipvc.estg.cm.fragments

import android.os.Bundle
import android.util.Log
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
import org.w3c.dom.Text
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class CartFragment:Fragment(), CartAdapter.CartAdapterListener {
    private var productsList: MutableList<Cart> = ArrayList<Cart>()
    private var mAdapter: CartAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var cartViewModel: CartViewModel
    private var subtotal: TextView? = null
    private var total: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_cart_fragment, container, false)
        subtotal = view.findViewById(R.id.subtotal_val)
        total = view.findViewById(R.id.total_val)
        setClickListeners(view)
        setAdapter()
        setRecyclerView(view)
        fillRecyclerView()
        return view
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
            if(recyclerView!!.adapter == null){
                productsList = it as MutableList<Cart>
                mAdapter = context?.let { CartAdapter(productsList, this, it) }
                mAdapter!!.setHasStableIds(true)
                mAdapter!!.notifyItemRangeInserted(0, productsList.size - 1)
                recyclerView!!.adapter = mAdapter
            }

        })


        cartViewModel.getTotal().observe(viewLifecycleOwner, { it ->

            if(it != null){
                Log.e("total",it.toString())
                subtotal!!.text = requireContext().resources.getString(
                    R.string.price, BigDecimal(it.toString()).setScale(
                        2,
                        RoundingMode.HALF_EVEN
                    ).toString().replace('.', ',')
                )
                total!!.text = requireContext().resources.getString(
                    R.string.price, BigDecimal(it.toString()).setScale(
                        2,
                        RoundingMode.HALF_EVEN
                    ).toString().replace('.', ',')
                )
            }

        })

    }

    private fun setAdapter() {
        mAdapter = context?.let { CartAdapter(productsList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    override fun onProductSelected(product: Cart?) {
        TODO("Not yet implemented")
    }

    override fun onRemoveCartClick(product: Cart, image: ImageView, position: Int) {
        mAdapter!!.removeItem(position)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.deleteById(product.id!!)
    }

    override fun onIncrementClick(product: Cart, position: Int) {

        val quantity = (product.quantity + 1 )
        Log.e("onIncrementClick",quantity.toString())
        val cart = Cart(
            id = product.id,
            name = product.name,
            image = product.image,
            price = product.price,
            subcategory = product.subcategory,
            favorite = product.favorite,
            quantity = quantity,
            total = (quantity * product.price)
        )
        mAdapter!!.setQuantity(position,quantity,cart)
        Log.e("prod incr total",cart.total.toString())
        //mAdapter!!.notifyItemChanged(position)

        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
        cartViewModel.insert(cart)
    }

    override fun onDecrementClick(product: Cart, position: Int) {
        val quantity = (product.quantity - 1 )
        Log.e("onDecrementClick",quantity.toString())
        val cart = Cart(
            id = product.id,
            name = product.name,
            image = product.image,
            price = product.price,
            subcategory = product.subcategory,
            favorite = product.favorite,
            quantity = quantity,
            total = (quantity * product.price)
        )
        mAdapter!!.setQuantity(position,quantity,cart)
        Log.e("prod decr total",cart.total.toString())
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        if(quantity <= 0){
            cartViewModel.deleteById(cart.id!!)
            mAdapter!!.removeItem(position)
        }else{
            cartViewModel.insert(cart)
        }

    }

}