package ipvc.estg.cm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ipvc.estg.cm.R
import ipvc.estg.cm.navigation.NavigationHost
import kotlinx.android.synthetic.main.cm_checkout_fragment.view.*
import kotlinx.android.synthetic.main.cm_checkout_fragment.view.pay_checkout_btn
import kotlinx.android.synthetic.main.cm_order_end_fragment.view.*

class OrderEndFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_order_end_fragment, container, false)

        setClickListeners(view)
        return view
    }

    private fun setClickListeners(view: View){
        view.take_me_home!!.setOnClickListener {
            takeMeToHome()
        }

        view.track_order!!.setOnClickListener {
            trackOrder()
        }
    }

    fun trackOrder()
     {
        (activity as NavigationHost).popBackStack()
        (activity as NavigationHost).navigateTo(
            HomeFragment(),
            addToBackStack = false,
            animate = true,
            "order_end"
        )
    }

    fun takeMeToHome(){
        requireView().take_me_home!!.setOnClickListener {
            (activity as NavigationHost).popBackStack()
            (activity as NavigationHost).navigateTo(
                HomeFragment(),
                addToBackStack = false,
                animate = true,
                "order_end"
            )
        }
    }

}