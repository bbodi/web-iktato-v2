package hu.nevermind.utils.hu.nevermind.antd

import hu.nevermind.antd.SliderProps
import react.RClass

private val _message: Message = kotlinext.js.require("antd").message

val message: Message = _message
external class Message {
    fun success(str: String)
    fun warning(str: String)
    fun error(str: String)
}