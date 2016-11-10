package com.lightningkite.kotlin.anko.viewcontrollers

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.ContainerVC
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCContainerEmbedder
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.runAll
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import java.util.*

/**
 * This view controller class implements a number of events that can be registered to, including [onMake], [onAnimateInComplete], [onAnimateOutStart], [onUnmake], and [onDispose].
 *
 * You register by adding a lambda to them, and can unregister by removing the lambda.  After any of these events are called, the lambdas for that event are cleared, ensuring that the lambda will only ever be called once.
 *
 * CallbackViewController also has some useful functions that depend on these lambdas.
 *
 * Created by jivie on 1/19/16.
 */

abstract class CallbackViewController() : ViewController {

    val onMake: ArrayList<(View) -> Unit> = ArrayList()
    val onAnimateInComplete: ArrayList<(VCActivity, View) -> Unit> = ArrayList()
    val onAnimateOutStart: ArrayList<(VCActivity, View) -> Unit> = ArrayList()
    val onUnmake: ArrayList<(View) -> Unit> = ArrayList()
    val onDispose: ArrayList<() -> Unit> = ArrayList()

    /**
     * A lifecycle object that is called when a view is created or destroyed.
     */
    val viewLifecycle: LifecycleConnectable = object : LifecycleConnectable {
        override fun connect(listener: LifecycleListener) {
            onMake.add { listener.onStart() }
            onUnmake.add { listener.onStop() }
        }
    }
    /**
     * A lifecycle object that is called when the whole view controller is created or destroyed.
     */
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
    @Deprecated("Use [CallbackViewController.listen] instead.")
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
    @Deprecated("Use [CallbackViewController.listen] instead.")
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


    /**
     * Creates a view that shows whatever is in the view container, transitioning between view controllers as needed.
     */
    inline fun AnkoContext<*>.viewContainer(container: VCContainer): View {
        return viewController(ContainerVC(container, false, { FrameLayout.LayoutParams(matchParent, matchParent) }), {})
    }

    /**
     * Creates a view that shows whatever is in the view container, transitioning between view controllers as needed.
     */
    inline fun AnkoContext<*>.viewContainer(container: VCContainer, init: View.() -> Unit): View {
        return viewController(ContainerVC(container, false, { FrameLayout.LayoutParams(matchParent, matchParent) }), init)
    }

    /**
     * Creates a view that shows a single [ViewController].
     */
    inline fun AnkoContext<*>.viewController(controller: ViewController, init: View.() -> Unit): View {
        val view = controller.make(ctx as VCActivity)
        addView(view, ViewGroup.LayoutParams(matchParent, matchParent))
        view.init()
        onAnimateInComplete.add { activity, view ->
            controller.animateInComplete(activity, view)
        }
        onAnimateOutStart.add { activity, view ->
            controller.animateOutStart(activity, view)
        }
        onUnmake.add {
            controller.unmake(view)
        }
        return view
    }

    /**
     * Creates a view that shows whatever is in the view container, transitioning between view controllers as needed.
     */
    inline fun ViewGroup.viewContainer(container: VCContainer): View {
        return viewController(ContainerVC(container, false, { FrameLayout.LayoutParams(matchParent, matchParent) }), {})
    }

    /**
     * Creates a view that shows whatever is in the view container, transitioning between view controllers as needed.
     */
    inline fun ViewGroup.viewContainer(container: VCContainer, init: View.() -> Unit): View {
        return viewController(ContainerVC(container, false, { FrameLayout.LayoutParams(matchParent, matchParent) }), init)
    }

    /**
     * Creates a view that shows a single [ViewController].
     */
    inline fun ViewGroup.viewController(controller: ViewController, init: View.() -> Unit): View {
        val view = controller.make(context as VCActivity)
        addView(view)
        view.init()
        onAnimateInComplete.add { activity, view ->
            controller.animateInComplete(activity, view)
        }
        onAnimateOutStart.add { activity, view ->
            controller.animateOutStart(activity, view)
        }
        onUnmake.add {
            controller.unmake(view)
        }
        return view
    }

    /**
     * Embeds the views generated by view controllers within [container] in the receiver view, animating them in and out as needed.
     */
    fun <ROOT : ViewGroup> ROOT.embedViewContainer(container: VCContainer, makeLayoutParams: () -> ViewGroup.LayoutParams) {
        val embedder = VCContainerEmbedder(this@embedViewContainer, container, makeLayoutParams)
        onAnimateInComplete += { a, b -> embedder.animateInComplete(a, b) }
        onAnimateOutStart += { a, b -> embedder.animateOutStart(a, b) }
        onUnmake += { embedder.unmake() }
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

    /**
     * Adds an item to a menu for the duration of this ViewController.
     * Does not support more than one view created by the same view controller.
     */
    inline fun Menu.item(
            textRes: Int,
            iconRes: Int,
            groupId: Int = 0,
            id: Int = textRes + iconRes,
            order: Int = Menu.CATEGORY_CONTAINER or (textRes and 0xFFFF),
            crossinline setup: MenuItem.() -> Unit
    ) {
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
