package com.lightningkite.kotlin.anko.viewcontrollers.containers

import android.content.res.Resources
import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.observable.property.ObservableProperty
import java.io.Closeable

/**
 * Something that contains [ViewController]s and handles the changes between them.
 * Created by jivie on 10/12/15.
 */
@Deprecated("Deprecated along with ViewControllers in general.")
interface VCContainer : Closeable, ObservableProperty<ViewController> {

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