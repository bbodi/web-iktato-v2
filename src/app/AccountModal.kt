package hu.nevermind.utils.app

import app.AccountScreenIds
import app.AppState
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Account
import hu.nevermind.utils.store.Role
import kotlinext.js.jsObject
import react.ReactElement
import react.buildElement
import react.children
import react.createElement

private data class AccountModalState(val account: Account)

fun accountModal(
        editingAccount: Account,
        appState: AppState,
        onClose: (Boolean, Account?) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val editingAccount: Account = props.editingAccount
        val (modalState, setComponentState) = useState(AccountModalState(editingAccount))

        buildElement {
            Modal {
                attrs.visible = true
                attrs.title = StringOrReactElement.fromString("Felhasználó ${if (modalState.account.id == 0) "létrehozása" else "szerkesztése"}")
                attrs.cancelButtonProps = jsObject {
                    this.asDynamic().id = AccountScreenIds.modal.buttons.close
                }
                attrs.okButtonProps = jsObject {
                    disabled = with(modalState.account) {
                        alvallalkozoId == 0 || username.length < 3 || ((modalState.account.id == 0 || modalState.account.plainPassword.isNotEmpty()) && plainPassword.length < 3)
                    }
                    this.asDynamic().id = AccountScreenIds.modal.buttons.save
                }
                attrs.onOk = {
                    onClose(true, modalState.account)
                }
                attrs.onCancel = {
                    onClose(false, null)
                }
                Form {
                    Row {
                        Col(span = 11) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Felhasználónév")
                                attrs.validateStatus = if (modalState.account.username.length < 3) ValidateStatus.error else ValidateStatus.success
                                attrs.help = if (modalState.account.username.length < 3) StringOrReactElement.fromString("Hosszabbnak kell kell lennie 2 karakternél") else null
                                Input {
                                    attrs.value = modalState.account.username
                                    attrs.onChange = { e ->
                                        setComponentState(
                                                modalState.copy(account = modalState.account.copy(username = e.currentTarget.asDynamic().value))
                                        )
                                    }
                                }
                            }
                        }
                        Col(span = 11, offset = 2) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Teljes név")
                                Input {
                                    attrs.value = modalState.account.fullName
                                    attrs.onChange = { e ->
                                        setComponentState(
                                                modalState.copy(account = modalState.account.copy(fullName = e.currentTarget.asDynamic().value))
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
                                attrs.label = StringOrReactElement.fromString("Szerepkör")
                                Select {
                                    attrs.value = modalState.account.role.name
                                    attrs.onSelect = { value, option ->
                                        setComponentState(
                                                modalState.copy(account = modalState.account.copy(role = Role.valueOf(value as String)))
                                        )
                                    }
                                    Role.values().forEach { role ->
                                        Option { attrs.value = role.name; +(role.name) }
                                    }
                                }
                            }
                        }
                        Col(span = 11, offset = 2) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Alvállalkozó")
                                Select {
                                    attrs.showSearch = true
                                    attrs.value = modalState.account.alvallalkozoId
                                    attrs.notFoundContent = "Nincs találat"
                                    attrs.filterOption = { inputString, optionElement ->
                                        (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                                    }
                                    attrs.onSelect = { value: Int, option ->
                                        val av = appState.alvallalkozoState.alvallalkozok[value]
                                        setComponentState(
                                                modalState.copy(
//                                                        alvallalkozoName = selectedName,
                                                        account = modalState.account.copy(alvallalkozoId = av?.id ?: 0)
                                                )
                                        )
                                    }
                                    appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.forEach {
                                        Option { attrs.value = it.id; +it.name }
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Col(span = 11) {
                            FormItem {
                                attrs.required = true
                                attrs.label = StringOrReactElement.fromString("Jelszó")
                                attrs.validateStatus = if (modalState.account.id == 0 && modalState.account.plainPassword.length < 3) ValidateStatus.error else null
                                attrs.help = if ((modalState.account.id == 0 || modalState.account.plainPassword.isNotEmpty()) && modalState.account.plainPassword.length < 3) StringOrReactElement.fromString("Hosszabbnak kell kell lennie 2 karakternél") else null
                                Input {
                                    attrs.value = modalState.account.plainPassword
                                    attrs.onChange = { e ->
                                        setComponentState(
                                                modalState.copy(account = modalState.account.copy(plainPassword = e.currentTarget.asDynamic().value))
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
                                    attrs.checked = !modalState.account.disabled
                                    attrs.onChange = { checked ->
                                        setComponentState(
                                                modalState.copy(account = modalState.account.copy(disabled = !checked))
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
        this.editingAccount = editingAccount
    })
}