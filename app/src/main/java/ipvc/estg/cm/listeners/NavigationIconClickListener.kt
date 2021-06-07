package ipvc.estg.cm.listeners

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat.getColor
import ipvc.estg.cm.R


/**
 * [android.view.View.OnClickListener] used to translate the product grid sheet downward on
 * the Y-axis when the navigation icon in the toolbar is pressed.
 */
class NavigationIconClickListener @JvmOverloads internal constructor(
    private val context: Context,
    private val sheet: View,
    private val interpolator: Interpolator? = null,
    private val openIcon: Drawable? = null,
    private val closeIcon: Drawable? = null
) : View.OnClickListener {

    private val animatorSet = AnimatorSet()
    private val height: Int
    private var backdropShown = false

    init {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
    }

    override fun onClick(view: View) {
        Log.e("backdropShown", backdropShown.toString())
        backdropShown = !backdropShown

        // Cancel the existing animations
        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()

        updateIcon(view)

        var translateY = height - context.resources.getDimensionPixelSize(R.dimen.animation_height)
        if (context.resources!!.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            translateY = height - context.resources.getDimensionPixelSize(R.dimen.animation_height_landscape)
        }


        val animator = ObjectAnimator.ofFloat(
            sheet,
            "translationY",
            (if (backdropShown) translateY else 0).toFloat()
        )
        animator.duration = 500
        if (interpolator != null) {
            animator.interpolator = interpolator
        }
        animatorSet.play(animator)
        animator.start()
    }

    private fun updateIcon(view: View) {
        if (openIcon != null && closeIcon != null) {
            if (view !is ImageView) {
                throw IllegalArgumentException("updateIcon() must be called on an ImageView")
            }
            if (backdropShown) {
                closeIcon.colorFilter = PorterDuffColorFilter( getColor(context,R.color.cpb_white), PorterDuff.Mode.SRC_IN)
                view.setImageDrawable(closeIcon)
                //view.setColorFilter(R.color.cpb_white)
            } else {
                openIcon.colorFilter = PorterDuffColorFilter( getColor(context,R.color.cpb_white), PorterDuff.Mode.SRC_IN)
                view.setImageDrawable(openIcon)
            }
        }
    }


}
