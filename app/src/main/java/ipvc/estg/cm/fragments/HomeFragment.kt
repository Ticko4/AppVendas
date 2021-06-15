package ipvc.estg.cm.fragments

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonParser
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.ProductsAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.listeners.CircleAnimationUtil
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.retrofit.EndPoints
import ipvc.estg.cm.retrofit.ServiceBuilder
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_home_fragment.*
import kotlinx.android.synthetic.main.cm_home_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class HomeFragment: Fragment(), ProductsAdapter.ProductsAdapterListener,ActivityCompat.OnRequestPermissionsResultCallback,TextToSpeech.OnInitListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: ProductsAdapter? = null
    private var productsList: MutableList<Product> = ArrayList<Product>()
    private var itemCounter:TextView? = null
    private lateinit var cartViewModel: CartViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var tts: TextToSpeech? = null


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    private fun declareItems(view: View){
        itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        tts = TextToSpeech(context, this)
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

        view.nav_cart.setOnClickListener {
            (activity as NavigationHost).navigateTo(
                CartFragment(),
                addToBackStack = true,
                animate = true,
                "cart"
            )
        }
        /*view.activate_microphone.setOnClickListener {
            Log.e("Btn","Entrou")
            getSpeechInput()
        }*/

        view.findViewById<FloatingActionButton>(R.id.activate_microphone).setOnClickListener {
            Log.e("Btn","Entrou")
        }

        view.nav_settings.setOnClickListener {

        }

        view.nav_prod.setOnClickListener {
            (activity as NavigationHost).navigateTo(EntitiesFragment(),addToBackStack = false,animate = true,"entities")
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
        val gson = Gson()
        val myJson: String = gson.toJson(product)
        val bundle = Bundle()
        bundle.putString("product", myJson)
        (activity as NavigationHost).navigateTo(ProductDetailFragment(), addToBackStack = true, animate = true, tag ="details",bundle)
    }

    override fun onFavoriteClick(product: Product?) {
        var message = "Removido dos favoritos"

        if(product!!.favorite){
            message = "Adicionado aos favoritos"
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
            total = product.total
        )

        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.insert(cart)

        (activity as NavigationHost).customToaster(
            title = getString(R.string.toast_success),
            message = message,
            type = "success"
        )

    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    override fun onAddCartClick(product: Product, image: ImageView, position: Int) {
        makeFlyAnimation(image, product, position)
    }

    private fun getData() {
        productsList.clear()
        recyclerView!!.adapter = mAdapter
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireContext() as Activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)

                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /*PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true);*/

            return
        }



        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            if (location != null) {
                val obj = JSONObject()
                obj.put("latitude", location.latitude)
                obj.put("longitude", location.longitude)

                val payload = Base64.encodeToString(
                    obj.toString().toByteArray(charset("UTF-8")),
                    Base64.DEFAULT
                )

                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.getRecommendedProducts(payload)
                call.enqueue(object : Callback<List<Product>> {
                    override fun onResponse(
                        call: Call<List<Product>>?,
                        response: Response<List<Product>>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                response.body().forEach {
                                    cartViewModel.getProductById(it.id)
                                        .observeOnce(viewLifecycleOwner, { cart ->

                                            productsList.add(
                                                Product(
                                                    id = it.id,
                                                    name = it.name,
                                                    image = it.image,
                                                    images = it.images,
                                                    price = it.price,
                                                    subcategory = it.subcategory,
                                                    description = it.description,
                                                    favorite = cart?.favorite?: false ,
                                                    quantity = cart?.quantity ?: 0,
                                                    total = (cart?.quantity ?: 0) * it.price
                                                )
                                            )
                                        })

                                }
                            } catch (e: Exception) {
                                Log.e("catch", e.toString())
                            }
                        } else {
                            (activity as NavigationHost).customToaster(
                                title = getString(R.string.toast_error),
                                message = getString(R.string.general_error),
                                type = "error"
                            )
                        }
                    }

                    override fun onFailure(call: Call<List<Product>>?, t: Throwable?) {
                        /*(activity as NavigationHost).customToaster(
                            title = getString(R.string.toast_error),
                            message = getString(R.string.general_error),
                            type = "connection"
                        )*/
                    }

                })



            }



            mSwipeRefreshLayout!!.isRefreshing = false
        }
        cartViewModel.getProductById(1).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://images.samsung.com/is/image/samsung/assets/br/p6_gro2/p6_initial_pf/watches/pf_galaxy_watch3_45mm_black_mo_png.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(

                Product(
                    id = 1,
                    name = "Watch",
                    image = "https://s2.glbimg.com/LlVk8Dzlv2aKZrt23xTDT46glog=/0x0:1900x1422/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2021/c/A/mPg3XCTKWAzhqSBxLKAQ/galaxy-watch3-product-image-1.jpg",
                    images = myJson,
                    price = 100.0f,
                    subcategory = "Wearables",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p1.livro.qtd", 1.toString())
            Log.e("p1.livro", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(2).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://www.oficinadeinverno.com.br/blog/wp-content/uploads/2015/03/gluten-free-new-york-cheesecake-1450985-hero-01-dc54f9daf38044238b495c7cefc191fa.jpg",
                "https://www.teleculinaria.pt/wp-content/uploads/2019/09/Cheesecake-de-gelatina-CHLM-19.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 2,
                    name = "Cheesecake",
                    image = "https://conteudo.imguol.com.br/c/entretenimento/04/2020/08/10/cheesecake-com-calda-de-frutas-vermelhas-1597080856359_v2_1000x667.jpg",
                    images = myJson,
                    price = 14.99f,
                    subcategory = "Dairy",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p2.queijo.qtd", 2.toString())
            Log.e("p2.queijo", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(3).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://www.techadvisor.com/cmsdata/slideshow/3677861/best_smartphone_jan_2021_hero_thumb1200_4-3.jpg",
                "https://cdn.vox-cdn.com/thumbor/v97OD-MBgNjw8p5crApucVs9RB8=/0x0:2050x1367/1800x1800/filters:focal(1025x684:1026x685)/cdn.vox-cdn.com/uploads/chorus_asset/file/22022572/bfarsace_201106_4269_012.0.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 3,
                    name = "Phone",
                    image = "https://images.trustinnews.pt/uploads/sites/5/2019/10/muda-muito-de-telemovel-esta-a-prejudicar-o-ambiente-2-1024x683.jpg",
                    images = myJson,
                    price = 399.99f,
                    subcategory = "Tecnology",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p3.telemovel.qtd", 3.toString())
            Log.e("p3.telemovel", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(4).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 4,
                    name = "Coca-Cola",
                    image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                    images = myJson,
                    price = 1.20f,
                    subcategory = "Drinks",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p4.coca-cola.qtd", 4.toString())
            Log.e("p4.coca-cola", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(5).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://images.samsung.com/is/image/samsung/assets/br/p6_gro2/p6_initial_pf/watches/pf_galaxy_watch3_45mm_black_mo_png.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(

                Product(
                    id = 5,
                    name = "Watch",
                    image = "https://s2.glbimg.com/LlVk8Dzlv2aKZrt23xTDT46glog=/0x0:1900x1422/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2021/c/A/mPg3XCTKWAzhqSBxLKAQ/galaxy-watch3-product-image-1.jpg",
                    images = myJson,
                    price = 100.0f,
                    subcategory = "Wearables",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p1.livro.qtd", 1.toString())
            Log.e("p1.livro", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(6).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://www.oficinadeinverno.com.br/blog/wp-content/uploads/2015/03/gluten-free-new-york-cheesecake-1450985-hero-01-dc54f9daf38044238b495c7cefc191fa.jpg",
                "https://www.teleculinaria.pt/wp-content/uploads/2019/09/Cheesecake-de-gelatina-CHLM-19.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 6,
                    name = "Cheesecake",
                    image = "https://conteudo.imguol.com.br/c/entretenimento/04/2020/08/10/cheesecake-com-calda-de-frutas-vermelhas-1597080856359_v2_1000x667.jpg",
                    images = myJson,
                    price = 14.99f,
                    subcategory = "Dairy",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p2.queijo.qtd", 2.toString())
            Log.e("p2.queijo", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(7).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://www.techadvisor.com/cmsdata/slideshow/3677861/best_smartphone_jan_2021_hero_thumb1200_4-3.jpg",
                "https://cdn.vox-cdn.com/thumbor/v97OD-MBgNjw8p5crApucVs9RB8=/0x0:2050x1367/1800x1800/filters:focal(1025x684:1026x685)/cdn.vox-cdn.com/uploads/chorus_asset/file/22022572/bfarsace_201106_4269_012.0.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 7,
                    name = "Phone",
                    image = "https://images.trustinnews.pt/uploads/sites/5/2019/10/muda-muito-de-telemovel-esta-a-prejudicar-o-ambiente-2-1024x683.jpg",
                    images = myJson,
                    price = 399.99f,
                    subcategory = "Tecnology",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
                    quantity = cart?.quantity ?: 0,
                    total = (cart?.quantity ?: 0) * 10.2f
                )
            )
            Log.e("p3.telemovel.qtd", 3.toString())
            Log.e("p3.telemovel", productsList[productsList.size - 1].id.toString())
            mAdapter!!.notifyItemInserted((productsList.size - 1))
        })
        cartViewModel.getProductById(8).observeOnce(this, { cart ->

            val images = arrayListOf(
                "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg"
            )
            val gson = Gson()
            val myJson: String = gson.toJson(images)

            productsList.add(
                Product(
                    id = 8,
                    name = "Coca-Cola",
                    image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                    images = myJson,
                    price = 1.20f,
                    subcategory = "Drinks",
                    description = "Varius id interdum diam dolor tincidunt nunc arcu accumsan scelerisque condimentum aliquam interdum congue quisque pellentesque nec sollicitudin vel mi leo amet arcu nunc quam.\n" +
                            "\n" +
                            "Portaest quam pellentesque amet lacus amet aliquam nisl suspendisse scelerisque dolor facilisis nunc euismod tortor commodo tortor interdum sem mi lacus maximus erat urna facilisis.",
                    favorite = cart?.favorite ?: false,
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

    private fun makeFlyAnimation(targetView: ImageView, product: Product, position: Int) {
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
                        total = (quantity * product.price)
                    )
                    Log.e("home total",cart.total.toString())
                    mAdapter!!.setQuantity(position,quantity)

                    cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
                    cartViewModel.insert(cart)
                    (activity as NavigationHost).customToaster(
                        title = getString(R.string.toast_success), message = resources.getString(
                            R.string.product_added,
                            cart.name,
                            cart.quantity.toString()
                        ), type = "success"
                    )

                }

                override fun onAnimationEnd(animation: Animator?) {
                    itemCounter!!.text = (((itemCounter!!.text).toString()).toInt() + 1).toString()
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).startAnimation()
    }

    fun readProducts(){
        speakOut(0,productsList.size)
    }
    private fun speakOut(pos1:Int, pos2:Int) {
        /*val sharedPref = getSharedPreferences(ler, Context.MODE_PRIVATE) ?: return
        val le = sharedPref.getBoolean("le",false)
        if(!le)
            return;*/
        val formatArray  = productsList.subList(pos1, pos2);
        if(formatArray== null){
            return;
        }

        for(item in formatArray){
            tts!!.speak("Item "+(formatArray.indexOf(item)+1)+item.name, TextToSpeech.QUEUE_ADD, null, "")
            }
        }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
//                buttonSpeak!!.isEnabled = true
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
    fun stopRead(){
        Log.e("canRead","canRead")
        tts!!.speak("", TextToSpeech.QUEUE_FLUSH, null,"")

    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

}