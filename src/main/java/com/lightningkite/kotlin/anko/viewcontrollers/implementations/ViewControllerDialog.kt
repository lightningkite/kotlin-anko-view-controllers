package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.Window
import android.widget.RelativeLayout
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import org.jetbrains.anko._RelativeLayout
import org.jetbrains.anko.matchParent

/**
 * Allows you to create a dialog using a view controller.
 * Created by jivie on 9/25/15.
 */
@Deprecated("This uses a standard alert dialog which is deprecated.")
fun VCActivity.dialog(
        vcMaker: (AlertDialog) -> ViewController
) {
    var dialog: AlertDialog? = null
    var dismissed: Boolean = false
    val builder = AlertDialog.Builder(this)
    dialog = builder.create()
    val viewController = vcMaker(dialog)
    val view = viewController.make(this)
    dialog!!.setView(view, 0, 0, 0, 0)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setOnDismissListener(DialogInterface.OnDismissListener { viewController.unmake(view) })
    dialog.show()
}

@Deprecated("This uses a standard alert dialog which is deprecated.")
fun VCActivity.dialog(
        stack: VCStack
) {
    var dialog: AlertDialog? = null
    var dismissed: Boolean = false
    val builder = AlertDialog.Builder(this)

    stack.onEmptyListener = {
        dialog?.dismiss()
    }
    val view = _RelativeLayout(this)
    val embedder = VCContainerEmbedder(view, stack, { RelativeLayout.LayoutParams(matchParent, matchParent) })
    embedder.animateInComplete(this, view)

    dialog = builder.create()
    dialog!!.setView(view, 0, 0, 0, 0)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setOnDismissListener {
        embedder.animateOutStart(this, view)
        embedder.unmake()
        stack.dispose()
    }
    dialog.show()
}