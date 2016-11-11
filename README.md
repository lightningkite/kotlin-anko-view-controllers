# kotlin-anko-view-controllers

This package is Android-specific, and therefore is not cross-platform.

This package greatly simplifies creating different screens in an app by providing an alternative to `Activity` called `ViewController`, which has the following differences:

- Lightweight - ViewController is simply an interface with one primary function - `make(activity:VCActivity):View`, which builds the views and returns the heirarchy.
- Data retention - ViewControllers are built to hold information that should be displayed between view recreations by being stored statically.  This completely removes the need for storing things using `Bundle` and `Parcelable`, which are both time consuming to implement and reduce type safety.
- Data passing - ViewControllers are built to allow easy sending of information between different views, allowing you to pass information through the constructor.  This completely removes the need for passing things using `Bundle` and `Parcelable`, which are both time consuming to implement and reduce type safety.

## Example Top-Level Activity

```kotlin
class MainActivity : VCActivity() {

    companion object {
         // This creates a new stack of view controllers.
        val stack = VCStack().apply{
            //Push an initial view controller onto the stack.
            //This view controller is defined below.
            push(StackDemoVC(this))
        }
        
        //ContainerVC is a view controller that displays a container of other view controllers.
        //The most important type of container (and most common) is VCStack.
        val mainVC = ContainerVC(stack) 
    }

    override val viewController: ViewController
        get() = mainVC
}
```

## Example View Controller

```kotlin
//AnkoViewController is a view controller whose view is created using Anko.  This is the most common class to extend from.
//Notice how we are passing in information through our constructor.
class StackDemoVC(val stack: VCStack, val depth: Int = 1) : AnkoViewController() {

    //This function simply defines a title for this view.  You can make other things use this later.
    override fun getTitle(resources: Resources): String {
        return "Stack Demo ($depth)"
    }

    //This function actually creates the view.
    override fun createView(ui: AnkoContext<VCActivity>): View = ui.verticalLayout {
        gravity = Gravity.CENTER

        textView("This view controller has a depth of $depth.") {
            //I typically make a separate file called "Style.kt" which defines extension functions for each view
            //named things like "styleDefault" to handle styling.
            styleDefault()
        }

        button("Go deeper") {
            styleDefault()
            onClick {
                //This pushes a new view controller onto the stack.
                stack.push(StackDemoVC(stack, depth + 1))
            }
        }

        button("Go back") {
            styleDefault()
            onClick {
                //This removes a view controller from the stack, going back to the previous view controller.
                stack.pop()
            }
        }
    }
}
