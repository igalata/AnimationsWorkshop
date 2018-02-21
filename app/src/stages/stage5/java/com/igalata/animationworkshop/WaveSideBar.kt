package java.com.igalata.animationworkshop

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import com.igalata.animationsworkshop.R
import java.lang.Math.max

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
            init()
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

    private var currentX = 0f
    private var currentY = 0f

    private var controlX = 0f

    private var zeroX = 0f
    private var invertedFraction = 1f

    private var paint = Paint()
    private var path = Path()

    private val gradient: LinearGradient
        get() = LinearGradient(600f, 0f, 0f, 1500f, startColorRes,
                endColorRes, Shader.TileMode.CLAMP)

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
        currentX = event.x
        currentY = event.y

        var invalidateNeeded = false

        when (event.action) {
            ACTION_DOWN -> {
                startX = event.x
                startY = event.y

                if (event.x >= offset && !isExpanded) return false
            }
            ACTION_MOVE -> {
                invalidateNeeded = startX != currentX
                controlX = max(currentX, sideBarWidth - offset)
            }
            ACTION_UP -> {
                if (!event.isClick(startX, startY)) {
                    if (!isExpanded && event.isPulled(startX, pullOffset)) {
                        expand()
                    } else if (event.isPulledBack(startX, pullOffset)) {
                        collapse()
                    }
                }
                invalidateNeeded = true
            }
        }

        if (invalidateNeeded) {
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        reset()

        if (isExpanded) {
            drawQuadBezierCurve(canvas)
        } else {
            drawCubicBezierCurve(canvas)
        }
    }

    private fun reset() {
        path.reset()
    }

    private fun init() {
        paint.apply {
            shader = gradient
            isAntiAlias = true
        }
    }

    private fun drawCubicBezierCurve(canvas: Canvas?) {
        path.let {
            it.moveTo(0f, 0f)
            it.lineTo(0f, height.toFloat())
            it.lineTo(zeroX, height.toFloat())
            it.cubicTo(
                    zeroX, currentY + 3 * offset,
                    zeroX + currentX * invertedFraction, currentY + 3 * offset,
                    zeroX + currentX * invertedFraction, currentY)
            it.cubicTo(
                    zeroX + currentX * invertedFraction, currentY - 3 * offset,
                    zeroX, currentY - 3 * offset,
                    zeroX, 0f)
            it.lineTo(0f, 0f)
        }
        canvas?.drawPath(path, paint)
    }

    private fun drawQuadBezierCurve(canvas: Canvas?) {
        path.let {
            it.moveTo(0f, 0f)
            it.lineTo(0f, height.toFloat())
            it.lineTo(zeroX, height.toFloat())
            it.quadTo(controlX, height / 2f, zeroX, 0f)
            it.lineTo(0f, 0f)
        }
        canvas?.drawPath(path, paint)
    }

    fun collapse() {
        hideContent()
        ValueAnimator.ofFloat(sideBarWidth, 0f).apply {
            duration = collapseAnimationDuration
            addUpdateListener {
                zeroX = animatedValue as Float
            }
        }.start()
        ValueAnimator.ofFloat(sideBarWidth, 0f).apply {
            duration = collapseAnimationDuration + 100
            interpolator = BounceInterpolator()
            addUpdateListener {
                controlX = animatedValue as Float
                invalidate()
            }
            addListener(object : OnAnimationFinishedListener {
                override fun onAnimationEnd(animation: Animator?) {
                    isExpanded = false
                    clearData()
                }
            })
        }.start()
    }

    fun expand() {
        showContent()
        ValueAnimator.ofFloat(0f, sideBarWidth).apply {
            duration = expandAnimationDuration / 2
            addUpdateListener {
                zeroX = animatedValue as Float
                invertedFraction = 1 - animatedFraction
                invalidate()
            }
            addListener(object : OnAnimationFinishedListener {
                override fun onAnimationEnd(animation: Animator?) {
                    finishExpandAnimation()
                    isExpanded = true
                }
            })
        }.start()
    }

    private fun clearData() {
        zeroX = 0f
        invertedFraction = 1f
        controlX = 0f
        currentX = 0f
        currentY = 0f
    }

    private fun finishExpandAnimation() {
        ValueAnimator.ofFloat(currentX, sideBarWidth).apply {
            duration = expandAnimationDuration / 2 + 200
            interpolator = SpringInterpolator()
            addUpdateListener {
                controlX = animatedValue as Float
                invalidate()
            }
        }.start()
    }

    private fun showContent() {
        ViewAnimationUtils.createCircularReveal(
                view, 0, height / 2, 0f, height.toFloat())
                .apply {
                    duration = expandAnimationDuration
                    view?.visibility = View.VISIBLE
                }.start()
    }

    private fun hideContent() {
        ViewAnimationUtils.createCircularReveal(
                view, 0, height / 2, height.toFloat(), 0f)
                .apply {
                    duration = collapseAnimationDuration / 4
                    view?.visibility = View.VISIBLE
                    addListener(object : OnAnimationFinishedListener {
                        override fun onAnimationEnd(animation: Animator?) {
                            view?.visibility = View.GONE
                        }
                    })
                }.start()
    }

}