package com.lightningkite.kotlin.anko.viewcontrollers.containers

import com.lightningkite.kotlin.anko.animation.AnimationSet
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import java.util.*

/**
 *
 * Created by shanethompson on 7/21/17.
 *
 */

class TypedVCStack<T : ViewController> : VCContainerImpl() {
    override val current: T get() = internalStack.peek()

    /**
     * Gets the view controller that is [index] pops back.
     * 0 = the current view controller
     * 1 = the next view controller if pop() is called
     * size - 1 = the view controller at the bottom of the stack
     */
    operator fun get(index: Int): T {
        return internalStack[internalStack.size - 1 - index]
    }

    var defaultPushAnimation: AnimationSet = AnimationSet.slidePush
    var defaultPopAnimation: AnimationSet = AnimationSet.slidePop
    var defaultSwapAnimation: AnimationSet = AnimationSet.fade

    val size: Int get() = internalStack.size
    val isEmpty: Boolean get() = internalStack.isEmpty()
    var onEmptyListener: () -> Unit = {}


    var stack: Stack<T>
        get() = internalStack
        set(value) = setStack(value)
    private var internalStack: Stack<T> = Stack()

    /**
     * Sets the stack and updates the view.
     */
    fun setStack(newStack: Stack<T>, animationSet: AnimationSet? = defaultPushAnimation): Unit {
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
    fun push(viewController: T, animationSet: AnimationSet? = defaultPushAnimation) {
        internalStack.push(viewController)
        swapListener?.invoke(current, animationSet) {}
        onSwap.forEach { it(current) }
    }

    /**
     * Removes the top controllers off the stack.
     */
    fun pop(animationSet: AnimationSet? = defaultPopAnimation) {
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
    fun root(animationSet: AnimationSet? = defaultPopAnimation) {
        val toDispose = ArrayList<T>(internalStack)
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
    fun back(predicate: (T) -> Boolean, animationSet: AnimationSet? = defaultPopAnimation) {
        val toDispose = ArrayList<T>()
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
    fun swap(viewController: T, animationSet: AnimationSet? = defaultSwapAnimation) {
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
    fun reset(viewController: T, animationSet: AnimationSet? = defaultPushAnimation) {
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
    override fun dispose() {
        for (vc in internalStack) {
            vc.dispose()
        }
    }
}
