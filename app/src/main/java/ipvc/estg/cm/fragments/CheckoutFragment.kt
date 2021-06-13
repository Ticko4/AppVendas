package ipvc.estg.cm.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.CartAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_cart_fragment.view.*
import kotlinx.android.synthetic.main.cm_cart_fragment.view.close_cart
import kotlinx.android.synthetic.main.cm_checkout_fragment.view.*
import kotlinx.android.synthetic.main.cm_home_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.ArrayList

class CheckoutFragment : Fragment(), CartAdapter.CartAdapterListener {
    private var total: TextView? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var cartViewModel: CartViewModel
    private var productsList: MutableList<Cart> = ArrayList<Cart>()
    private var mAdapter: CartAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_checkout_fragment, container, false)

        setClickListeners(view)
        setRecyclerView(view)
        fillRecyclerView()

        return view
    }

    private fun setClickListeners(view: View){
        view.close_checkout!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_checkout)
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

        cartViewModel.allProductsWithQuantity().observe(viewLifecycleOwner, { it ->
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
                total!!.text = requireContext().resources.getString(
                    R.string.price, BigDecimal(it.toString()).setScale(
                        2,
                        RoundingMode.HALF_EVEN
                    ).toString().replace('.', ',')
                )
            }

        })

    }

    override fun onProductSelected(product: Cart?) {
        TODO("Not yet implemented")
    }

    override fun onRemoveCartClick(product: Cart, image: ImageView, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onIncrementClick(product: Cart, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onDecrementClick(product: Cart, position: Int) {
        TODO("Not yet implemented")
    }
}