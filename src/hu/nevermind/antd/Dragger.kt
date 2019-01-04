package hu.nevermind.antd

import org.w3c.files.File
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps


val DraggerComp: RClass<DraggerProps> = kotlinext.js.require("antd").Upload.Dragger

data class DefaultFileListItem(
        val uid: String,
        val name: String,
        val status: String,
        val url: String,
        val response: String? = null // custom error message to show
)

external interface DraggerProps : RProps {
    var name: String
    var multiple: Boolean
    var action: String
    var defaultFileList: Array<DefaultFileListItem>
    var onChange: (dynamic) -> Unit
    var onRemove: (File) -> Boolean
}

fun RBuilder.Dragger(handler: RHandler<DraggerProps> = {}) {
    DraggerComp {
        handler()
    }
}