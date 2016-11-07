package com.lightningkite.kotlin.anko.viewcontrollers

import android.view.View
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContextImpl

/**
 *
 * Created by jivie on 1/19/16.
 *
 */

abstract class AnkoViewController() : CallbackViewController(), AnkoComponent<VCActivity> {
    override final fun makeView(activity: VCActivity): View {
        return createView(AnkoContextImpl(activity, activity, false))
    }
}
