package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.VCContext
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import com.lightningkite.kotlin.anko.viewcontrollers.startIntent
import org.jetbrains.anko.AnkoContext
import java.util.*

/**
 * A specific [VCActivity]
 * Created by jivie on 10/12/15.
 */
//TODO: How do we replace this?
@Deprecated("Deprecated along with ViewControllers in general.")
class VCDialogActivity : VCActivity() {

    class ContainerData(val container: VCStack, val layoutParamsSetup: WindowManager.LayoutParams.() -> Unit, val windowModifier: Window.() -> Unit = {}) {
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
        get() = myContainerData?.vc ?: ViewController.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        myIndex = intent.getIntExtra(EXTRA_CONTAINER, 0)
        myContainerData = containers[myIndex]
        if (myContainerData != null) {
            myContainerData!!.container.onEmptyListener = {
                finish()
            }
            setFinishOnTouchOutside(intent.getBooleanExtra(EXTRA_DISMISS_ON_TOUCH_OUTSIDE, true))
            super.onCreate(savedInstanceState)
        } else {
            super.onCreate(savedInstanceState)
            finish()
        }
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

    override fun onBackPressed() {
        if (intent.getBooleanExtra(EXTRA_DISMISS_ON_TOUCH_OUTSIDE, true)) {
            super.onBackPressed()
        }
    }

    override fun finish() {
        containers.remove(myIndex)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!containers.containsKey(myIndex)) {
            myContainerData?.container?.close()
        }
    }
}

inline fun Context.dialog(
        dismissOnTouchOutside: Boolean = true,
        noinline windowModifier: Window.() -> Unit = {},
        noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {},
        crossinline viewMaker: AnkoContext<VCContext>.(VCStack) -> View
) {
    viewControllerDialog(VCStack().apply {
        push(object : AnkoViewController() {

            override fun onBackPressed(backAction: () -> Unit) {
                if(dismissOnTouchOutside) {
                    backAction()
                }
            }

            override fun createView(ui: AnkoContext<VCContext>): View {
                return viewMaker(ui, this@apply)
            }
        })
    }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Context.dialog(
        dismissOnTouchOutside: Boolean = true,
        noinline windowModifier: Window.() -> Unit = {},
        noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {},
        crossinline viewMaker: AnkoViewController.(AnkoContext<VCContext>, VCStack) -> View
) {
    viewControllerDialog(VCStack().apply {
        push(object : AnkoViewController() {
            override fun onBackPressed(backAction: () -> Unit) {
                if(dismissOnTouchOutside) {
                    backAction()
                }
            }

            override fun createView(ui: AnkoContext<VCContext>): View {
                return viewMaker(ui, this@apply)
            }
        })
    }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Context.viewControllerDialog(vcMaker: (VCStack) -> ViewController, dismissOnTouchOutside: Boolean = true, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}) {
    viewControllerDialog(VCStack().apply { push(vcMaker(this)) }, dismissOnTouchOutside, windowModifier = windowModifier, layoutParamModifier = layoutParamModifier)
}

inline fun Context.viewControllerDialog(container: VCStack, dismissOnTouchOutside: Boolean = true, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}) {
    val id: Int = container.hashCode()
    VCDialogActivity.containers[id] = VCDialogActivity.ContainerData(container, layoutParamModifier, windowModifier)
    startActivity(Intent(this, VCDialogActivity::class.java).apply {
        putExtra(VCDialogActivity.EXTRA_CONTAINER, id)
        putExtra(VCDialogActivity.EXTRA_DISMISS_ON_TOUCH_OUTSIDE, dismissOnTouchOutside)
    })
}

inline fun VCContext.viewControllerDialog(container: VCStack, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}, crossinline onDismissed: () -> Unit) {
    val id: Int = container.hashCode()
    VCDialogActivity.containers[id] = VCDialogActivity.ContainerData(container, layoutParamModifier, windowModifier)
    startIntent(
            Intent(context, VCDialogActivity::class.java).apply {
                putExtra(VCDialogActivity.EXTRA_CONTAINER, id)
            },
            onResult = { code, data -> onDismissed() }
    )
}

inline fun VCContext.viewControllerDialog(container: VCStack, noinline windowModifier: Window.() -> kotlin.Unit = {}, noinline layoutParamModifier: WindowManager.LayoutParams.() -> Unit = {}) {
    val id: Int = container.hashCode()
    VCDialogActivity.containers[id] = VCDialogActivity.ContainerData(container, layoutParamModifier, windowModifier)
    startIntent(
            Intent(context, VCDialogActivity::class.java).apply {
                putExtra(VCDialogActivity.EXTRA_CONTAINER, id)
            },
            onResult = { code, data -> }
    )
}