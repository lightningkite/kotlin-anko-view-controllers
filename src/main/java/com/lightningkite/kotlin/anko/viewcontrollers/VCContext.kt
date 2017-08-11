package com.lightningkite.kotlin.anko.viewcontrollers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Represents all of the data necessary to create Android views.
 * Created by joseph on 6/9/17.
 */
interface VCContext {
    val activity: Activity?
    val context: Context

    val onResume: MutableCollection<() -> Unit>
    val onPause: MutableCollection<() -> Unit>
    val onSaveInstanceState: MutableCollection<(outState: Bundle) -> Unit>
    val onLowMemory: MutableCollection<() -> Unit>
    val onDestroy: MutableCollection<() -> Unit>
    val onActivityResult: MutableCollection<(request: Int, result: Int, data: Intent?) -> Unit>

    fun prepareOnResult(presetCode: Int = (Math.random() * 65535).toInt(), onResult: (Int, Intent?) -> Unit = { a, b -> }): Int
    fun requestPermissions(permission: Array<String>, onResult: (Map<String, Int>) -> Unit)
    fun requestPermission(permission: String, onResult: (Boolean) -> Unit)
}