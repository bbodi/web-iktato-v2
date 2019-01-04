package hu.nevermind.utils.app

import app.AlvallalkozoScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.RegioOsszerendeles
import kotlinext.js.jsObject
import react.ReactElement
import react.buildElement
import react.createElement
import react.dom.div


fun regioModal(
        editingRegio: RegioOsszerendeles,
        alvallalkozo: Alvallalkozo,
        onClose: (Boolean, RegioOsszerendeles?) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val editingRegio: RegioOsszerendeles = props.editingRegio
        val (state, setState) = useState(editingRegio)

        buildElement {
            Modal {
                attrs.visible = true
                attrs.title = StringOrReactElement.from {
                    div {
                        +"${alvallalkozo.name} "
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
                    onClose(true, state)
                }
                attrs.onCancel = {
                    onClose(false, null)
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
    }, props = jsObject<dynamic> {
        this.editingRegio = editingRegio
    })
}