package com.lightningkite.kotlin.anko.viewcontrollers.dialogs

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.lightningkite.kotlin.anko.hideSoftInput
import com.lightningkite.kotlin.anko.selectableItemBackgroundBorderlessResource
import com.lightningkite.kotlin.anko.snackbar
import com.lightningkite.kotlin.anko.textColorResource
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCStack
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.dialog
import org.jetbrains.anko.*

private inline fun ViewGroup.MarginLayoutParams.standardMargins(ctx: Context) {
    leftMargin = ctx.dip(16)
    rightMargin = ctx.dip(16)
    topMargin = ctx.dip(8)
    bottomMargin = ctx.dip(8)
}

private inline fun TextView.styleTitle() {
    textSize = 18f
    setTypeface(null, Typeface.BOLD)
    textColorResource = R.color.primary_text_light
}

private inline fun TextView.styleMessage() {
    textSize = 16f
    textColorResource = R.color.secondary_text_light
}

private inline fun Button.styleNormal() {
    textSize = 16f
    textColorResource = R.color.secondary_text_light
    setAllCaps(true)
    backgroundResource = selectableItemBackgroundBorderlessResource
}

private inline fun Button.styleDestructive() {
    textSize = 16f
    textColor = Color.RED
    setAllCaps(true)
}

object StandardDialog {
    fun okButton(resources: Resources, okResource: Int = R.string.ok, action: () -> Unit = {}): Pair<String, (VCStack) -> Unit> =
            resources.getString(okResource) to { it: VCStack -> action(); it.pop() }

    fun cancelButton(resources: Resources, cancelResource: Int = R.string.cancel, action: () -> Unit = {}): Pair<String, (VCStack) -> Unit> =
            resources.getString(cancelResource) to { it: VCStack -> action(); it.pop() }

    fun cancelButton(resources: Resources): Pair<String, (VCStack) -> Unit> = resources.getString(R.string.cancel) to { it: VCStack -> it.pop() }


    inline fun ViewGroup.MarginLayoutParams.standardMargins(ctx: Context) {
        leftMargin = ctx.dip(16)
        rightMargin = ctx.dip(16)
        topMargin = ctx.dip(8)
        bottomMargin = ctx.dip(8)
    }

    inline fun TextView.styleTitle() {
        textSize = 18f
        setTypeface(null, Typeface.BOLD)
        textColorResource = R.color.primary_text_light
    }

    inline fun TextView.styleMessage() {
        textSize = 16f
        textColorResource = R.color.secondary_text_light
    }

    inline fun Button.styleNormal() {
        textSize = 16f
        textColorResource = R.color.secondary_text_light
        setAllCaps(true)
        backgroundResource = selectableItemBackgroundBorderlessResource
    }

    private inline fun Button.styleDestructive() {
        textSize = 16f
        textColor = Color.RED
        setAllCaps(true)
    }
}

fun Activity.alertDialog(message: Int) = standardDialog(
        null,
        resources.getString(message),
        listOf(StandardDialog.okButton(resources) {}),
        true,
        null
)

fun Activity.standardDialog(
        title: Int?,
        message: Int?,
        buttons: List<Pair<String, (VCStack) -> Unit>>,
        dismissOnClickOutside: Boolean = true,
        content: (ViewGroup.(VCStack) -> View)? = null
) = standardDialog(
        if (title != null) resources.getString(title) else null,
        if (message != null) resources.getString(message) else null,
        buttons,
        dismissOnClickOutside,
        content
)

object CustomDialog {
    fun okButton(resources: Resources, okResource: Int = R.string.ok, action: () -> Unit = {}, okStyle: (Button) -> Unit): Triple<String, (VCStack) -> Unit, (Button) -> Unit> =
            Triple(resources.getString(okResource), { it: VCStack -> action(); it.pop() }, okStyle)

    fun cancelButton(resources: Resources, cancelResource: Int = R.string.cancel, action: () -> Unit = {}, cancelStyle: (Button) -> Unit): Triple<String, (VCStack) -> Unit, (Button) -> Unit> =
            Triple(resources.getString(cancelResource), { it: VCStack -> action(); it.pop() }, cancelStyle)
}

/**
 * Creates a psuedo-dialog that is actually an activity.  Significantly more stable and safe.
 */
fun Activity.standardDialog(
        title: String?,
        message: String?,
        buttons: List<Pair<String, (VCStack) -> Unit>>,
        dismissOnClickOutside: Boolean = true,
        content: (ViewGroup.(VCStack) -> View)? = null
) {
    return dialog(dismissOnClickOutside, layoutParamModifier = { width = matchParent }) { ui, vcStack ->
        ui.scrollView {
            verticalLayout {
                //title
                textView(title) {
                    styleTitle()
                    if (title.isNullOrEmpty()) {
                        visibility = View.GONE
                    }
                }.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                    topMargin = dip(16)
                }

                //message
                textView(message) {
                    styleMessage()
                    if (message.isNullOrEmpty()) {
                        visibility = View.GONE
                    }
                }.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                }

                //custom content
                content?.invoke(this, vcStack)?.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                }

                //buttons
                linearLayout {
                    gravity = Gravity.END
                    for ((buttonName, action) in buttons) {
                        button(buttonName) {
                            styleNormal()
                            onClick {
                                action(vcStack)
                            }
                        }.lparams(wrapContent, wrapContent) {
                            standardMargins(context)
                        }
                    }
                }.lparams(matchParent, wrapContent)
            }
        }
    }
}

fun Activity.customDialog(
        title: Int?,
        message: Int,
        buttons: List<Triple<String, (VCStack) -> Unit, (Button) -> Unit>>,
        dismissOnClickOutside: Boolean = true,
        content: (ViewGroup.(VCStack) -> View)? = null
) = customDialog(
        if (title != null) resources.getString(title) else null,
        resources.getString(message),
        buttons,
        dismissOnClickOutside,
        content
)

/**
 * Creates a psuedo-dialog that is actually an activity.  Significantly more stable and safe.
 */
fun Activity.customDialog(
        title: String?,
        message: String,
        buttons: List<Triple<String, (VCStack) -> Unit, (Button) -> Unit>>,
        dismissOnClickOutside: Boolean = true,
        content: (ViewGroup.(VCStack) -> View)? = null
) {
    return dialog(dismissOnClickOutside, layoutParamModifier = { width = matchParent }) { ui, vcStack ->
        ui.scrollView {
            verticalLayout {
                //title
                textView(title) {
                    styleTitle()
                    if (title.isNullOrEmpty()) {
                        visibility = View.GONE
                    }
                }.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                    topMargin = dip(16)
                }

                //message
                textView(message) {
                    styleMessage()
                }.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                }

                //custom content
                content?.invoke(this, vcStack)?.lparams(matchParent, wrapContent) {
                    standardMargins(context)
                }

                //buttons
                linearLayout {
                    gravity = Gravity.END
                    buttons.forEach { triple ->
                        button(triple.first) {
                            onClick { triple.second(vcStack) }
                            triple.third.invoke(this)
                        }.lparams {
                            standardMargins(context)
                        }
                    }
                }.lparams(matchParent, wrapContent)
            }
        }
    }
}

fun Activity.confirmationDialog(title: Int? = null, message: Int, onCancel: () -> Unit = {}, onConfirm: () -> Unit) {
    return standardDialog(title, message, listOf(StandardDialog.okButton(resources, action = onConfirm), StandardDialog.cancelButton(resources, action = onCancel)))
}

fun Activity.confirmationDialog(title: String? = null, message: String, onCancel: () -> Unit = {}, onConfirm: () -> Unit) {
    return standardDialog(title, message, listOf(StandardDialog.okButton(resources, action = onConfirm), StandardDialog.cancelButton(resources, action = onCancel)))
}

fun Activity.confirmationDialog(title: String? = null, message: String, okResource: Int = R.string.ok, cancelResource: Int = R.string.cancel, dismissOnClickOutside: Boolean = true, onPositiveAction: () -> Unit, onNegativeAction: () -> Unit) {
    return standardDialog(
            title,
            message,
            listOf(StandardDialog.okButton(resources, okResource, onPositiveAction), StandardDialog.cancelButton(resources, cancelResource, onNegativeAction)),
            dismissOnClickOutside = dismissOnClickOutside)
}

fun Activity.confirmationDialog(title: Int? = null, message: Int, okResource: Int = R.string.ok, cancelResource: Int = R.string.cancel, dismissOnClickOutside: Boolean = true, onPositiveAction: () -> Unit, onNegativeAction: () -> Unit) {
    return standardDialog(
            title,
            message,
            listOf(StandardDialog.okButton(resources, okResource, onPositiveAction), StandardDialog.cancelButton(resources, cancelResource, onNegativeAction)),
            dismissOnClickOutside = dismissOnClickOutside)
}

fun Activity.customConfirmationDialog(title: Int? = null, message: Int, okResource: Int = R.string.ok, cancelResource: Int = R.string.cancel, dismissOnClickOutside: Boolean = true, onPositiveAction: () -> Unit, onNegativeAction: () -> Unit, okStyle: Button.() -> Unit, cancelStyle: Button.() -> Unit) {
    return customDialog(
            title,
            message,
            listOf(CustomDialog.okButton(resources, okResource, onPositiveAction, okStyle), CustomDialog.cancelButton(resources, cancelResource, onNegativeAction, cancelStyle)),
            dismissOnClickOutside = dismissOnClickOutside
    )
}

fun Activity.infoDialog(title: Int? = null, message: Int, content: (ViewGroup.(VCStack) -> View)? = null, onConfirm: () -> Unit = {}) {
    return standardDialog(title, message, listOf(StandardDialog.okButton(resources, action = onConfirm)), content = content)
}

fun Activity.infoDialog(title: String? = null, message: String, content: (ViewGroup.(VCStack) -> View)? = null, onConfirm: () -> Unit = {}) {
    return standardDialog(title, message, listOf(StandardDialog.okButton(resources, action = onConfirm)), content = content)
}


/**
 * Creates a dialog with an input text field on it.
 */
fun Activity.inputDialog(title: Int, message: Int, hint: Int = 0, inputType: Int = InputType.TYPE_CLASS_TEXT, canCancel: Boolean = true, validation: (String) -> Int? = { null }, onResult: (String?) -> Unit) {
    return inputDialog(resources.getString(title), resources.getString(message), if (hint == 0) "" else resources.getString(hint), inputType, canCancel, validation, onResult)
}


/**
 * Creates a dialog with an input text field on it.
 */
fun Activity.inputDialog(
        title: String,
        message: String,
        hint: String = "",
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        canCancel: Boolean = true,
        validation: (String) -> Int? = { null },
        onResult: (String?) -> Unit
) {
    var et: EditText? = null
    standardDialog(
            title,
            message,
            listOf(
                    resources.getString(R.string.cancel)!! to { it: VCStack ->
                        hideSoftInput()
                        onResult(null)
                        it.pop()
                    },
                    resources.getString(R.string.ok)!! to { it: VCStack ->
                        if (et != null) {
                            hideSoftInput()
                            val result = et!!.text.toString()
                            val error = validation(result)
                            if (error == null) {
                                onResult(result)
                                it.pop()
                            } else {
                                snackbar(error)
                            }
                        }
                    }
            ),
            canCancel,
            {
                et = editText() {
                    this.inputType = inputType
                    this.hint = hint
                }
                et!!
            }
    )
}