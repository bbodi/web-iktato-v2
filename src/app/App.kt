package app

import hu.nevermind.antd.*
import hu.nevermind.utils.demo.ButtonDemoPage
import hu.nevermind.utils.demo.layoutDemoPage
import hu.nevermind.utils.hu.nevermind.antd.*
import hu.nevermind.utils.jsStyle
import kotlinx.html.id
import logo.logo
import react.*
import react.dom.*
import ticker.ticker
import kotlin.browser.window
import hu.nevermind.utils.hu.nevermind.antd.Menu.Menu as MenuComponent


private object API {
    val useState: (dynamic) -> Array<dynamic> = kotlinext.js.require("react").useState
    val useReducer = kotlinext.js.require("react").useReducer
    val useEffect: (() -> Unit, Array<out Any>?) -> Unit = kotlinext.js.require("react").useEffect
    val useRef: () -> UseRef<dynamic> = kotlinext.js.require("react").useRef
}

external interface Reducer
external interface Dispatch

external interface UseRef<T> {
    var current: T
}
fun <T> useRef(): UseRef<T> = API.useRef()

fun <S, A> useReducer(reducerFunc: (S, A) -> S, state: S): Pair<S, Dispatcher<A>> {
    val resultArray = API.useReducer(reducerFunc, state)
    return Pair(resultArray[0], resultArray[1])
}

fun <S> useState(initialState: S): Pair<S, Dispatcher<S>> {
    val resultArray = API.useState(initialState)
    return Pair(resultArray[0], resultArray[1])
}

typealias Dispatcher<S> = (S) -> Unit

val RUN_ONLY_WHEN_MOUNT = emptyArray<Any>()

fun useEffect(changeSet: Array<Any>? = null, body: () -> Unit) {
    API.useEffect({
        body()
        js("return null;")
    }, changeSet)
}

fun useEffectWithCleanup(changeSet: Array<Any>? = null, body: () -> (() -> Unit)) {
    API.useEffect({
        body()
    }, changeSet)
}


interface AppComponentState : RState {
    var path: String
}

class App : RComponent<RProps, AppComponentState>() {

    override fun AppComponentState.init() {
        path = window.location.hash.drop(1)
    }

    private val onMenuChange: (OnClickEventData) -> Unit = { event ->
        val (menuItem, submenu) = event.keyPath
        setState {
            path = "$submenu/$menuItem"
            window.location.hash = "#$path";
        }
    }

    override fun RBuilder.render() {
        val (_, selectedSubMenu) = if (state.path.contains("/")) state.path.split("/") else listOf("", "")
        Layout {
            Header {
                this.apply {
                    //                    Example()
                }
            }
            Content {
                attrs.style = jsStyle { padding = "0 50px" }
                Layout(style = jsStyle { padding = "0 50px" }) {
                    Sider {
                        attrs.width = 200
                        attrs.style = jsStyle { background = "#fff" }
                        Menu {
                            attrs.mode = MenuMode.inline
                            attrs.defaultSelectedKeys = arrayOf(selectedSubMenu)
                            attrs.defaultOpenKeys = arrayOf("components")
                            attrs.onClick = onMenuChange
                            attrs.style = jsStyle { height = "100%" }
                            SubMenu("components") {
                                attrs.title = StringOrReactElement.from { span { +"Components" } }
                                MenuItemGroup(key = "general") {
                                    attrs.title = StringOrReactElement.fromString("General")
                                    MenuItem("button") {
                                        +"Button"
                                    }
                                    MenuItem("icon") {
                                        +"Icon"
                                    }
                                }
                                MenuItemGroup(key = "layout") {
                                    attrs.title = StringOrReactElement.fromString("Layout")
                                    MenuItem("grid") {
                                        +"Grid"
                                    }
                                    MenuItem("layout") {
                                        +"Layout"
                                    }
                                }
                            }
                        }
                    }
                    Content {
                        attrs.style = jsStyle { padding = "0 24px"; minHeight = 280 }
                        when (state.path) {
                            "components/button" -> child(ButtonDemoPage::class) {}
                            "components/icon" -> child(ButtonDemoPage::class) {}
                            "components/grid" -> child(ButtonDemoPage::class) {}
                            "components/layout" -> layoutDemoPage()
                        }
                    }
                }
            }
            Footer {
                +"KAnt Design ©2018 Created by Balázs Bódi"
            }
        }
        header(classes = "") {
            attrs.id = "header"
//            Row {
//
//            }
        }
        div("App-header") {
            logo()
            h2 {
                +"Welcome to React with Kotlin"
            }
        }
        p("App-intro") {
            +"To get started, edit "
            code { +"app/App.kt" }
            +" and save to reload."
        }
        div("App-ticker") {
            ticker()
        }
    }
}

fun RBuilder.app() = child(App::class) {}
