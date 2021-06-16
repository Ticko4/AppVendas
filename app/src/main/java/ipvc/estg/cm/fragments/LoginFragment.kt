package ipvc.estg.cm.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import ipvc.estg.cm.R
import ipvc.estg.cm.entities.User
import ipvc.estg.cm.listeners.NavigationIconClickListener
import ipvc.estg.cm.navigation.NavigationHost
import ipvc.estg.cm.retrofit.EndPoints
import ipvc.estg.cm.retrofit.ServiceBuilder
import kotlinx.android.synthetic.main.cm_main_activity.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginFragment: Fragment() {

    private var btnLogin: CircularProgressButton? = null
    private var btnRegister: Button? = null
    private var btnResetPassword: Button? = null
    private var rememberMe: CheckBox? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.cm_login_fragment, container, false)
        declareItems(view)
        setClickListeners(view)
        return view
    }

    private fun declareItems(view: View) {
        btnLogin = view.findViewById<View>(R.id.btn_login) as CircularProgressButton
        btnRegister = view.findViewById(R.id.btn_register)
        btnResetPassword = view.findViewById(R.id.btn_reset_password)
        rememberMe = view.findViewById(R.id.remember_me)
    }


    private fun setClickListeners(view: View) {
        btnLogin!!.setOnClickListener {
            login()
        }
    }

    fun login(){

        btnLogin!!.isEnabled = false
        btnLogin!!.startAnimation()

        val usernameInput = requireView().findViewById<TextView>(R.id.username_text)
        val passwordInput = requireView().findViewById<TextView>(R.id.password_text)
        val username = usernameInput.text.toString().lowercase(Locale.getDefault())
        val password = passwordInput.text.toString()
        usernameInput.error = null
        passwordInput.error = null

        if(username.isEmpty() && password.isEmpty()){
            usernameInput.error = getString(R.string.invalid_username)
            passwordInput.error = getString(R.string.invalid_password)
            failed()
        }else if (username.isEmpty()) {
            usernameInput.error = getString(R.string.invalid_username)
            failed()
        } else if (password.isEmpty() || password == " ") {
            passwordInput.error = getString(R.string.invalid_password)
            failed()
        }else{
            val obj = JSONObject()
            obj.put("username", username)
            obj.put("password", password)
            var payload = obj.toString()
            payload = Base64.encodeToString(payload.toByteArray(charset("UTF-8")), Base64.DEFAULT)

           /* val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.loginUser(payload = payload)
            call.enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>?, response: Response<User>?) {

                    if (response!!.isSuccessful) {
                        if (rememberMe!!.isChecked) {
                            val rememberMe: SharedPreferences = context!!.getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                            rememberMe.edit().putString("username", "response").apply()
                        } else {
                            val rememberMe: SharedPreferences = context!!.getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                            rememberMe.edit().clear().apply()
                        }
                        val rememberMe: SharedPreferences = context!!.getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
                        rememberMe.edit().putInt("idUser", response.body().id).apply()
                        rememberMe.edit().putString("_token", "Bearer " + response.body()._token).apply()
                        success()
                    } else {
                        failed()
                        if (response.code() == 403 && response.message() == "login_fail") {
                            usernameInput.error = getString(R.string.wrong_user_info)
                            passwordInput.error = getString(R.string.wrong_user_info)
                        } else {
                            (activity as NavigationHost).customToaster(title = getString(R.string.toast_error), message = getString(R.string.general_error), type = "connection")
                        }
                    }
                    btnLogin!!.isEnabled = true
                }

                override fun onFailure(call: Call<User>?, t: Throwable?) {
                    btnLogin!!.isEnabled = true

                    val rememberMe: SharedPreferences = context!!.getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
                    rememberMe.edit().putInt("idUser", 0).apply()
                    rememberMe.edit().putString("_token", "").apply()

                    (activity as NavigationHost).customToaster(title = getString(R.string.toast_error), message = getString(R.string.general_error), type = "connection")
                    failed()
                }
            })*/
        }

        if(username == "admin@admin.com" && password == "admin"){
            if (rememberMe!!.isChecked) {
                val rememberMe: SharedPreferences = requireContext().getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                rememberMe.edit().putString("username", "response").apply()
            } else {
                val rememberMe: SharedPreferences = requireContext().getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                rememberMe.edit().clear().apply()
            }
            val rememberMe: SharedPreferences = requireContext().getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
            rememberMe.edit().putInt("idUser", 1).apply()
            rememberMe.edit().putString("_token", "Bearer " + "teste_token").apply()
            success()
        }
    }

    private fun failed(){
        btnLogin!!.isEnabled = true
        btnLogin!!.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(resources, R.drawable.error))

        Handler(Looper.getMainLooper()).postDelayed({
            btnLogin!!.revertAnimation()
            btnLogin!!.setBackgroundResource(R.drawable.shape)
        }, 10 * 100)
    }

    private fun success(){
        btnLogin!!.isEnabled = true
        btnLogin!!.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(resources, R.drawable.done))

        Handler(Looper.getMainLooper()).postDelayed({
            btnLogin!!.revertAnimation()
            btnLogin!!.setBackgroundResource(R.drawable.shape)
            (activity as NavigationHost).customToaster(title = getString(R.string.toast_success), message = getString(R.string.login_success), type = "success")
            activity?.onBackPressed()
        }, 10 * 100)
    }
}