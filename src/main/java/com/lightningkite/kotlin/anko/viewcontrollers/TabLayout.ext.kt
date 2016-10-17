package com.lightningkite.kotlin.anko.viewcontrollers

import android.support.design.widget.TabLayout
import android.view.View
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCTabs

/**
 * Created by jivie on 6/6/16.
 */

inline fun TabLayout.setUpWithVCTabs(vcTabs: VCTabs, crossinline onReselect: (Int) -> Unit, crossinline onSelectBeforeChange: (Int) -> Unit, crossinline tabBuilder: TabLayout.Tab.(Int) -> Unit) {

    val offset = tabCount

    var index = 0

    for (vc in vcTabs.viewControllers) {
        val tab = newTab()
        tab.icon
        tab.text = vc.getTitle(resources)
        tab.tabBuilder(index - offset)
        addTab(tab)
        if (index == vcTabs.index) {
            tab.select()
        }
        index++
    }

    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

        val listener: (Int) -> Unit = {
            if (it == selectedTabPosition) {

            } else {
                getTabAt(it)?.select()
            }
        }

        override fun onViewAttachedToWindow(v: View?) {
            vcTabs.onIndexChange.add(listener)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            vcTabs.onIndexChange.remove(listener)
        }
    })

    setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {
            onReselect(tab.position - offset)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            onSelectBeforeChange(tab.position - offset)
            if (vcTabs.index != tab.position - offset)
                vcTabs.index = tab.position - offset
        }

    })
}