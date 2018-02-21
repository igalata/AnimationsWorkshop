package java.com.igalata.animationworkshop

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.igalata.animationsworkshop.R

/**
 * Created by irinagalata on 1/26/18.
 */
class WaveSideBar : FrameLayout {

    var expandAnimationDuration = 800L
    var collapseAnimationDuration = 700L

    var view: View? = null
        set(value) {
            field = value
            addView(value)
            value?.visibility = View.GONE
        }

    @ColorRes
    var startColorRes = android.R.color.white
        get() = ContextCompat.getColor(context, field)

    @ColorRes
    var endColorRes = android.R.color.white
        get() = ContextCompat.getColor(context, field)

    @DimenRes
    var sideBarWidthRes = R.dimen.side_bar_width

    private var isExpanded = false

    private val smallOffset by lazy { dpToPx(R.dimen.small_offset) }
    private val offset by lazy { dpToPx(R.dimen.offset) }
    private val pullOffset by lazy { dpToPx(R.dimen.pull_offset) }

    private val sideBarWidth: Float
        get() = dpToPx(sideBarWidthRes) + smallOffset

    private var startX = 0f
    private var startY = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val touchOutside = isExpanded && event.x > sideBarWidth
        val touchEdge = event.x < offset && !isExpanded

        return touchEdge || touchOutside || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> {
                startX = event.x
                startY = event.y

                if (event.x >= offset && !isExpanded) return false
            }
            ACTION_MOVE -> {
            }
            ACTION_UP -> {
                if (!event.isClick(startX, startY)) {
                    if (!isExpanded && event.isPulled(startX, pullOffset)) {
                        expand()
                    } else if (event.isPulledBack(startX, pullOffset)) {
                        collapse()
                    }
                }
            }
        }
        return true
    }

    fun collapse() {
        isExpanded = false
        Toast.makeText(context, "Menu collapsed", Toast.LENGTH_SHORT).show()
    }

    fun expand() {
        isExpanded = true
        Toast.makeText(context, "Menu expanded", Toast.LENGTH_SHORT).show()
    }

}