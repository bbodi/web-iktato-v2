package app

import hu.nevermind.utils.hu.nevermind.antd.Menu.Menu as MenuComponent


private object API {
    val useState: (dynamic) -> Array<dynamic> = kotlinext.js.require("react").useState
    val useReducer = kotlinext.js.require("react").useReducer
    val useEffect: (() -> Any, Array<out Any>?) -> Unit = kotlinext.js.require("react").useEffect
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

fun <S> useState(initialStateFunc: () -> S): Pair<S, Dispatcher<S>> {
    val resultArray = API.useState(initialStateFunc)
    return Pair(resultArray[0], resultArray[1])
}

typealias Dispatcher<S> = (S) -> Unit

val RUN_ONLY_WHEN_MOUNT = emptyArray<Any>()

fun useEffect(changeSet: Array<Any>? = null, body: () -> Unit) {
    API.useEffect({
        body()
        js("return;") // so it will return nothing
    }, changeSet)
}

fun useEffectWithCleanup(changeSet: Array<Any>? = null, body: () -> (() -> Unit)) {
    API.useEffect({
        body()
    }, changeSet)
}