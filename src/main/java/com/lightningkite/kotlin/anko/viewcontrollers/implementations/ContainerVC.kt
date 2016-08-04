package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.content.res.Resources
import android.view.View
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer

/**
 * Contains a given [VCContainer], embedding the container of views inside this view controller.
 * Useful if you want to have a smaller section of your view that changes, like you might with tabs.
 * Created by jivie on 10/14/15.
 */
open class ContainerVC(val container: VCContainer, val disposeContainer: Boolean = true) : ViewController {

    override fun make(activity: VCActivity): View {
        val vcView = VCView(activity).apply {
            wholeViewAnimatingIn = true
        }
        vcView.attach(container)
        return vcView
    }

    override fun unmake(view: View) {
        if (view !is VCView) throw IllegalStateException()
        view.detatch()
        view.unmake()
        super.unmake(view)
    }

    override fun animateInComplete(activity: VCActivity, view: View) {
        if (view !is VCView) throw IllegalStateException()
        view.animateInComplete(activity, view)
        super.animateInComplete(activity, view)
    }

    override fun animateOutStart(activity: VCActivity, view: View) {
        if (view !is VCView) throw IllegalStateException()
        view.animateOutStart(activity, view)
        super.animateOutStart(activity, view)
    }

    override fun dispose() {
        if (disposeContainer) {
            container.dispose()
        }
        super.dispose()
    }

    override fun onBackPressed(backAction: () -> Unit) {
        container.onBackPressed(backAction)
    }

    override fun getTitle(resources: Resources): String {
        return container.getTitle(resources)
    }
}