package com.lightningkite.kotlin.anko.viewcontrollers.containers

import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.observable.property.ObservableProperty

/**
 * Something that contains [ViewController]s and handles the changes between them.
 * Created by jivie on 10/12/15.
 */
@Deprecated("Use observable properties directly", ReplaceWith("ObservableProperty<Any>", "com.lightningkite.kotlin.observable.property.ObservableProperty"))
typealias VCContainer = ObservableProperty<Any>