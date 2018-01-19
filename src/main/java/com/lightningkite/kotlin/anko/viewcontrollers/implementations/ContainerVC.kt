package com.lightningkite.kotlin.anko.viewcontrollers.implementations

import android.content.res.Resources
import android.view.View
import android.widget.FrameLayout
import com.lightningkite.kotlin.anko.viewcontrollers.VCContext
import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.matchParent
import java.util.*

/**
 * Contains a given [VCContainer], embedding the container of views inside this view controller.
 * Useful if you want to have a smaller section of your view that changes, like you might with tabs.
 * Created by jivie on 10/14/15.
 */
@Deprecated("Deprecated along with ViewControllers in general.")
open class ContainerVC(
        val container: VCContainer,
        val disposeContainer: Boolean = true,
        val layoutParams: () -> FrameLayout.LayoutParams = { FrameLayout.LayoutParams(matchParent, matchParent) }
) : ViewController {

    private var embedders = HashMap<View, VCContainerEmbedder>()

    override fun make(vcContext: VCContext): View {
        val vcView = _FrameLayout(vcContext.context).apply {
            embedders[this] = (VCContainerEmbedder(vcContext, this, container, { layoutParams() }))
        }
        return vcView
    }

    override fun unmake(view: View) {
        embedders[view]?.unmake()
        embedders.remove(view)
        super.unmake(view)
    }

    override fun animateInComplete(vcContext: VCContext, view: View) {
        embedders[view]?.animateInComplete(vcContext, view)
        super.animateInComplete(vcContext, view)
    }

    override fun animateOutStart(vcContext: VCContext, view: View) {
        embedders[view]?.animateOutStart(vcContext, view)
        super.animateOutStart(vcContext, view)
    }

    override fun dispose() {
        if (disposeContainer) {
            container.close()
        }
        super.dispose()
    }

    override fun onBackPressed(backAction: () -> Unit) {
        container.onBackPressed(backAction)
    }

    override fun getTitle(resources: Resources): String {
        return container.getTitle(resources)
    }
}