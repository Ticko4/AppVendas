package ipvc.estg.cm.fragments

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.WishListAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.entities.EntityProd
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.entities.Subcategory
import ipvc.estg.cm.listeners.CircleAnimationUtil
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_wish_list_fragment.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*

class WishListFragment : Fragment(), WishListAdapter.WishListAdapterListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: WishListAdapter? = null
    private var liveProductsList: MutableLiveData<MutableList<Cart>> = MutableLiveData<MutableList<Cart>>()
    private var itemCounter: TextView? = null
    private lateinit var cartViewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_wish_list_fragment, container, false)
        setClickListeners(view)
        declareItems(view)
        setAdapter()
        setRecycleView(view)
        setRefreshLayout(view)
        getData()
        return view

    }

    private fun setClickListeners(view: View){
        view.close_wish_list.setOnClickListener {
            activity?.onBackPressed()
        }

        view.cartRelativeLayout.setOnClickListener {
            (activity as NavigationHost).navigateTo(
                CartFragment(),
                addToBackStack = true,
                animate = true,
                "cart"
            )
        }
    }

    private fun declareItems(view: View){
        itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.getCount().observeOnce(this, {
            if (it != null) {
                itemCounter!!.text = it.toString()
            }
        })
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    private fun setAdapter() {
        mAdapter = context?.let { WishListAdapter(liveProductsList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    private fun setRecycleView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_wishlist)
        /*  recyclerView?.setHasFixedSize(true)*/
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

    private fun setRefreshLayout(view: View) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        //cada "ciclo" do refresh icon muda a cor (FF)
        context?.let { ContextCompat.getColor(it, R.color.cpb_green) }?.let {
            mSwipeRefreshLayout!!.setColorSchemeColors(
                it,
                ContextCompat.getColor(requireContext(), R.color.cpb_blue),
                ContextCompat.getColor(requireContext(), R.color.cpb_red)
            )
        }
        mSwipeRefreshLayout!!.setOnRefreshListener { // Refresh items
            refresh()
        }
    }

    private fun refresh() {
        getData()
    }

    private fun getData() {
        liveProductsList.value = null
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.getFavorites().observe(viewLifecycleOwner, { it ->
            Log.e("fav",it.toString())
            if(it == null || it.isEmpty()){
                requireView().findViewById<TextView>(R.id.no_products).visibility = View.VISIBLE
                requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.GONE
            }else if(recyclerView!!.adapter == null){
               liveProductsList.value = it as MutableList<Cart>?
               if(liveProductsList.value!!.size <= 0){
                   requireView().findViewById<TextView>(R.id.no_products).visibility = View.VISIBLE
                   requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.GONE
               }else{
                   mAdapter = context?.let { WishListAdapter(liveProductsList, this, it) }
                   mAdapter!!.setHasStableIds(true)
                   mAdapter!!.notifyItemRangeInserted(0, liveProductsList.value!!.size - 1)
                   recyclerView!!.adapter = mAdapter

                   requireView().findViewById<TextView>(R.id.no_products).visibility = View.GONE
                   requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.VISIBLE
               }
           }

        })

        mSwipeRefreshLayout!!.isRefreshing = false
    }

    override fun onProductSelected(product: Cart?) {
        val gson = Gson()
        val cart =  Product(
            id = product!!.id!!,
            name = product!!.name,
            image = product!!.image,
            images = product!!.images,
            price = product!!.price,
            subcategory = gson.fromJson(product.subcategory, Subcategory::class.java),
            description = product.description,
            favorite = product?.favorite,
            quantity = product?.quantity,
            total = product.total,
            entity = gson.fromJson(product.entity, EntityProd::class.java)
        )

        val myJson: String = gson.toJson(cart)
        val bundle = Bundle()
        bundle.putString("product", myJson)
        (activity as NavigationHost).navigateTo(ProductDetailFragment(), addToBackStack = true, animate = true, tag ="details",bundle)
    }

    override fun onFavoriteClick(product: Cart?,position: Int) {
        var message = getString(R.string.fav_removed)

        if(product!!.favorite){
            message = getString(R.string.fav_added)
        }

        val cart = Cart(
            id = product.id,
            name = product.name,
            image = product.image,
            images= product.images,
            price = product.price,
            subcategory = product.subcategory,
            description = product.description,
            favorite = product.favorite,
            quantity = product.quantity,
            total = product.total,
            entity = product.entity
        )

        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.insert(cart)

        (activity as NavigationHost).customToaster(title = getString(R.string.toast_success), message = message, type = "success")

        Log.e("favorite", product.favorite.toString())
        if(!product.favorite){
            mAdapter!!.removeItem(position)
        }
    }

    override fun onAddCartClick(product: Cart, image: ImageView, position: Int) {
        makeFlyAnimation(image, product, position)
    }

    private fun makeFlyAnimation(targetView: ImageView, product: Cart, position: Int) {
        val destView = requireView().findViewById(R.id.cartRelativeLayout) as RelativeLayout
        CircleAnimationUtil().attachActivity(requireActivity()).setTargetView(targetView,0).setMoveDuration(100)
            .setDestView(destView).setAnimationListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    val quantity = (product.quantity + 1 )
                    val cart = Cart(
                        id = product.id,
                        name = product.name,
                        image = product.image,
                        images = product.images,
                        price = product.price,
                        subcategory = product.subcategory,
                        description = product.description,
                        favorite = product.favorite,
                        quantity = quantity,
                        total = (quantity * product.price),
                        entity = product.entity
                    )
                    Log.e("home total",cart.total.toString())
                    mAdapter!!.setQuantity(position,quantity,cart)

                    cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
                    cartViewModel.insert(cart)
                    (activity as NavigationHost).customToaster(title = getString(R.string.toast_success), message = resources.getString(R.string.product_added, cart.name, cart.quantity.toString()), type = "success")

                }

                override fun onAnimationEnd(animation: Animator?) {
                    itemCounter!!.text = (((itemCounter!!.text).toString()).toInt() + 1).toString()
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).startAnimation()
    }



}