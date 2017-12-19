package com.lightningkite.kotlin.anko.viewcontrollers

import android.content.Intent
import android.os.Bundle
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import org.jetbrains.anko.bundleOf
import java.util.*

fun VCContext.startIntent(intent: Intent, options: Bundle = bundleOf(), onResult: (Int, Intent?) -> Unit = { _, _ -> }) {
    activity?.startActivityForResult(intent, prepareOnResult(onResult = onResult), options)
}

private val VCContext_activeLifecycle = WeakHashMap<VCContext, LifecycleConnectable>()
val VCContext.activeLifecycle: LifecycleConnectable
    get() = VCContext_activeLifecycle.getOrPut(this) {
        return object : LifecycleConnectable {
            val listeners = ArrayList<LifecycleListener>()

            override fun connect(listener: LifecycleListener) {
                listeners += listener
            }
        }
    }