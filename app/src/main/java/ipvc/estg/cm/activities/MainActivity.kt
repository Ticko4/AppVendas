package ipvc.estg.cm.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.cm.R
import ipvc.estg.cm.fragments.*
import ipvc.estg.cm.navigation.NavigationHost
import kotlinx.android.synthetic.main.cm_home_fragment.*
import www.sanju.motiontoast.MotionToast
import java.util.*


class MainActivity : AppCompatActivity(), NavigationHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cm_main_activity)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, HomeFragment())
            .commit()

       /* if (getRememberMe() != null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, HomeFragment())
                .commit()
        }else{
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, LoginFragment())
                .commit()
        }*/

        findViewById<FloatingActionButton>(R.id.activate_microphone).setOnClickListener {
            getSpeechInput()
        }

    }

    private fun getSpeechInput()
    {
        Log.e("12","Abre");
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /**/
            Log.e("permissions","sem");
            return
        }
        val intent = Intent(
            RecognizerIntent
            .ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())

        if (intent.resolveActivity(applicationContext.packageManager) != null)
        {
            startActivityForResult(intent, 10)
            Log.e("Teste","Abre");
        } else
        {
            customToaster(
                title = getString(R.string.toast_error),
                message = getString(R.string.Speech_Input_Error),
                type = "error"
            )
        }
    }
    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode,
            resultCode, data)
        when (requestCode) {
            10 -> if (resultCode == RESULT_OK && data != null) {

                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                if(getVisibleFragment()!!.tag != null){
                    when (getVisibleFragment()!!.tag) {
                        "details" -> {
                            val fragment: ProductDetailFragment = supportFragmentManager.findFragmentByTag("details") as ProductDetailFragment

                        }
                        "home" -> {
                            val fragment: HomeFragment = supportFragmentManager.findFragmentByTag("home") as HomeFragment

                        }
                        "login" -> {
                            val fragment: LoginFragment = supportFragmentManager.findFragmentByTag("login") as LoginFragment

                        }
                        "cart" -> {
                            val fragment: CartFragment = supportFragmentManager.findFragmentByTag("cart") as CartFragment

                        }
                        "checkout" -> {
                            val fragment: CheckoutFragment = supportFragmentManager.findFragmentByTag("checkout") as CheckoutFragment

                        }
                        "order_end" -> {
                            val fragment: OrderEndFragment = supportFragmentManager.findFragmentByTag("order_end") as OrderEndFragment

                        }
                        "entities" -> {
                            val fragment: EntitiesFragment = supportFragmentManager.findFragmentByTag("entities") as EntitiesFragment

                        }
                        "products" -> {
                            val fragment: ProductsByEntityFragment = supportFragmentManager.findFragmentByTag("products") as ProductsByEntityFragment

                        }
                        else ->{
                            val fragment: HomeFragment = supportFragmentManager.findFragmentByTag("home") as HomeFragment
                        }
                    }
                }else{
                    val fragment: HomeFragment = supportFragmentManager.findFragmentByTag("home") as HomeFragment
                }
            }
        }
    }

    fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible) return fragment
            }
        }
        return null
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment       Fragment to navigate to.
     * @param addToBackStack Whether or not the current fragment should be added to the backStack.
     */
    override fun navigateTo(fragment: Fragment, addToBackStack: Boolean, animate: Boolean, tag: String, data: Bundle?) {

        val transaction = supportFragmentManager
            .beginTransaction()/*.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)*/

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        if(animate){
            transaction/*.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);*/
            .setCustomAnimations(
                R.animator.slide_in_up,//enter
                R.animator.slide_out_down,//exit
                R.animator.slide_in_up,//popEnter
                R.animator.slide_out_up
            )//popExit
        }
        fragment.arguments = data
        transaction.replace(R.id.container, fragment, tag).commit()
    }


    override fun getRememberMe(): String? {
        val sharedPref: SharedPreferences = this.getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    override fun logout(fragment: Fragment,tag: String) {
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.leave))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val settings = getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                settings.edit().clear().apply()

                val auth = getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
                auth.edit().clear().apply()
                navigateTo(fragment, addToBackStack = false, animate = true, tag = tag)
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    override fun customToaster(message: String,title: String,type: String){
        var toastType =  MotionToast.TOAST_SUCCESS
        when (type) {
            "success" -> {
                toastType = MotionToast.TOAST_SUCCESS
            }
            "error" -> {
                toastType = MotionToast.TOAST_ERROR
            }
            "warning" -> {
                toastType = MotionToast.TOAST_WARNING
            }
            "info" -> {
                toastType = MotionToast.TOAST_INFO
            }
            "delete" -> {
                toastType = MotionToast.TOAST_DELETE
            }
            "connection" -> {
                toastType = MotionToast.TOAST_NO_INTERNET
            }
        }

        MotionToast.darkToast( this,
            title,
            message,
            toastType,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this,R.font.helvetica_regular))
    }

    override fun getAuthenticationToken(): String? {
        val sharedPref = getSharedPreferences("AUTHENTICATION", MODE_PRIVATE)
        return sharedPref.getString("_token", null)
    }

    override fun getAuthenticationUserId(): Int {
        val sharedPref = getSharedPreferences("AUTHENTICATION", MODE_PRIVATE)
        return sharedPref.getInt("idUser", 0)
    }

    override fun setConsent() {
        val preferences: SharedPreferences = getSharedPreferences("CONSENT", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("isConsentCheck", 1)
        editor.apply()
    }

    override fun getConsentStatus(): Boolean {
        val sharedPref = getSharedPreferences("CONSENT", MODE_PRIVATE)
        return sharedPref.getInt("isConsentCheck", 0) == 1
    }

    override fun isUserLogged(): Boolean {
        val sharedPref: SharedPreferences = this.getSharedPreferences(
            "AUTHENTICATION",
            Context.MODE_PRIVATE
        )

        if(sharedPref.getInt("idUser", 0) != 0){
            return true
        }
        return false
    }

    override fun popBackStack(){
        supportFragmentManager.popBackStack();
    }

}
