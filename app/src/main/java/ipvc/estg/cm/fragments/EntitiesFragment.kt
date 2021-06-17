package ipvc.estg.cm.fragments

import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.CompaniesAdapter
import ipvc.estg.cm.entities.Company
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.retrofit.EndPoints
import ipvc.estg.cm.retrofit.ServiceBuilder
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_entities_fragment.*
import kotlinx.android.synthetic.main.cm_entities_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class EntitiesFragment : Fragment(), CompaniesAdapter.EntitiesAdapterListener,TextToSpeech.OnInitListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: CompaniesAdapter? = null
   /* private var entitiesList: MutableList<Company> = ArrayList<Company>()*/
    private var liveEntitiesList: MutableLiveData<MutableList<Company>> = MutableLiveData<MutableList<Company>>()
    private var itemCounter:TextView? = null
    private lateinit var cartViewModel: CartViewModel
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        tts = TextToSpeech(context, this)

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
        val entitiesList: MutableList<Company> = ArrayList()
        liveEntitiesList.value = null
        recyclerView!!.adapter = mAdapter
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.getEntities()
                call.enqueue(object : Callback<List<Company>> {
                    override fun onResponse(call: Call<List<Company>>?, response: Response<List<Company>>) {
                        if (response.isSuccessful) {
                            try {
                                response.body().forEach {
                                    entitiesList.add(
                                        Company(
                                            id = it.id,
                                            name = it.name,
                                            image = it.image,
                                            location = it.location
                                        )
                                    )
                                    liveEntitiesList.value = entitiesList
                                    mAdapter!!.notifyDataSetChanged()
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

                    override fun onFailure(call: Call<List<Company>>?, t: Throwable?) {
                        (activity as NavigationHost).customToaster(
                            title = getString(R.string.toast_error),
                            message = getString(R.string.general_error),
                            type = "connection"
                        )
                    }

                })

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
        mSwipeRefreshLayout!!.setOnRefreshListener { // Refresh items
            refresh()
        }
    }

    private fun refresh() {
      /*  entitiesList.clear()
        mAdapter?.notifyDataSetChanged()
        Log.e("refresh", true.toString())*/
        getData()
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
        view.nav_main.setOnClickListener {
            (activity as NavigationHost).navigateTo(HomeFragment(),addToBackStack = false,animate = true,"home")
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
        mAdapter = context?.let { CompaniesAdapter(liveEntitiesList, this, it) }
        mAdapter!!.setHasStableIds(true)
    }

    override fun onEntitySelected(company: Company?) {
        val bundle = Bundle()
        if (company != null) {
            bundle.putString("title", company.name)
            bundle.putInt("id", company.id)
        }
        (activity as NavigationHost).navigateTo(ProductsByEntityFragment(),addToBackStack = true,animate = true,tag = "products", data = bundle)
    }
    fun readProducts(){
        speakOut(0,liveEntitiesList.value!!.size)
    }
    private fun speakOut(pos1:Int, pos2:Int) {
        val formatArray  = liveEntitiesList.value!!.subList(pos1, pos2) ?: return

        for(item in formatArray){
            tts!!.speak(resources.getString(R.string.speech_store,(formatArray.indexOf(item)+1).toString(),item.name,item.location), TextToSpeech.QUEUE_ADD, null, "")
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
        tts!!.speak("", TextToSpeech.QUEUE_FLUSH, null,"")
    }

    override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

}