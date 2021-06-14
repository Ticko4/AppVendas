package ipvc.estg.cm.fragments

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.EntitiesAdapter
import ipvc.estg.cm.entities.Entity
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.retrofit.EndPoints
import ipvc.estg.cm.retrofit.ServiceBuilder
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_cart_fragment.*
import kotlinx.android.synthetic.main.cm_cart_fragment.view.*
import kotlinx.android.synthetic.main.cm_entities_fragment.*
import kotlinx.android.synthetic.main.cm_entities_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ipvc.estg.cm.vmodel.SimpleDividerItemDecoration


class EntitiesFragment : Fragment(), EntitiesAdapter.EntitiesAdapterListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: EntitiesAdapter? = null
    private var entitiesList: MutableList<Entity> = ArrayList<Entity>()
    private var itemCounter:TextView? = null
    private lateinit var cartViewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_entities_fragment, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view_entities.setHasFixedSize(true)

        recycler_view_entities.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //recycler_view_entities.addItemDecoration(SimpleDividerItemDecoration(requireContext(),R.drawable.line_divider));
        recycler_view_entities.removeItemDecorationAt(0);
    }


    private fun getData(){

        entitiesList.clear()
        recyclerView!!.adapter = mAdapter
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        val obj = JSONObject()

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

                                            entitiesList.add(
                                                Entity(
                                                    id = it.id,
                                                    name = it.name,
                                                    image = it.image,
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
                  /*      (activity as NavigationHost).customToaster(
                            title = getString(R.string.toast_error),
                            message = getString(R.string.general_error),
                            type = "connection"
                        )*/
                    }

                })



            entitiesList.add(
                Entity(
                    id = 1,
                    name = "ProLar",
                    image = "https://specials-images.forbesimg.com/imageserve/5f85be4ed0acaafe77436710/960x0.jpg?fit=scale",
                )
            )
            Log.e("p1.livro.qtd", 1.toString())
            Log.e("p1.livro", entitiesList[entitiesList.size - 1].id.toString())
            //mAdapter!!.notifyItemInserted((productsList.size - 1))

            entitiesList.add(
                Entity(
                    id = 2,
                    name = "O Pote com nome do tamanho do caralho que te foda",
                    image = "https://static1.casapraticaqualita.com.br/articles/6/95/6/@/1101-o-que-nao-faltam-sao-queijos-que-fazem-e-article_content_img-2.jpg",
                )
            )
            Log.e("p2.queijo.qtd", 2.toString())
            Log.e("p2.queijo", entitiesList[entitiesList.size - 1].id.toString())

            entitiesList.add(
                Entity(
                    id = 3,
                    name = "Nosso café",
                    image = "https://images.trustinnews.pt/uploads/sites/5/2019/10/muda-muito-de-telemovel-esta-a-prejudicar-o-ambiente-2-1024x683.jpg",
                )
            )
            Log.e("p3.telemovel.qtd", 3.toString())
            Log.e("p3.telemovel", entitiesList[entitiesList.size - 1].id.toString())

            entitiesList.add(
                Entity(
                    id = 4,
                    name = "Sonho do capitão",
                    image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
                )
            )
        entitiesList.add(
            Entity(
                id = 5,
                name = "Maceira",
                image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
            )
        )

        entitiesList.add(
            Entity(
                id = 6,
                name = "Marcado do mira",
                image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
            )
        )
        entitiesList.add(
            Entity(
                id = 7,
                name = "Oceano",
                image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
            )
        )
        entitiesList.add(
            Entity(
                id = 8,
                name = "Pastelaria estrelinha de mel",
                image = "https://newinoeiras.nit.pt/wp-content/uploads/2021/02/d840d9637b626e0b764c69098840986c.jpg",
            )
        )
            Log.e("p4.coca-cola.qtd", 4.toString())
            Log.e("p4.coca-cola", entitiesList[entitiesList.size - 1].id.toString())

        mSwipeRefreshLayout!!.isRefreshing = false
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
      /*  mSwipeRefreshLayout!!.setOnRefreshListener { // Refresh items
            refresh()
        }*/
    }

    /*NAVIGATION MENU*/
    private fun setToolbar(view: View){
        (activity as AppCompatActivity).setSupportActionBar(view.app_bar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = getString(R.string.navigation_entities)
        view.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                requireActivity(), view.main_frame_entity,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_menu
                ), // Menu open icon
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
            )
        ) // Menu close icon
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
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

    private fun declareItems(view: View){
        itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.getCount().observeOnce(this, {
            if (it != null) {
                itemCounter!!.text = it.toString()
            }
        })
    }

    private fun setRecycleView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_entities)
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

    private fun setAdapter() {
        mAdapter = context?.let { EntitiesAdapter(entitiesList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    override fun onEntitySelected(entity: Entity?) {
        Log.e("entity", entity.toString())
    }

}