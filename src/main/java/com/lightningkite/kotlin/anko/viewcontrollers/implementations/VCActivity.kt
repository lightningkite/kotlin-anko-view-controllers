package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.os.Bundle
import android.view.View
import com.lightningkite.kotlin.anko.activity.AccessibleActivity
import com.lightningkite.kotlin.anko.viewcontrollers.VCContext
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer

/**
 * All activities hosting [ViewController]s must be extended from this one.
 * It handles the calling of other activities with [onActivityResult], the attaching of a
 * [VCContainer], and use the back button on the [VCContainer].
 * Created by jivie on 10/12/15.
 */
abstract class VCActivity : AccessibleActivity(), VCContext {

    abstract val viewController: ViewController

    var vcView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vcView = viewController.make(this)
        setContentView(vcView!!)
    }

    override fun onBackPressed() {
        viewController.onBackPressed {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (vcView != null) {
            viewController.unmake(vcView!!)
            vcView = null
        }
        super.onDestroy()
    }
}