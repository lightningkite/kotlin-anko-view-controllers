package com.lightningkite.kotlin.anko.viewcontrollers.containers

import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import java.util.*

/**
 * A stack of [ViewController]s.  You can [push] and [pop] them, among other things.
 * Any controllers popped off the stack are disposed.
 *
 * Created by jivie on 10/12/15.
 */
@Deprecated("Deprecated along with ViewControllers in general.", ReplaceWith("StackObservableProperty<Any>", "com.lightningkite.kotlin.observable.property"))
open class VCStack() : VCContainerImpl(), VCStackInterface<ViewController> {
    override val current: ViewController get() = internalStack.peek()

    /**
     * Gets the view controller that is [index] pops back.
     * 0 = the current view controller
     * 1 = the next view controller if pop() is called
     * size - 1 = the view controller at the bottom of the stack
     */
    operator fun get(index: Int): ViewController {
        return internalStack[internalStack.size - 1 - index]
    }

    override var defaultPushAnimation: AnimationSet = AnimationSet.slidePush
    override var defaultPopAnimation: AnimationSet = AnimationSet.slidePop
    override var defaultSwapAnimation: AnimationSet = AnimationSet.fade

    val size: Int get() = internalStack.size
    val isEmpty: Boolean get() = internalStack.isEmpty()
    var onEmptyListener: () -> Unit = {}


    var stack: Stack<ViewController>
        get() = internalStack
        set(value) = setStack(value)
    private var internalStack: Stack<ViewController> = Stack()

    /**
     * Sets the stack and updates the view.
     */
    override fun setStack(newStack: Stack<ViewController>, animationSet: AnimationSet?): Unit {
        val toDispose = internalStack.filter { !newStack.contains(it) }
        internalStack = newStack
        swapListener?.invoke(current, animationSet) {
            toDispose.forEach {
                it.dispose()
            }
        }
        onSwap.forEach { it(current) }
    }


    /**
     * Pushes a new controllers onto the stack.
     */
    override fun push(viewController: ViewController, animationSet: AnimationSet?) {
        internalStack.push(viewController)
        swapListener?.invoke(current, animationSet) {}
        onSwap.forEach { it(current) }
    }

    /**
     * Removes the top controllers off the stack.
     */
    override fun pop(animationSet: AnimationSet?) {
        if (internalStack.size <= 1) {
            onEmptyListener()
        } else {
            val toDispose = internalStack.pop()
            swapListener?.invoke(current, animationSet) {
                toDispose.dispose()
            }
            onSwap.forEach { it(current) }
        }
    }

    /**
     * Pops all of the controllers except fo the first one.
     */
    override fun root(animationSet: AnimationSet?) {
        val toDispose = ArrayList<ViewController>(internalStack)
        val root = toDispose.removeAt(0)
        while (stack.isNotEmpty()) {
            stack.pop()
        }
        stack.push(root)
        swapListener?.invoke(current, animationSet) {
            toDispose.forEach {
                it.dispose()
            }
        }
        onSwap.forEach { it(current) }
    }

    /**
     * Pops controllers off the stack until [predicate] returns true.
     */
    override fun back(predicate: (ViewController) -> Boolean, animationSet: AnimationSet?) {
        val toDispose = ArrayList<ViewController>()
        while (!predicate(internalStack.peek())) {
            toDispose.add(internalStack.pop())
            if (internalStack.size == 0) throw IllegalArgumentException("There is no view controller that matches this predicate!")
        }
        swapListener?.invoke(current, animationSet) {
            toDispose.forEach {
                it.dispose()
            }
        }
        onSwap.forEach { it(current) }
    }

    /**
     * Swaps the top controller with another one.
     */
    override fun swap(viewController: ViewController, animationSet: AnimationSet?) {
        val toDispose = internalStack.pop()
        internalStack.push(viewController)
        swapListener?.invoke(current, animationSet) {
            toDispose.dispose()
        }
        onSwap.forEach { it(current) }
    }

    /**
     * Clears the stack and initiates the stack with a single controller.
     */
    override fun reset(viewController: ViewController, animationSet: AnimationSet?) {
        val toDispose = ArrayList(internalStack)
        internalStack.clear()
        internalStack.push(viewController)
        swapListener?.invoke(current, animationSet) {
            toDispose.forEach { it.dispose() }
        }
        onSwap.forEach { it(current) }
    }

    /**
     * Pops a controller off the stack if available; otherwise it calls onEmptyListener.
     */
    override fun onBackPressed(backAction: () -> Unit) {
        if (internalStack.size == 0) {
            backAction()
        } else if (internalStack.size == 1) {
            current.onBackPressed {
                backAction()
            }
        } else {
            current.onBackPressed {
                pop()
            }
        }
    }

    /**
     * Disposes all of the view controllers in the stack.
     */
    override fun close() {
        for (vc in internalStack) {
            vc.close()
        }
    }
}