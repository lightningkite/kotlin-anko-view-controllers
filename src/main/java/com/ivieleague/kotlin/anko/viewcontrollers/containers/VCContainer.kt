package com.ivieleague.kotlin.anko.viewcontrollers.containers

import android.content.res.Resources
import com.ivieleague.kotlin.Disposable
import com.ivieleague.kotlin.anko.animation.AnimationSet
import com.ivieleague.kotlin.anko.viewcontrollers.ViewController
import java.util.*

/**
 * Something that contains [ViewController]s and handles the changes between them.
 * Created by jivie on 10/12/15.
 */
interface VCContainer : Disposable {

    var swapListener: ((newVC: ViewController, AnimationSet?, onFinish: () -> Unit) -> Unit)?
    val onSwap: MutableList<(ViewController) -> Unit>

    val current: ViewController

    fun onBackPressed(backAction: () -> Unit) {
        current.onBackPressed (backAction)
    }

    fun getTitle(resources: Resources): String {
        return current.getTitle(resources)
    }
}

abstract class VCContainerImpl : VCContainer {
    override var swapListener: ((newVC: ViewController, AnimationSet?, onFinish: () -> Unit) -> Unit)? = null
    override val onSwap: MutableList<(ViewController) -> Unit> = ArrayList()
}