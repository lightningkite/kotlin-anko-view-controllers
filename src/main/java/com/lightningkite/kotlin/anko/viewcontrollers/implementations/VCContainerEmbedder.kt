package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.getActivity
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import org.jetbrains.anko.onClick

/**
 * Created by joseph on 11/7/16.
 */
class VCContainerEmbedder(val root: ViewGroup, val container: VCContainer, val makeLayoutParams: () -> ViewGroup.LayoutParams) {

    var defaultAnimation: AnimationSet? = AnimationSet.fade

    var wholeViewAnimatingIn: Boolean = false
    var killViewAnimateOutCalled: Boolean = false

    val activity: VCActivity get() = root.getActivity() as? VCActivity ?: throw IllegalArgumentException("Root view must belong to a VCActivity")

    var current: ViewController? = null
    var currentView: View? = null
    val swap = fun(new: ViewController, preferredAnimation: AnimationSet?, onFinish: () -> Unit) {
        val oldView = currentView
        val old = current
        val animation = preferredAnimation ?: defaultAnimation
        current = new
        val newView = new.make(activity).apply {
            if (this !is AbsListView) {
                onClick { }
            }
        }
        root.addView(newView, makeLayoutParams())
        currentView = newView
        if (old != null && oldView != null) {
            if (animation == null) {
                old.animateOutStart(activity, oldView)
                old.unmake(oldView)
                root.removeView(oldView)
                onFinish()
                new.animateInComplete(activity, newView)
            } else {
                val animateOut = animation.animateOut
                old.animateOutStart(activity, oldView)
                oldView.animateOut(root).withEndAction {
                    old.unmake(oldView)
                    root.removeView(oldView)
                    onFinish()
                }.start()
                val animateIn = animation.animateIn
                newView.animateIn(root).withEndAction {
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

    init {
        container.swapListener = swap
        swap(container.current, null) {}
    }

    fun animateInComplete(activity: VCActivity, view: View) {
        current?.animateInComplete(activity, currentView!!)
    }

    fun animateOutStart(activity: VCActivity, view: View) {
        killViewAnimateOutCalled = true
        current?.animateOutStart(activity, currentView!!)
    }

    fun unmake() {
        if (!killViewAnimateOutCalled) {
            current?.animateOutStart(activity, currentView!!)
            killViewAnimateOutCalled = true
        }
        current?.unmake(currentView!!)
        if (currentView != null) {
            root.removeView(currentView)
        }
        current = null
        currentView = null
    }

}