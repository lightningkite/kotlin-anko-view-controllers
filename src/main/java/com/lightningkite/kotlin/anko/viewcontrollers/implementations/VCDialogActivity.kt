package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import org.jetbrains.anko.AnkoContext
import java.util.*

/**
 * All activities hosting [ViewController]s must be extended from this one.
 * It handles the calling of other activities with [onActivityResult], the attaching of a
 * [VCContainer], and use the back button on the [VCContainer].
 * Created by jivie on 10/12/15.
 */
class VCDialogActivity : VCActivity() {

    class ContainerData(val container: VCContainer, val layoutParamsSetup: WindowManager.LayoutParams.() -> Unit, val windowModifier: Window.() -> Unit = {}) {
        val vc = ContainerVC(container)
    }

    companion object {
        const val EXTRA_CONTAINER: String = "VCDialogActivity.containerId"
        const val EXTRA_DISMISS_ON_TOUCH_OUTSIDE: String = "VCDialogActivity.dismissOnTouchOutside"
        val containers: HashMap<Int, ContainerData> = HashMap()
    }

    var myIndex = 0
    var myContainerData: ContainerData? = null

    override val viewController: ViewController
        get() = myContainerData?.vc ?: throw IllegalStateException()

    override fun onCreate(savedInstanceState: Bundle?) {
        myIndex = intent.getIntExtra(EXTRA_CONTAINER, 0)
        myContainerData = containers[myIndex] ?: return
        setFinishOnTouchOutside(intent.getBooleanExtra(EXTRA_DISMISS_ON_TOUCH_OUTSIDE, true))
        super.onCreate(savedInstanceState)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (myContainerData != null) {
            window.apply(myContainerData!!.windowModifier)
            windowManager.updateViewLayout(
                    window.decorView,
                    (window.decorView.layoutParams as WindowManager.LayoutParams)
                            .apply(myContainerData!!.layoutParamsSetup))
        }
    }

    override fun finish() {
        containers.remove(myIndex)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!containers.containsKey(myIndex)) {
            myContainerData?.container?.dispose()
        }
    }
}

inline fun Activity.dialog(
        dismissOnTouchOutside: Boolean = true,
        noinline windowModifier: Window.() -> Unit = {},
        noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {},
        crossinline viewMaker: AnkoContext<VCActivity>.(VCStack) -> View
) {
    viewControllerDialog(VCStack().apply {
        push(object : AnkoViewController() {

            override fun onBackPressed(backAction: () -> Unit) {
                if(dismissOnTouchOutside) {
                    backAction()
                }
            }

            override fun createView(ui: AnkoContext<VCActivity>): View {
                return viewMaker(ui, this@apply)
            }
        })
    }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Activity.dialog(
        dismissOnTouchOutside: Boolean = true,
        noinline windowModifier: Window.() -> Unit = {},
        noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {},
        crossinline viewMaker: AnkoViewController.(AnkoContext<VCActivity>, VCStack) -> View
) {
    viewControllerDialog(VCStack().apply {
        push(object : AnkoViewController() {
            override fun onBackPressed(backAction: () -> Unit) {
                if(dismissOnTouchOutside) {
                    backAction()
                }
            }

            override fun createView(ui: AnkoContext<VCActivity>): View {
                return viewMaker(ui, this@apply)
            }
        })
    }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Activity.viewControllerDialog(vcMaker: (VCStack) -> ViewController, dismissOnTouchOutside: Boolean = true, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}) {
    viewControllerDialog(VCStack().apply { push(vcMaker(this)) }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Activity.viewControllerDialog(container: VCContainer, dismissOnTouchOutside: Boolean = true, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}) {
    val id: Int = container.hashCode()
    VCDialogActivity.containers[id] = VCDialogActivity.ContainerData(container, layoutParamModifier, windowModifier)
    startActivity(Intent(this, VCDialogActivity::class.java).apply {
        putExtra(VCDialogActivity.EXTRA_CONTAINER, id)
        putExtra(VCDialogActivity.EXTRA_DISMISS_ON_TOUCH_OUTSIDE, dismissOnTouchOutside)
    })
}

inline fun VCActivity.viewControllerDialog(container: VCContainer, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}, crossinline onDismissed: () -> Unit) {
    val id: Int = container.hashCode()
    VCDialogActivity.containers[id] = VCDialogActivity.ContainerData(container, layoutParamModifier, windowModifier)
    startIntent(
            Intent(this, VCDialogActivity::class.java).apply {
                putExtra(VCDialogActivity.EXTRA_CONTAINER, id)
            },
            onResult = { code, data -> onDismissed() }
    )
}