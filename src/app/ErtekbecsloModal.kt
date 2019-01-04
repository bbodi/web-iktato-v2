package hu.nevermind.utils.app

import app.AlvallalkozoScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Ertekbecslo
import kotlinext.js.jsObject
import react.ReactElement
import react.buildElement
import react.createElement


fun ertekbecsloModal(
        editingErtekbecslo: Ertekbecslo,
        onClose: (Boolean, Ertekbecslo?) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val editingErtekbecslo: Ertekbecslo = props.editingErtekbecslo
        val (modalState, setComponentState) = useState(editingErtekbecslo)

        buildElement {
            Modal {
                attrs.visible = true
                attrs.title = StringOrReactElement.fromString("Értékbecslő ${if (modalState.id == 0) "létrehozása" else "szerkesztése"}")
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
                    onClose(true, modalState)
                }
                attrs.onCancel = {
                    onClose(false, null)
                }
                Form {
                    Row {
                        Col(span = 24) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Név")
                                Input {
                                    attrs.value = modalState.name
                                    attrs.onChange = { e ->
                                        setComponentState(
                                                modalState.copy(name = e.currentTarget.asDynamic().value)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Col(span = 24) {
                            FormItem {
                                attrs.label = StringOrReactElement.fromString("Telefon")
                                Input {
                                    attrs.value = modalState.phone
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
                        Col(span = 24) {
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
                        Col(span = 24) {
                            FormItem {
                                attrs.label = StringOrReactElement.fromString("Megjegyzés")
                                TextArea {
                                    attrs.value = modalState.comment
                                    attrs.rows = 3
                                    attrs.onChange = { e ->
                                        setComponentState(
                                                modalState.copy(comment = e.currentTarget.asDynamic().value)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Col(span = 24) {
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
    }, props = jsObject<dynamic> {
        this.editingErtekbecslo = editingErtekbecslo
    })
}