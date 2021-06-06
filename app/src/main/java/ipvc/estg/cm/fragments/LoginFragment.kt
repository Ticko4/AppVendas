package ipvc.estg.cm.fragments

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import ipvc.estg.cm.R
import ipvc.estg.cm.navigation.NavigationHost
import org.json.JSONObject
import org.w3c.dom.Text
import java.util.*

class LoginFragment: Fragment() {

    private var btnLogin: CircularProgressButton? = null
    private var btnRegister: Button? = null
    private var btnResetPassword: Button? = null
    private var rememberMe: CheckBox? = null
    private var bntClose: Toolbar? = null

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
        bntClose = view.findViewById(R.id.close_login)
    }

    private fun setClickListeners(view: View) {
        bntClose!!.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        btnLogin!!.setOnClickListener {
            btnLogin!!.isEnabled = false
            btnLogin!!.startAnimation()

            val usernameInput = view.findViewById<TextView>(R.id.username_text)
            val passwordInput = view.findViewById<TextView>(R.id.password_text)
            val username = usernameInput.text.toString().toLowerCase(Locale.getDefault())
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
                obj.put("username", username);
                obj.put("password", password);
                var payload = obj.toString()
                payload = Base64.encodeToString(payload.toByteArray(charset("UTF-8")), Base64.DEFAULT)

               /* val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.loginUser(payload = payload!!)*/

            }
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