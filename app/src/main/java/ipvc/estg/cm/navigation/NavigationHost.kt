package ipvc.estg.cm.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * A host (typically an `Activity`} that can display fragments and knows how to respond to
 * navigation events.
 */
interface NavigationHost {
    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    fun navigateTo(fragment: Fragment, addToBackStack: Boolean, animate: Boolean, tag:String = "", data: Bundle? = null)

    fun logout(fragment: Fragment,tag: String)

    fun getRememberMe(): String?

    fun customToaster(message:String,title: String,type:String)

    fun isUserLogged(): Boolean?

    fun getAuthenticationUserId(): Int?

    fun getAuthenticationToken(): String?

    fun setConsent()

    fun getConsentStatus(): Boolean?

    fun popBackStack()

    fun isBtnVisible()
    
    fun resetSpeechIcon()
}
