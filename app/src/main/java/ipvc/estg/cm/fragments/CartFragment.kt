package ipvc.estg.cm.fragments

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.CartAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_cart_fragment.*
import kotlinx.android.synthetic.main.cm_cart_fragment.view.*
import org.w3c.dom.Text
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class CartFragment:Fragment(), CartAdapter.CartAdapterListener,TextToSpeech.OnInitListener {
    private var productsList: MutableList<Cart> = ArrayList<Cart>()
    private var cart: MutableList<Cart> = ArrayList<Cart>()
    private var mAdapter: CartAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var cartViewModel: CartViewModel
    private var subtotal: TextView? = null
    private var total: TextView? = null
    private var tts: TextToSpeech? = null

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
        tts = TextToSpeech(context, this)

        return view
    }

    private fun setClickListeners(view: View){
        view.close_cart!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        view.checkout_btn!!.setOnClickListener {
            if(productsList.size > 0){
                (activity as NavigationHost).navigateTo(CheckoutFragment(),addToBackStack = true,animate = true,"checkout")
            }
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

        cartViewModel.allProductsWithQuantity().observe(viewLifecycleOwner, { it ->
            if(recyclerView!!.adapter == null){
                productsList = it as MutableList<Cart>
                if(productsList.size <= 0){
                    requireView().findViewById<TextView>(R.id.no_products).visibility = View.VISIBLE
                    requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.GONE
                }else{
                    mAdapter = context?.let { CartAdapter(productsList, this, it) }
                    mAdapter!!.setHasStableIds(true)
                    mAdapter!!.notifyItemRangeInserted(0, productsList.size - 1)
                    recyclerView!!.adapter = mAdapter

                    requireView().findViewById<TextView>(R.id.no_products).visibility = View.GONE
                    requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.VISIBLE
                }

            }

        })

        cartViewModel.getTotal().observe(viewLifecycleOwner, { it ->
            if (it != null) {
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
            } else if (it.toString() == "0" || it == null) {

                val fadeIn = ScaleAnimation(
                    0.0f,
                    1f,
                    0.0f,
                    1f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                )
                fadeIn.duration = 500
                fadeIn.fillAfter = true

                requireView().findViewById<TextView>(R.id.no_products).startAnimation(fadeIn)
                requireView().findViewById<TextView>(R.id.no_products).visibility = View.VISIBLE
                requireView().findViewById<NestedScrollView>(R.id.cart_scroll).visibility = View.GONE

                subtotal!!.text = requireContext().resources.getString(
                    R.string.price, BigDecimal(0).setScale(
                        2,
                        RoundingMode.HALF_EVEN
                    ).toString().replace('.', ',')
                )
                total!!.text = requireContext().resources.getString(
                    R.string.price, BigDecimal(0).setScale(
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
        Log.e("onProductSelected",product!!.id.toString())
    }

    override fun onRemoveCartClick(product: Cart, image: ImageView, position: Int) {
        mAdapter!!.removeItem(position)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.deleteById(product.id!!)
    }

    override fun onIncrementClick(product: Cart, position: Int) {
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
        mAdapter!!.setQuantity(position,quantity,cart)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
        cartViewModel.insert(cart)
    }

    override fun onDecrementClick(product: Cart, position: Int) {
        val quantity = (product.quantity - 1 )
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
        mAdapter!!.setQuantity(position,quantity,cart)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
        if(quantity <= 0){
            //cartViewModel.deleteById(cart.id!!)
            cart.quantity = 0
            cartViewModel.insert(cart)
            mAdapter!!.removeItem(position)
        }else{
            cartViewModel.insert(cart)
        }

    }
    fun readCart(){
        speakOut(0,productsList.size)
    }
    private fun speakOut(pos1:Int, pos2:Int) {
        val formatArray  = productsList.subList(pos1, pos2);
        if(formatArray== null){
            return;
        }

        for(item in formatArray){
            tts!!.speak("Item "+(formatArray.indexOf(item)+1)+item.name + ", Quantidade " +item.quantity + ",   Com valor de " + requireContext().resources.getString(
                R.string.price, BigDecimal(item.price.toString()).setScale(
                    2,
                    RoundingMode.HALF_EVEN
                ).toString().replace('.', ',')
            ), TextToSpeech.QUEUE_ADD, null, "")
        }

        tts!!.speak("Total do carrinho " + total!!.text, TextToSpeech.QUEUE_ADD, null, "")

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