package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import kotlinext.js.jsObject
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val ModalComp: RClass<ModalProps> = kotlinext.js.require("antd").Modal
private val confirm: dynamic = kotlinext.js.require("antd").Modal.confirm
private val info: dynamic = kotlinext.js.require("antd").Modal.info
private val modalError: dynamic = kotlinext.js.require("antd").Modal.error


external interface ModalProps : RProps {
    var visible: Boolean
    var width: Int
    var title: StringOrReactElement?
    var okText: String
    var okType: ButtonType
    var cancelText: String
    var okButtonProps: ButtonProps
    var cancelButtonProps: ButtonProps
    var centered: Boolean
    var closable: Boolean
    var confirmLoading: Boolean
    var destroyOnClose: Boolean
    var mask: Boolean
    var maskClosable: Boolean
    var footer: StringOrReactElement?
    var onOk: () -> Unit
    var onCancel: () -> Unit
    var afterClose: () -> Unit
}

external interface ModalConfirmOption {
    var title: String
    var content: StringOrReactElement
    var onCancel: () -> Unit
    var okText: String
    var cancelText: String
    var okType: ButtonType
    var onOk: () -> Unit?
}

object Modal {
    fun confim(body: ModalConfirmOption.()->Unit) {
        confirm(jsObject<ModalConfirmOption> { this.body() })
    }

    fun info(body: ModalConfirmOption.()->Unit) {
        info(jsObject<ModalConfirmOption> { this.body() })
    }

    fun error(body: ModalConfirmOption.()->Unit) {
        modalError(jsObject<ModalConfirmOption> { this.body() })
    }
}

fun RBuilder.Modal(
        handler: RHandler<ModalProps> = {}
) {
    ModalComp {
        handler()
    }
}
