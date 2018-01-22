package com.lightningkite.kotlin.anko.viewcontrollers.containers

import android.content.res.Resources
import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import java.io.Closeable
import java.util.*

/**
 * Something that contains [ViewController]s and handles the changes between them.
 * Created by jivie on 10/12/15.
 */
interface VCContainer : Closeable {

    var swapListener: ((newVC: ViewController, AnimationSet?, onFinish: () -> Unit) -> Unit)?
    val onSwap: MutableList<(ViewController) -> Unit>

    val current: ViewController

    fun onBackPressed(backAction: () -> Unit) {
        current.onBackPressed(backAction)
    }

    fun getTitle(resources: Resources): String {
        return current.getTitle(resources)
    }
}

abstract class VCContainerImpl : VCContainer {
    override var swapListener: ((newVC: ViewController, AnimationSet?, onFinish: () -> Unit) -> Unit)? = null
    override val onSwap: MutableList<(ViewController) -> Unit> = ArrayList()
}