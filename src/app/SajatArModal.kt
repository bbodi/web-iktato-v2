package hu.nevermind.utils.app

import app.SajatArScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import app.AppState
import hu.nevermind.utils.store.SajatAr
import kotlinext.js.jsObject
import react.ReactElement
import react.buildElement
import react.children
import react.createElement


fun sajatArModal(
        editingSajatAr: SajatAr,
        appState: AppState,
        onClose: (Boolean, SajatAr?) -> Unit): ReactElement {
    return createElement({ props: dynamic ->
        val (sajatAr, setComponentState) = useState(props.editingSajatAr as SajatAr)

        buildElement {
            Modal {
                attrs.visible = true
                attrs.title = StringOrReactElement.fromString("Saját ár ${if (sajatAr.id == 0) "létrehozása" else "szerkesztése"}")
                attrs.cancelButtonProps = jsObject {
                    this.asDynamic().id = SajatArScreenIds.modal.button.close
                }
                attrs.okButtonProps = jsObject {
                    disabled = with(sajatAr) {
                        megrendelo.isEmpty() || munkatipus.isEmpty() || leiras.isEmpty()
                    }
                    this.asDynamic().id = SajatArScreenIds.modal.button.save
                }
                attrs.onOk = {
                    onClose(true, sajatAr)
                }
                attrs.onCancel = {
                    onClose(false, null)
                }
                Form {
                    attrs.asDynamic().id = SajatArScreenIds.modal.id
                    Row {
                        Col(span = 11) {
                            FormItem {
                                attrs.required = true
                                attrs.help = StringOrReactElement.fromString("Meglévő vagy új Megrendelő")
                                attrs.label = StringOrReactElement.fromString("Megrendelő")
                                AutoComplete(appState.sajatArState.allMegrendelo) {
                                    attrs.asDynamic().id = SajatArScreenIds.modal.input.megrendelo
                                    attrs.value = sajatAr.megrendelo
                                    attrs.placeholder = "Megrendelő"
                                    attrs.filterOption = { inputString, optionElement ->
                                        (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                                    }
                                    attrs.onChange = { selectedName ->
                                        setComponentState(sajatAr.copy(megrendelo = selectedName))
                                    }
                                }
                            }
                        }
                        Col(span = 11, offset = 2) {
                            FormItem {
                                attrs.required = true
                                attrs.help = StringOrReactElement.fromString("Meglévő vagy új Munkatípus")
                                attrs.label = StringOrReactElement.fromString("Munkatípus")
                                AutoComplete(appState.sajatArState.getMunkatipusokForMegrendelo(sajatAr.megrendelo).toTypedArray()) {
                                    attrs.asDynamic().id = SajatArScreenIds.modal.input.munkatipus
                                    attrs.value = sajatAr.munkatipus
                                    attrs.placeholder = "Munkatípus"
                                    attrs.filterOption = { inputString, optionElement ->
                                        (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                                    }
                                    attrs.onChange = { selectedName ->
                                        setComponentState(sajatAr.copy(munkatipus = selectedName))
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Col(span = 24) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Leírás")
                                Input {
                                    attrs.asDynamic().id = SajatArScreenIds.modal.input.leiras
                                    attrs.value = sajatAr.leiras
                                    attrs.onChange = { e -> setComponentState(sajatAr.copy(leiras = e.currentTarget.asDynamic().value)) }
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
                                    attrs.asDynamic().id = SajatArScreenIds.modal.input.nettoAr
                                    attrs.number = sajatAr.nettoAr.toLong()
                                    attrs.addonAfter = StringOrReactElement.from { +"Ft" }
                                    attrs.onValueChange = { value ->
                                        setComponentState(sajatAr.copy(nettoAr = value?.toInt() ?: 0))
                                    }
                                }
                            }
                        }
                        Col(span = 11, offset = 2) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("ÁFA")
                                MyNumberInput {
                                    attrs.asDynamic().id = SajatArScreenIds.modal.input.afa
                                    attrs.number = sajatAr.afa.toLong()
                                    attrs.addonAfter = StringOrReactElement.from { +"%" }
                                    attrs.onValueChange = { value ->
                                        setComponentState(sajatAr.copy(afa = minOf(100, maxOf(0, value?.toInt() ?: 0))))
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }, jsObject<dynamic> {
        this.editingSajatAr = editingSajatAr
    })
}