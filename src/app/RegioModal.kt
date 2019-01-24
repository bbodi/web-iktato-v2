package hu.nevermind.utils.app

import app.AlvallalkozoScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.RegioOsszerendeles
import kotlinext.js.jsObject
import react.RBuilder
import react.ReactElement
import react.buildElement
import react.createElement
import react.dom.div


data class RegioModalParams(val editingRegio: RegioOsszerendeles,
                            val alvallalkozo: Alvallalkozo,
                            val onClose: (Boolean, RegioOsszerendeles?) -> Unit
)

object RegioModalComponent : DefinedReactComponent<RegioModalParams>() {
    override fun RBuilder.body(props: RegioModalParams) {
        val editingRegio: RegioOsszerendeles = props.editingRegio
        val (state, setState) = useState(editingRegio)
        Modal {
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
                this.asDynamic().id = AlvallalkozoScreenIds.modal.buttons.close
            }
            attrs.okButtonProps = jsObject {
                disabled = with(state) {
                    leiras.isEmpty()
                }
                this.asDynamic().id = AlvallalkozoScreenIds.modal.buttons.save
            }
            attrs.onOk = {
                props.onClose(true, state)
            }
            attrs.onCancel = {
                props.onClose(false, null)
            }
            Form {
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("Munkatípus")
                            Select {
                                attrs.value = state.munkatipus
                                attrs.onSelect = { value, option ->
                                    setState(state.copy(munkatipus = value))
                                }
                                Option { attrs.value = "Értékbecslés"; +"Értékbecslés" }
                                Option { attrs.value = "Energetika"; +"Energetika" }
                                Option { attrs.value = "Értékbecslés&Energetika"; +"Értékbecslés&Energetika" }
                            }
                        }
                    }
                    Col(offset = 2, span = 11) {
                        FormItem {
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("Leírás")
                            TextArea {
                                attrs.rows = 2
                                attrs.value = state.leiras
                                attrs.onChange = { e ->
                                    setState(
                                            state.copy(leiras = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("Nettó ár")
                            MyNumberInput {
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
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("Jutalék")
                            MyNumberInput {
                                attrs.number = state.jutalek.toLong()
                                attrs.addonAfter = StringOrReactElement.from { +"Ft" }
                                attrs.onValueChange = { value ->
                                    setState(state.copy(nettoAr = value?.toInt() ?: 0))
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("ÁFA")
                            MyNumberInput {
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