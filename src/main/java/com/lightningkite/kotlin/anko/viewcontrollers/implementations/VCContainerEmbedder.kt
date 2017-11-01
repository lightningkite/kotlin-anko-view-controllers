package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.view.View
import android.view.ViewGroup
import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.VCContext
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer

/**
 * Embeds the given view container in the given view, transitioning new views in and out as needed.
 *
 * Created by joseph on 11/7/16.
 */
class VCContainerEmbedder(val vcContext: VCContext, val root: ViewGroup, val container: VCContainer, val makeLayoutParams: () -> ViewGroup.LayoutParams) {

    var defaultAnimation: AnimationSet? = AnimationSet.fade

    var wholeViewAnimatingIn: Boolean = false
    var killViewAnimateOutCalled: Boolean = false

    var current: ViewController? = null
    var currentView: View? = null
    val swap = fun(new: ViewController, preferredAnimation: AnimationSet?, onFinish: () -> Unit) {
        val oldView = currentView
        val old = current
        val animation = preferredAnimation ?: defaultAnimation
        current = new
        val newView = new.make(vcContext)
        root.addView(newView, makeLayoutParams())
        currentView = newView
        if (old != null && oldView != null) {
            if (animation == null) {
                old.animateOutStart(vcContext, oldView)
                old.unmake(oldView)
                root.removeView(oldView)
                onFinish()
                new.animateInComplete(vcContext, newView)
            } else {
                val animateOut = animation.animateOut
                old.animateOutStart(vcContext, oldView)
                oldView.animateOut(root).withEndAction {
                    old.unmake(oldView)
                    root.removeView(oldView)
                    onFinish()
                }.start()
                val animateIn = animation.animateIn
                newView.animateIn(root).withEndAction {
                    new.animateInComplete(vcContext, newView)
                }.start()
            }
        } else {
            if (!wholeViewAnimatingIn) {
                new.animateInComplete(vcContext, newView)
            }
        }
        killViewAnimateOutCalled = false
    }

    init {
        container.swapListener = swap
        swap(container.current, null) {}
    }

    fun animateInComplete(vcContext: VCContext, view: View) {
        current?.animateInComplete(vcContext, currentView!!)
    }

    fun animateOutStart(vcContext: VCContext, view: View) {
        killViewAnimateOutCalled = true
        current?.animateOutStart(vcContext, currentView!!)
    }

    fun unmake() {
        if (!killViewAnimateOutCalled) {
            current?.animateOutStart(vcContext, currentView!!)
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