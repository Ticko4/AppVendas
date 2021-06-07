package ipvc.estg.cm.fragments

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.ProductsAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.listeners.CircleAnimationUtil
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_home_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*
import java.util.*


class HomeFragment: Fragment(), ProductsAdapter.ProductsAdapterListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: ProductsAdapter? = null
    private var productsList: MutableList<Product> = ArrayList<Product>()
    private var itemCounter:TextView? = null
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_home_fragment, container, false)
        setToolbar(view)
        setClickListeners(view)
        declareItems(view)
        setAdapter()
        setRecycleView(view)
        setRefreshLayout(view)
        Log.e("create", true.toString())
        getData()
        return view

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

    private fun setClickListeners(view: View){
        view.nav_logout.setOnClickListener {
            (activity as NavigationHost).logout(LoginFragment(), "login")
        }
        view.nav_prod.setOnClickListener {

        }

        view.nav_cart.setOnClickListener {
            (activity as NavigationHost).navigateTo(CartFragment(),addToBackStack = true,animate = true,"cart")
        }

        view.nav_settings.setOnClickListener {

        }

        view.cartRelativeLayout.setOnClickListener {
            (activity as NavigationHost).navigateTo(CartFragment(),addToBackStack = true,animate = true,"cart")
        }
    }

    /*NAVIGATION MENU*/
    private fun setToolbar(view: View){
        (activity as AppCompatActivity).setSupportActionBar(view.app_bar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = getString(R.string.navigation_home)
        view.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                requireActivity(), view.main_frame,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_menu
                ), // Menu open icon
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
            )
        ) // Menu close icon
    }

    private fun setAdapter() {
        mAdapter = context?.let { ProductsAdapter(productsList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    private fun setRecycleView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_products)
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
        productsList.clear()
        mAdapter?.notifyDataSetChanged()
        Log.e("refresh", true.toString())
        getData()
    }

    override fun onProductSelected(product: Product?) {
        /*TODO("Not yet implemented")*/
    }

    override fun onFavoriteClick(product: Product?) {
        var message = "Removido dos favoritos"

        if(product!!.favorite){
            message = "Adicionado aos favoritos"
        }

        (activity as NavigationHost).customToaster(title = getString(R.string.toast_success), message = message, type = "success")
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    override fun onAddCartClick(product: Product, image: ImageView,position: Int) {
        makeFlyAnimation(image, product,position)
    }

    private fun getData(){
        productsList.clear();
        recyclerView!!.adapter = mAdapter
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.getProductById(1).observeOnce(this, { cart ->
            productsList.add(
                Product(
                    id = 1,
                    name = "Livro",
                    image = "https://specials-images.forbesimg.com/imageserve/5f85be4ed0acaafe77436710/960x0.jpg?fit=scale",
                    price = 10.2f,
                    subcategory = "teste",
                    favorite = false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p1.livro.qtd", 1.toString())
            Log.e("p1.livro", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })


        cartViewModel.getProductById(2).observeOnce(this, { cart ->
            productsList.add(
                Product(
                    id = 2,
                    name = "Queijo",
                    image = "https://static1.casapraticaqualita.com.br/articles/6/95/6/@/1101-o-que-nao-faltam-sao-queijos-que-fazem-e-article_content_img-2.jpg",
                    price = 10000.2f,
                    subcategory = "teste",
                    favorite = false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p2.queijo.qtd", 2.toString())
            Log.e("p2.queijo", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })


        cartViewModel.getProductById(3).observeOnce(this, { cart ->
            productsList.add(
                Product(
                    id = 3,
                    name = "Telemovel",
                    image = "https://images.trustinnews.pt/uploads/sites/5/2019/10/muda-muito-de-telemovel-esta-a-prejudicar-o-ambiente-2-1024x683.jpg",
                    price = 10.2f,
                    subcategory = "teste",
                    favorite = false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p3.telemovel.qtd", 3.toString())
            Log.e("p3.telemovel", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })

        cartViewModel.getProductById(4).observeOnce(this, { cart ->
            productsList.add(
                Product(
                    id = 4,
                    name = "Coca-Cola",
                    image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                    price = 10.2f,
                    subcategory = "teste",
                    favorite = false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p4.coca-cola.qtd", 4.toString())
            Log.e("p4.coca-cola", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })



        mSwipeRefreshLayout!!.isRefreshing = false
    }

    private fun makeFlyAnimation(targetView: ImageView, product: Product,position:Int) {
        val destView = requireView().findViewById(R.id.cartRelativeLayout) as RelativeLayout
        CircleAnimationUtil().attachActivity(requireActivity()).setTargetView(targetView).setMoveDuration(100)
            .setDestView(destView).setAnimationListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    val quantity = (product.quantity + 1 )
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
                    Log.e("home total",cart.total.toString())
                    mAdapter!!.setQuantity(position,quantity)

                    cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
                    cartViewModel.insert(cart)
                    (activity as NavigationHost).customToaster(title = getString(R.string.toast_success), message = resources.getString(R.string.product_added,cart.name,cart.quantity.toString()), type = "success")

                }
                override fun onAnimationEnd(animation: Animator?) {
                    itemCounter!!.text = (((itemCounter!!.text).toString()).toInt() + 1).toString()
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).startAnimation()
    }

}