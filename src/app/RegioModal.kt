package hu.nevermind.utils.app

import app.AppState
import app.RegioScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.RegioOsszerendeles
import kotlinext.js.jsObject
import react.RBuilder
import react.dom.div


data class RegioModalParams(val editingRegio: RegioOsszerendeles,
                            val alvallalkozo: Alvallalkozo,
                            val appState: AppState,
                            val onClose: (Boolean, RegioOsszerendeles?) -> Unit
)

object RegioModalComponent : DefinedReactComponent<RegioModalParams>() {
    override fun RBuilder.body(props: RegioModalParams) {
        val editingRegio: RegioOsszerendeles = props.editingRegio
        val (state, setState) = useState(editingRegio)
        Modal {
            attrs.width = 620
            attrs.visible = true
            attrs.title = StringOrReactElement.from {
                div {
                    +"${props.alvallalkozo.name} "
                    Tag {
                        attrs.color = "blue"
                        +editingRegio.megye
                    }
                }
            }
            attrs.cancelButtonProps = jsObject {
                this.asDynamic().id = RegioScreenIds.modal.buttons.close
            }
            attrs.okButtonProps = jsObject {
                disabled = with(state) {
                    leiras.isEmpty() ||
                            munkatipus.isNullOrEmpty()
                }
                this.asDynamic().id = RegioScreenIds.modal.buttons.save
            }
            attrs.onOk = {
                props.onClose(true, state)
            }
            attrs.onCancel = {
                props.onClose(false, null)
            }
            Form {
                attrs.asDynamic().id = RegioScreenIds.modal.id
                Row {
                    Col(span = 9) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Munkatípus")
                            val munkatipusok = props.appState.sajatArState.sajatArak.values.map { it.munkatipus }.distinct()
                            Select {
                                attrs.asDynamic().id = RegioScreenIds.modal.inputs.munkatipus
                                attrs.value = state.munkatipus
                                attrs.onSelect = { value, option ->
                                    setState(state.copy(munkatipus = value, leiras = ""))
                                }
                                munkatipusok.forEach {
                                    Option { attrs.value = it; +it }
                                }
                            }
                        }
                    }
                    Col(offset = 2, span = 13) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Leírás")
                            Select {
                                attrs.asDynamic().id = RegioScreenIds.modal.inputs.leiras
                                attrs.asDynamic().style = jsStyle { minWidth = 300 }
                                attrs.value = state.leiras
                                attrs.onSelect = { value, option ->
                                    setState(state.copy(leiras = value))
                                }
                                props.appState.sajatArState.sajatArak.values.filter {
                                    it.munkatipus == state.munkatipus
                                }.forEach {
                                    Option { attrs.value = it.leiras; +it.leiras }
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Nettó ár")
                            MyNumberInput {
                                attrs.asDynamic().id = RegioScreenIds.modal.inputs.nettoAr
                                attrs.number = state.nettoAr.toLong()
                                attrs.addonAfter = StringOrReactElement.from { +"Ft" }
                                attrs.onValueChange = { value ->
                                    setState(state.copy(nettoAr = value?.toInt() ?: 0))
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Jutalék")
                            MyNumberInput {
                                attrs.asDynamic().id = RegioScreenIds.modal.inputs.jutalek
                                attrs.number = state.jutalek.toLong()
                                attrs.addonAfter = StringOrReactElement.from { +"Ft" }
                                attrs.onValueChange = { value ->
                                    setState(state.copy(jutalek = value?.toInt() ?: 0))
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("ÁFA")
                            MyNumberInput {
                                attrs.asDynamic().id = RegioScreenIds.modal.inputs.afa
                                attrs.number = state.afa.toLong()
                                attrs.addonAfter = StringOrReactElement.from { +"%" }
                                attrs.onValueChange = { value ->
                                    setState(state.copy(afa = minOf(100, maxOf(0, value?.toInt() ?: 0))))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}