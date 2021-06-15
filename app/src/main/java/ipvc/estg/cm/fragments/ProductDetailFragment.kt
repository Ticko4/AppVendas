package ipvc.estg.cm.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.SliderAdapter
import ipvc.estg.cm.entities.Attribute
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.entities.SliderItem
import ipvc.estg.cm.listeners.CircleAnimationUtil
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.cm_product_detail_fragment.view.*
import kotlinx.android.synthetic.main.cm_product_detail_fragment.view.cartRelativeLayout
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class ProductDetailFragment: Fragment() {
    var product: Product? = null
    var adapter: SliderAdapter? = null
    var np: NumberPicker? = null
    var expTv: ExpandableTextView? = null
    var sliderItems:ArrayList<SliderItem> = ArrayList<SliderItem>()
    private lateinit var cartViewModel: CartViewModel
    private var itemCounter:TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_product_detail_fragment, container, false)
        val bundle = this.arguments
        if (bundle != null) {
            val gson = Gson()
            product = gson.fromJson(bundle.getString("product"), Product::class.java)
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            val requestOptions = RequestOptions()
            requestOptions.placeholder(circularProgressDrawable)
            requestOptions.skipMemoryCache(true)
            requestOptions.fitCenter()

            Glide.with(requireContext()).load(product!!.image).apply(requestOptions).into(view.image_to_add)
        }

        declareItems(view)
        setSliderView(view)
        setClickListeners(view)

        val gson = Gson()

        val images = gson.fromJson(gson.fromJson(product!!.images, JsonElement::class.java), object : TypeToken<ArrayList<String?>?>() {}.type) as ArrayList<String>
        sliderItems.clear()
        images.forEach{
            sliderItems.add(SliderItem(it))
        }

        adapter?.addItems(sliderItems)
        return view
    }


    private fun setClickListeners(view: View) {
        view.close_details.setOnClickListener {
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

        view.btn_add.setOnClickListener {
            makeFlyAnimation(view.findViewById(R.id.image_to_add), product!!)
        }

        np!!.setOnValueChangedListener { _, _, _ ->
            view.total_val.text = getString(
                R.string.price, BigDecimal(product!!.price.toDouble()).multiply(
                    BigDecimal(
                        np!!.value
                    )
                ).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ',')
            )
        }
    }
    private fun declareItems(view: View){
        np = view.findViewById(R.id.np) as NumberPicker
        expTv = view.findViewById(R.id.expand_text_view)
        /*itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView*/
        np!!.minValue = 1
        //Specify the maximum value/number of NumberPicker
        np!!.maxValue = 10
        itemCounter = view.findViewById<View>(R.id.cartTotal) as TextView
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.getCount().observeOnce(this, {
            if (it != null) {
                itemCounter!!.text = it.toString()
            }
        })

        view.product_price.text = getString(
            R.string.price, BigDecimal(product!!.price.toDouble()).setScale(
                2,
                RoundingMode.HALF_EVEN
            ).toString().replace('.', ',')
        )
        view.total_val.text = view.product_price.text
        view.product_name.text = product!!.name
        view.close_details.title = product!!.name
        view.category.text = product!!.subcategory
        if(product!!.description.isEmpty()){
            product!!.description = getString(R.string.product_no_description)
        }
        expTv?.text = product!!.description
        expTv!!.setOnExpandStateChangeListener { _, isExpanded ->
            val sv = view.cart_scroll
            val anim: ObjectAnimator
            anim = if(isExpanded){
                ObjectAnimator.ofInt(sv, "scrollY", 0, sv.bottom)
            }else{
                ObjectAnimator.ofInt(sv, "scrollY", sv.top, 0)
            }
            anim.duration = 400
            anim.start()
        }

        var buttonNames: MutableList<Attribute> = ArrayList<Attribute>()

        buttonNames.add(Attribute(id = 1, name = "S", price = 1f))
        buttonNames.add(Attribute(id = 2, name = "L", price = 1f))
        buttonNames.add(Attribute(id = 3, name = "M", price = 1f))
        buttonNames.add(Attribute(id = 4, name = "XL", price = 1f))
        buttonNames.add(Attribute(id = 5, name = "XS", price = 1f))



        val radioGroup: RadioGroup = view.findViewById(R.id.fancy_radio_group) as RadioGroup
        buttonNames.forEach {
            val radioButton = RadioButton(context)
            radioButton.id = it.id
            val childParam1 = RadioGroup.LayoutParams(0, GridLayout.LayoutParams.MATCH_PARENT, 1f)
            childParam1.marginEnd = 5
            childParam1.marginStart = 5
            radioButton.gravity = Gravity.CENTER
            radioButton.layoutParams = childParam1
            radioButton.text = it.name

            radioButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.radio_states_orange
            )
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    Color.BLACK,  //disabled
                    Color.WHITE //enabled
                )
            )
            radioButton.setTextColor(colorStateList)
            radioButton.buttonDrawable = null
            radioGroup.addView(radioButton, childParam1)
        }

        radioGroup.getChildAt(0).performClick()
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    private fun setSliderView(view: View){
        val sliderView: SliderView = view.findViewById(R.id.imageSlider)
        adapter = SliderAdapter(requireContext())

        sliderView.setSliderAdapter(adapter!!)

        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!

        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        sliderView.scrollTimeInSec = 4 //set scroll delay in seconds :
        sliderView.startAutoCycle()
    }

    private fun makeFlyAnimation(targetView: ImageView, product: Product) {
        requireView().btn_add!!.isEnabled = false
        requireView().btn_add!!.startAnimation()
        targetView.visibility = View.GONE

        val destView = requireView().findViewById(R.id.cartRelativeLayout) as RelativeLayout
        CircleAnimationUtil().attachActivity(requireActivity()).setTargetView(targetView, 60).setMoveDuration(
            100
        )
            .setDestView(destView).setAnimationListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                    val quantity = (product.quantity + np!!.value)
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
                    success(cart)
                    Log.e("home total", cart.quantity.toString())
                    cartViewModel =
                        ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
                    cartViewModel.insert(cart)

                }

                override fun onAnimationEnd(animation: Animator?) {
                    itemCounter!!.text =
                        (((itemCounter!!.text).toString()).toInt() + np!!.value).toString()

                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            }).startAnimation()
    }

    private fun failed(){
        requireView().btn_add!!.isEnabled = true
        requireView().btn_add!!.doneLoadingAnimation(
            Color.TRANSPARENT, BitmapFactory.decodeResource(
                resources,
                R.drawable.error
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            requireView().btn_add!!.revertAnimation()
            requireView().btn_add!!.setBackgroundResource(R.drawable.shape)
        }, 10 * 100)
    }

    private fun success(cart: Cart){
        requireView().btn_add!!.isEnabled = true
        requireView().btn_add!!.doneLoadingAnimation(
            Color.TRANSPARENT, BitmapFactory.decodeResource(
                resources,
                R.drawable.done
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            requireView().btn_add!!.revertAnimation()
            requireView().btn_add!!.setBackgroundResource(R.drawable.shape)
            /* (activity as NavigationHost).customToaster(
                title = getString(R.string.toast_success), message = resources.getString(
                    R.string.product_added,
                    cart.name,
                    cart.quantity.toString()
                ), type = "success"
            )*/
        }, 10 * 100)


    }

}