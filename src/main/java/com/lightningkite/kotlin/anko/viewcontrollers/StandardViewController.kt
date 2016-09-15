package com.lightningkite.kotlin.anko.viewcontrollers

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCView
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.runAll
import java.util.*

/**
 *
 * Created by jivie on 1/19/16.
 *
 */

abstract class StandardViewController() : ViewController {

    val onMake: ArrayList<(View) -> Unit> = ArrayList()
    val onAnimateInComplete: ArrayList<(VCActivity, View) -> Unit> = ArrayList()
    val onAnimateOutStart: ArrayList<(VCActivity, View) -> Unit> = ArrayList()
    val onUnmake: ArrayList<(View) -> Unit> = ArrayList()
    val onDispose: ArrayList<() -> Unit> = ArrayList()

    val viewLifecycle: LifecycleConnectable = object : LifecycleConnectable {
        override fun connect(listener: LifecycleListener) {
            onMake.add { listener.onStart() }
            onUnmake.add { listener.onStop() }
        }
    }
    val fullLifecycle: LifecycleConnectable = object : LifecycleConnectable {
        override fun connect(listener: LifecycleListener) {
            listener.onStart()
            onDispose.add { listener.onStop() }
        }
    }

    /**
     * Adds the item to the collection immediately, but removes it when [unmake] is called.
     * The primary use of this is binding things in [make] that need to be removed when [unmake] is called.
     */
    @Deprecated("Use [StandardViewController.listen] instead.")
    fun <T> connectVC(collection: MutableCollection<T>, item: T): T {
        collection.add(item)
        onUnmake.add {
            collection.remove(item)
        }
        return item
    }

    /**
     * Adds the item to the collections immediately, but removes the item from all of the collections when [unmake] is called.
     * The primary use of this is binding things in [make] that need to be removed when [unmake] is called.
     */
    @Deprecated("Use [StandardViewController.listen] instead.")
    fun <T> connectManyVC(vararg collections: MutableCollection<T>, item: T): T {
        for (collection in collections) {
            collection.add(item)
        }
        onUnmake.add {
            for (collection in collections) {
                collection.remove(item)
            }
        }
        return item
    }

    abstract fun makeView(activity: VCActivity): View
    final override fun make(activity: VCActivity): View {
        val view = makeView(activity)
        onMake.runAll(view)
        onMake.clear()
        return view
    }

    override fun unmake(view: View) {
        onUnmake.runAll(view)
        onUnmake.clear()
        super.unmake(view)
    }

    override fun dispose() {
        onDispose.runAll()
        onDispose.clear()
        super.dispose()
    }

    override fun animateInComplete(activity: VCActivity, view: View) {
        onAnimateInComplete.runAll(activity, view)
        onAnimateInComplete.clear()
        super.animateInComplete(activity, view)
    }

    override fun animateOutStart(activity: VCActivity, view: View) {
        onAnimateOutStart.runAll(activity, view)
        onAnimateOutStart.clear()
        super.animateOutStart(activity, view)
    }

    fun <T : Disposable> autoDispose(disposable: T): T {
        onDispose.add { disposable.dispose() }
        return disposable
    }

    inline fun ViewGroup.viewContainer(container: VCContainer): VCView {
        val vcview = VCView(context as VCActivity)
        vcview.wholeViewAnimatingIn = true
        vcview.attach(container)
        onAnimateInComplete.add { activity, view ->
            vcview.animateInComplete(activity, view)
        }
        onAnimateOutStart.add { activity, view ->
            vcview.animateOutStart(activity, view)
        }
        onUnmake.add {
            vcview.detatch()
        }
        addView(vcview)
        return vcview
    }

    inline fun ViewGroup.viewContainer(container: VCContainer, init: VCView.() -> Unit): VCView {
        val vcview = VCView(context as VCActivity)
        vcview.init()
        vcview.wholeViewAnimatingIn = true
        vcview.attach(container)
        onAnimateInComplete.add { activity, view ->
            vcview.animateInComplete(activity, view)
        }
        onAnimateOutStart.add { activity, view ->
            vcview.animateOutStart(activity, view)
        }
        onUnmake.add {
            vcview.detatch()
        }
        addView(vcview)
        return vcview
    }

    fun ViewGroup.viewController(controller: ViewController, init: View.() -> Unit): View {
        val view = controller.make(context as VCActivity)
        addView(view)
        view.init()
        onUnmake.add {
            controller.unmake(view)
        }
        return view
    }

    @Deprecated("Please use text resources.  It's better anyways.")
    inline fun Menu.item(textRes: String, iconRes: Int, crossinline setup: MenuItem.() -> Unit) {
        var menuItem: MenuItem? = null
        onMake.add {
            menuItem = add(textRes).apply {
                setIcon(iconRes)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }.apply(setup)
        }
        onAnimateOutStart.add { a, v ->
            removeItem((menuItem ?: return@add).itemId)
        }
    }

    inline fun Menu.item(textRes: Int, iconRes: Int, groupId: Int = 0, id: Int = textRes + iconRes, order: Int = Menu.CATEGORY_CONTAINER or (textRes and 0xFFFF), crossinline setup: MenuItem.() -> Unit) {
        var menuItem: MenuItem? = null
        onMake.add {
            menuItem = add(groupId, id, order, textRes).apply {
                setIcon(iconRes)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }.apply(setup)
        }
        onAnimateOutStart.add { a, v ->
            removeItem((menuItem ?: return@add).itemId)
        }
    }
}
