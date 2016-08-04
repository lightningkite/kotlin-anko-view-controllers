package com.lightningkite.kotlin.anko.viewcontrollers.net

import android.view.View
import com.lightningkite.kotlin.anko.viewcontrollers.AnkoViewController
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import org.jetbrains.anko.*

/**
 * Created by joseph on 8/4/16.
 */
class HTMLViewController(val title: String?, val html: String) : AnkoViewController() {
    override fun createView(ui: AnkoContext<VCActivity>): View = ui.verticalLayout {
        if (title != null) {
            textView(title) {
                padding = dip(8)
            }.lparams(matchParent, wrapContent)
        }
        webView {
            println("html= $html")
            loadData(html, "text/html", "UTF-8")
        }.lparams(matchParent, matchParent)
    }
}