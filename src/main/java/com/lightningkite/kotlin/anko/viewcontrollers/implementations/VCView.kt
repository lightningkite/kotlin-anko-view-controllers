package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.view.Gravity
import android.view.View
import android.widget.AbsListView
import android.widget.FrameLayout
import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.onClick

/**
 * A [View] that has a [VCContainer].
 * Created by jivie on 10/13/15.
 */
open class VCView(val activity: VCActivity) : FrameLayout(activity) {

    open val defaultAnimation: AnimationSet? = AnimationSet.fade

    var wholeViewAnimatingIn: Boolean = false
    var killViewAnimateOutCalled: Boolean = false
    var defaultLayoutParams = FrameLayout.LayoutParams(matchParent, matchParent, Gravity.CENTER)

    var container: VCContainer? = null
    fun attach(newContainer: VCContainer) {
        container = newContainer
        newContainer.swapListener = swap
        swap(newContainer.current, null) {}
    }

    fun detatch() {
        unmake()
        container?.swapListener = null
    }

    fun unmake() {
        if (!killViewAnimateOutCalled) {
            current?.animateOutStart(activity, currentView!!)
            killViewAnimateOutCalled = true
        }
        current?.unmake(currentView!!)
        if (currentView != null) {
            removeView(currentView)
        }
        current = null
        currentView = null
    }

    var current: ViewController? = null
    var currentView: View? = null
    val swap = fun(new: ViewController, preferredAnimation: AnimationSet?, onFinish: () -> Unit) {
        val oldView = currentView
        val old = current
        val animation = preferredAnimation ?: defaultAnimation
        current = new
        val newView = new.make(activity).apply {
            layoutParams = FrameLayout.LayoutParams(defaultLayoutParams.width, defaultLayoutParams.height, defaultLayoutParams.gravity)
            if (this !is AbsListView) {
                onClick { }
            }
        }
        this.addView(newView)
        currentView = newView
        if (old != null && oldView != null) {
            if (animation == null) {
                old.animateOutStart(activity, oldView)
                old.unmake(oldView)
                removeView(oldView)
                onFinish()
                new.animateInComplete(activity, newView)
            } else {
                val animateOut = animation.animateOut
                old.animateOutStart(activity, oldView)
                oldView.animateOut(this).withEndAction {
                    old.unmake(oldView)
                    removeView(oldView)
                    onFinish()
                }.start()
                val animateIn = animation.animateIn
                newView.animateIn(this).withEndAction {
                    new.animateInComplete(activity, newView)
                }.start()
            }
        } else {
            if (!wholeViewAnimatingIn) {
                new.animateInComplete(activity, newView)
            }
        }
        killViewAnimateOutCalled = false
    }

    fun animateInComplete(activity: VCActivity, view: View) {
        current?.animateInComplete(activity, currentView!!)
    }

    fun animateOutStart(activity: VCActivity, view: View) {
        killViewAnimateOutCalled = true
        current?.animateOutStart(activity, currentView!!)
    }
}