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
        val mainVC = MainVC() //this is where you create your root view controller
    }

    override val viewController: ViewController
        get() = mainVC
}
```

## Example View Controller

```kotlin
class MainVC
