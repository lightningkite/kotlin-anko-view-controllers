package com.lightningkite.kotlin.anko.full

import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCTabs
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.VirtualObservableProperty
import java.util.*

val VCTabs.indexObs: MutableObservableProperty<Int>
    get() = object : MutableObservableProperty<Int>, MutableList<(Int) -> Unit> by this.onIndexChange {
        override var value: Int
            get() = this@indexObs.index
            set(value) {
                this@indexObs.index = value
            }
    }
private val VCContainer_currentObs = WeakHashMap<VCContainer, VirtualObservableProperty<ViewController>>()
val VCContainer.currentObs: ObservableProperty<ViewController>
    get() = VCContainer_currentObs.getOrPut(this) { VirtualObservableProperty({ this.current }, this.onSwap) }