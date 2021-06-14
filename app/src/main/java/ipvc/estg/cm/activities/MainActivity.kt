package ipvc.estg.cm.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ipvc.estg.cm.R
import ipvc.estg.cm.fragments.HomeFragment
import ipvc.estg.cm.navigation.NavigationHost
import www.sanju.motiontoast.MotionToast


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
