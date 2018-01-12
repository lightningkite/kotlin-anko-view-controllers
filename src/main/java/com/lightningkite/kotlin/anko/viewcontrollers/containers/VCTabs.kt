package com.lightningkite.kotlin.anko.viewcontrollers.containers

import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.lambda.invokeAll
import java.util.*

/**
 * Used to create left/right tabs.
 * @param startIndex The first view controller's index to show.
 * @param vcs The view controllers to display.
 * Created by jivie on 10/14/15.
 */
class VCTabs(startIndex: Int, vcs: List<ViewController>) : VCContainerImpl() {

    constructor(startIndex: Int, vararg vcs: ViewController) : this(startIndex, vcs.toList())

    val viewControllers: Array<ViewController> = Array(vcs.size, { vcs[it] })
    val onIndexChange = ArrayList<(Int) -> Unit>()
    var index: Int = startIndex
        set(value) {
            if (value == field) return
            field = value
            onIndexChange.invokeAll(value)
        }

    override val current: ViewController get() = viewControllers[index]

    fun swap(newIndex: Int) {
        index = newIndex
    }

    fun replace(modifyIndex: Int, newController: ViewController) {
        viewControllers[modifyIndex] = newController
        if (index == modifyIndex) {
            swapListener?.invoke(newController,
                    AnimationSet.fade,
                    {})
            onSwap.forEach { it(current) }
        }
    }

    var oldIndex: Int = startIndex
    val onChangeListener: (Int) -> Unit = { it: Int ->
        swapListener?.invoke(viewControllers[it],
                if (it > oldIndex) {
                    AnimationSet.slidePush
                } else {
                    AnimationSet.slidePop
                },
                {})
        oldIndex = it
        onSwap.forEach { it(current) }
    }

    init {
        onIndexChange.add(onChangeListener)
    }

    override fun dispose() {
        onIndexChange.remove(onChangeListener)
        viewControllers.forEach { it.dispose() }
    }

}