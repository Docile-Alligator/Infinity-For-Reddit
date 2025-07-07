package ml.docilealligator.infinityforreddit.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.card.MaterialCardView

class TouchInterceptableMaterialCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    private var mShouldInterceptTouchEvent: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mShouldInterceptTouchEvent || super.onInterceptTouchEvent(ev)
    }

    public fun setShouldInterceptTouch(value: Boolean) {
        mShouldInterceptTouchEvent = value
    }
}