package hu.nevermind.utils.app

import app.AlvallalkozoScreenIds
import app.AppState
import app.useState
import hu.nevermind.antd.Checkbox
import hu.nevermind.antd.Col
import hu.nevermind.antd.Form
import hu.nevermind.antd.FormItem
import hu.nevermind.antd.Input
import hu.nevermind.antd.Modal
import hu.nevermind.antd.Row
import hu.nevermind.antd.Switch
import hu.nevermind.antd.TextArea
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.Ertekbecslo
import kotlinext.js.jsObject
import react.RBuilder
import react.ReactElement
import react.buildElement
import react.createElement


data class AlvallalkozoModalParams(val editingAlvallalkozo: Alvallalkozo,
                                  val appState: AppState,
                                  val onClose: (Boolean, Alvallalkozo?) -> Unit)


object AlvallalkozoModalComponent : DefinedReactComponent<AlvallalkozoModalParams>() {
    override fun RBuilder.body(props: AlvallalkozoModalParams) {
        val (modalState, setComponentState) = useState(props.editingAlvallalkozo)

        Modal {
            attrs.width = 720
            attrs.visible = true
            attrs.title = StringOrReactElement.fromString("Alvállalkozó ${if (modalState.id == 0) "létrehozása" else "szerkesztése"}")
            attrs.cancelButtonProps = jsObject {
                this.asDynamic().id = AlvallalkozoScreenIds.modal.buttons.close
            }
            attrs.okButtonProps = jsObject {
                disabled = with(modalState) {
                    name.isEmpty()
                }
                this.asDynamic().id = AlvallalkozoScreenIds.modal.buttons.save
            }
            attrs.onOk = {
                props.onClose(true, modalState)
            }
            attrs.onCancel = {
                props.onClose(false, null)
            }
            Form {
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.required = true
                            attrs.label = StringOrReactElement.fromString("Cégnév")
                            TextArea {
                                attrs.value = modalState.name
                                attrs.rows = 2
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(name = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Telefon")
                            TextArea {
                                attrs.value = modalState.phone
                                attrs.rows = 2
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(phone = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Számlaszám")
                            Input {
                                attrs.value = modalState.szamlaSzam
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(szamlaSzam = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Adószám")
                            Input {
                                attrs.value = modalState.adoszam
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(adoszam = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Kapcsolattartó")
                            Input {
                                attrs.value = modalState.kapcsolatTarto
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(kapcsolatTarto = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Email")
                            Input {
                                attrs.value = modalState.email
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(email = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Cím")
                            TextArea {
                                attrs.value = modalState.cim
                                attrs.rows = 2
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(cim = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Tagsági szám")
                            Input {
                                attrs.value = modalState.tagsagiSzam
                                attrs.onChange = { e ->
                                    setComponentState(
                                            modalState.copy(tagsagiSzam = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Készpénzes")
                            Checkbox {
                                attrs.checked = modalState.keszpenzes
                                attrs.onChange = { checked ->
                                    setComponentState(
                                            modalState.copy(keszpenzes = checked)
                                    )
                                }
                            }
                        }
                    }
                    Col(span = 11, offset = 2) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Állapot")
                            Switch {
                                attrs.checkedChildren = StringOrReactElement.fromString("Engedélyezve")
                                attrs.unCheckedChildren = StringOrReactElement.fromString("Tiltva")
                                attrs.checked = !modalState.disabled
                                attrs.onChange = { checked ->
                                    setComponentState(
                                            modalState.copy(disabled = !checked)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
