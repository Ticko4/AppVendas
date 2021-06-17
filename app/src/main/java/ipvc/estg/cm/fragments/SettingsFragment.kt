package ipvc.estg.cm.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.cm.R
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.vmodel.CartViewModel
import kotlinx.android.synthetic.main.cm_cart_fragment.view.*
import kotlinx.android.synthetic.main.cm_home_fragment.view.*
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import kotlinx.android.synthetic.main.cm_settings_fragment.*
import kotlinx.android.synthetic.main.cm_settings_fragment.view.*
import kotlinx.android.synthetic.main.navigation_backdrop.view.*

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_settings_fragment, container, false)
        setClickListeners(view)
        declareItems(view)
        return view
    }

    private fun setClickListeners(view: View){
        view.close_settings!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun declareItems(view: View) {
        val sharedPref: SharedPreferences? =
            activity?.getSharedPreferences("MIC", Context.MODE_PRIVATE)
        val show = sharedPref?.getBoolean("show", true)

        view.btn_speech.isChecked = show == true

        view.btn_speech.setOnClickListener{
            if(btn_speech.isChecked){
                (activity as NavigationHost).customToaster(
                    title = getString(R.string.toast_success),
                    message = getString(R.string.toast_settings_btnmic_add),
                    type = "success"
                )
            }else{
                (activity as NavigationHost).customToaster(
                    //title = getString(R.string.toast_success),
                    title = getString(R.string.toast_success),
                    message = getString(R.string.toast_settings_btnmic_remove),
                    type = "success"
                )
            }
            val buttonMic: SharedPreferences = requireContext().getSharedPreferences("MIC", Context.MODE_PRIVATE)
            buttonMic.edit().putBoolean("show", btn_speech.isChecked).apply()
            (activity as NavigationHost).isBtnVisible()
        }
    }

}