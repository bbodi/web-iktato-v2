package hu.nevermind.utils.app

import react.RBuilder
import react.ReactElement
import react.buildElement

abstract class DefinedReactComponent<P> {

    abstract fun RBuilder.body(props: P): Unit

    val reactElementFunction = { props: dynamic ->
        buildElement {
            body(props)
        }
    }

    fun createElement(props: P): ReactElement {
        reactElementFunction.asDynamic().displayName = this@DefinedReactComponent::class.simpleName
        return react.createElement(
                type = reactElementFunction,
                props = props.asDynamic() /*because P does not extend RProps*/
        )
    }

    val insert: RBuilder.(P) -> Unit = { props ->
        reactElementFunction.asDynamic().displayName = this@DefinedReactComponent::class.simpleName
        val reactElement = react.createElement(
                type = reactElementFunction,
                props = props.asDynamic() /*because P does not extend RProps*/
        )
        child(reactElement)
    }
}
