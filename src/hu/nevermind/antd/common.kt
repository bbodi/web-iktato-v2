package hu.nevermind.utils.hu.nevermind.antd

import react.RBuilder
import react.ReactElement
import react.buildElement
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


typealias Key = String

val jsUndefined: dynamic = js("undefined")


//typealias StringOrReactElement = Any

object StringOrReactElement {
    fun fromReactElement(element: ReactElement): StringOrReactElement {
        return element.asDynamic()
    }
    fun fromString(element: String): StringOrReactElement {
        return element.asDynamic()
    }

    fun from(builder: RBuilder.() -> Unit): StringOrReactElement {
        return buildElement { builder() }.asDynamic()
    }
}
