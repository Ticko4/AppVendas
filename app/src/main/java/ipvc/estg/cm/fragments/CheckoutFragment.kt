package ipvc.estg.cm.fragments

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import ipvc.estg.cm.R
import ipvc.estg.cm.adapters.CartAdapter
import ipvc.estg.cm.entities.Cart
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_checkout_fragment.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class CheckoutFragment : Fragment() {
    private var total:Float? = 0f
    private lateinit var cartViewModel: CartViewModel
    private var productsList: MutableList<Cart> = ArrayList<Cart>()
    private var result: DropInResult? = null

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
        fillProducts()

        return view
    }

    private fun setClickListeners(view: View){

        view.close_checkout!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        view.pay_checkout_btn!!.setOnClickListener {
            view.pay_checkout_btn!!.isEnabled = false
            view.pay_checkout_btn!!.startAnimation()
            view.payment_error.visibility = View.GONE
            if(view.input_name.text!!.isEmpty()){
                view.input_name.error = getString(R.string.field_required)
            }
            if(view.input_email.text!!.isEmpty()){
                view.input_email.error = getString(R.string.field_required)
            }

            if(view.input_address.text!!.isEmpty()){
                view.input_address.error = getString(R.string.field_required)
            }
            if(result == null){
                view.payment_error.visibility = View.VISIBLE
            }
            if(view.input_name.text!!.isNotEmpty() && view.input_email.text!!.isNotEmpty() && view.input_address.text!!.isNotEmpty() && result != null){
                cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
                cartViewModel.clearQuantities()
                success()
            }else{
                failed()
            }
        }

        view.add_card.setOnClickListener {
            onBraintreeSubmit()
        }
    }

    fun onBraintreeSubmit() {
        val dropInRequest = DropInRequest().tokenizationKey("sandbox_f252zhq7_hh4cpc39zq4rgjcg")
        previewRequest.launch(dropInRequest.getIntent(context))
    }

    private val previewRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            result = it.data!!.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)!!
            requireView().add_card.text = getString(R.string.edit_payment_method)
            requireView().payment_error.visibility = View.GONE
        } else if (it.resultCode == RESULT_CANCELED) {
            //Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show()
        } else {
            // handle errors here, an exception may be available in
            //Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            val error = it.data!!.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception?
        }
    }

    private fun fillProducts(){
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        cartViewModel.allProductsWithQuantity().observe(viewLifecycleOwner, { it ->
            productsList = it as MutableList<Cart>
            for (item in this.productsList!!){
                val value = TextView(context)
                value.text = getString(R.string.checkout_item_count,item.quantity.toString() ,item.name)
                value.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                requireView().listview?.addView(value)
            }
        })

        cartViewModel.getTotal().observe(viewLifecycleOwner, { it ->
            if(it != null){
                total = it
                requireView().pay_checkout_btn!!.text = requireContext().resources.getString(R.string.payment_button, BigDecimal(it.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
            }
        })
    }

    private fun failed(){
        requireView().pay_checkout_btn!!.isEnabled = true
        requireView().pay_checkout_btn!!.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(resources, R.drawable.error))

        Handler(Looper.getMainLooper()).postDelayed({
            requireView().pay_checkout_btn!!.revertAnimation()
            requireView().pay_checkout_btn!!.setBackgroundResource(R.drawable.shape)

            Handler(Looper.getMainLooper()).postDelayed({
                requireView().pay_checkout_btn!!.text = requireContext().resources.getString(R.string.payment_button, BigDecimal(total.toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
            }, 500)


        }, 10 * 100)
    }

    private fun success(){
        requireView().pay_checkout_btn!!.isEnabled = true
        requireView().pay_checkout_btn!!.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(resources, R.drawable.done))

        Handler(Looper.getMainLooper()).postDelayed({
            requireView().pay_checkout_btn!!.revertAnimation()
            requireView().pay_checkout_btn!!.setBackgroundResource(R.drawable.shape)
            (activity as NavigationHost).navigateTo(OrderEndFragment(), addToBackStack = false, animate = true, "order_end")
        }, 10 * 100)
    }
}