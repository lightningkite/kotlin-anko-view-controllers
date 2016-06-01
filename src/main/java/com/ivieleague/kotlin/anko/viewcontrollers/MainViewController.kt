package com.ivieleague.kotlin.anko.viewcontrollers

import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.ivieleague.kotlin.anko.elevationCompat
import com.ivieleague.kotlin.anko.viewcontrollers.containers.VCContainer
import com.ivieleague.kotlin.anko.viewcontrollers.containers.VCStack
import com.ivieleague.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.ivieleague.kotlin.lifecycle.bind
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.actionMenuView
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.coordinatorLayout

/**
 * Created by jivie on 3/15/16.
 */
abstract class MainViewController(val backResource: Int, val styleToolbar: Toolbar.() -> Unit) : AnkoViewController() {

    var menuResource: Int = 0
    var toolbar: Toolbar? = null
    var actionMenu: ActionMenuView? = null

    var alwaysShowBack = false

    open fun setToolbarTitle(input: String) {
        toolbar?.apply {
            val index = input.indexOf('\n')
            if (index == -1) {
                this.title = input
            } else {
                this.title = input.substring(0, index)
                this.subtitle = input.substring(index + 1)
            }
        }

    }

    override fun createView(ui: AnkoContext<VCActivity>): View = ui.verticalLayout {

        toolbar = toolbar {
            elevationCompat = dip(4).toFloat()
            styleToolbar()
            actionMenu = actionMenuView {

            }.lparams(Gravity.RIGHT)
        }.lparams(matchParent, wrapContent)

        coordinatorLayout {
            makeSubview(ui.owner).lparams(matchParent, matchParent)
        }.lparams(matchParent, 0, 1f)
    }

    abstract fun ViewGroup.makeSubview(activity: VCActivity): View

    fun update(activity: VCActivity, stack: VCStack, onMenuClick: (() -> Unit)? = null) {
        toolbar?.apply {
            if (stack.size > 1 || alwaysShowBack) {
                setNavigationIcon(backResource)
                setNavigationOnClickListener { activity.onBackPressed() }
            } else if (onMenuClick != null) {
                setNavigationIcon(menuResource)
                setNavigationOnClickListener { onMenuClick() }
            } else {
                navigationIcon = null
                setNavigationOnClickListener { }
            }
            setToolbarTitle(stack.getTitle(resources))
        }
    }

    fun attach(activity: VCActivity, stack: VCStack, onMenuClick: (() -> Unit)? = null) {
        lifecycle.bind(stack.onSwap, stack.current) {
            toolbar?.apply {
                if (stack.size > 1 || alwaysShowBack) {
                    setNavigationIcon(backResource)
                    setNavigationOnClickListener { activity.onBackPressed() }
                } else if (onMenuClick != null) {
                    setNavigationIcon(menuResource)
                    setNavigationOnClickListener { onMenuClick.invoke() }
                } else {
                    navigationIcon = null
                    setNavigationOnClickListener { }
                }
                setToolbarTitle(stack.getTitle(resources))
            }
        }
        doThisOnBackPressed = { stack.onBackPressed(it) }
    }

    fun attach(activity: VCActivity, container: VCContainer) {
        lifecycle.bind(container.onSwap, container.current) {
            toolbar?.apply {
                setToolbarTitle(container.getTitle(resources))
            }
        }
    }

    var doThisOnBackPressed: (backAction: () -> Unit) -> Unit = { it() }
    override fun onBackPressed(backAction: () -> Unit) = doThisOnBackPressed(backAction)
}