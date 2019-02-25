package hu.nevermind.utils.app

import app.useState
import hu.nevermind.antd.*
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.communicator
import kotlinx.html.InputType
import react.RBuilder
import react.RElementBuilder


data class AlvallalkozoSajatAdataiModalParams(val alvallalkozo: Alvallalkozo,
                                              val visible: Boolean,
                                              val onClose: () -> Unit)

data class AlvallalkozoSajatAdataiModalState(val oldPass: String,
                                             val newPass: String,
                                             val newPass2: String)


object AlvallalkozoSajatAdataiModalComponent : DefinedReactComponent<AlvallalkozoSajatAdataiModalParams>() {
    override fun RBuilder.body(props: AlvallalkozoSajatAdataiModalParams) {
        val (modalState, setComponentState) = useState(AlvallalkozoSajatAdataiModalState(
                oldPass = "",
                newPass = "",
                newPass2 = ""
        ))

        Modal {
            attrs.width = 720
            attrs.visible = props.visible
            attrs.title = StringOrReactElement.fromString(props.alvallalkozo.name)
            attrs.footer = StringOrReactElement.from {
                Button {
                    attrs.onClick = {
                        props.onClose()
                    }
                    +"Bezár"
                }
            }
            attrs.onCancel = {
                props.onClose()
            }
            Collapse {
                attrs.bordered = false
                attrs.defaultActiveKey = arrayOf("cegadatok", "password")
                Panel("cegadatok") {
                    attrs.header = StringOrReactElement.fromString("Regisztrált cégadatok")
                    Form {
                        Row {
                            Col(span = 11) {
                                cegnevField(props.alvallalkozo)
                            }
                            Col(span = 11, offset = 2) {
                                cimField(props.alvallalkozo)
                            }
                        }
                        Row {
                            Col(span = 11) {
                                kapcsolattartoField(props.alvallalkozo)
                            }
                            Col(span = 11, offset = 2) {
                                adoSzamField(props.alvallalkozo)
                            }
                        }
                        Row {
                            Col(span = 11) {
                                szamlaszamField(props.alvallalkozo)
                            }
                            Col(span = 11, offset = 2) {
                                emailField(props.alvallalkozo)
                            }
                        }
                        Row {
                            Col(span = 11) {
                                telefonField(props.alvallalkozo)
                            }
                            Col(span = 11, offset = 2) {
                                tagsagiSzamField(props.alvallalkozo)
                            }
                        }
                    }
                }
                Panel("password") {
                    attrs.header = StringOrReactElement.fromString("Jelszó megváltoztatása")
                    Form {
                        Row {
                            Col(span = 11) {
                                FormItem {
                                    attrs.required = true
                                    attrs.validateStatus = if (modalState.oldPass.length.let { it > 0 && it < 4 }) ValidateStatus.error else null
                                    attrs.label = StringOrReactElement.fromString("Régi jelszó")
                                    Input {
                                        attrs.value = modalState.oldPass
                                        attrs.type = InputType.password
                                        attrs.onChange = { e ->
                                            setComponentState(
                                                    modalState.copy(oldPass = e.currentTarget.asDynamic().value)
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
                                    attrs.label = StringOrReactElement.fromString("Új jelszó")
                                    attrs.validateStatus = if (modalState.newPass.length.let { it > 0 && it < 4 }) ValidateStatus.error else null
                                    Input {
                                        attrs.value = modalState.newPass
                                        attrs.type = InputType.password
                                        attrs.onChange = { e ->
                                            setComponentState(
                                                    modalState.copy(newPass = e.currentTarget.asDynamic().value)
                                            )
                                        }
                                    }
                                }
                            }
                            Col(span = 11, offset = 2) {
                                FormItem {
                                    attrs.required = true
                                    attrs.label = StringOrReactElement.fromString("Új jelszó még egyszer")
                                    attrs.validateStatus = if (modalState.newPass2.let {
                                                it.length > 0 &&
                                                        (it.length < 4 ||
                                                        it != modalState.newPass)
                                            })
                                        ValidateStatus.error
                                    else
                                        null
                                    Input {
                                        attrs.value = modalState.newPass2
                                        attrs.type = InputType.password
                                        attrs.onChange = { e ->
                                            setComponentState(
                                                    modalState.copy(newPass2 = e.currentTarget.asDynamic().value)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Row {
                            Col(span = 11) {
                                Button {
                                    attrs.type = ButtonType.primary
                                    attrs.disabled = modalState.oldPass.length < 4 ||
                                            modalState.newPass.length < 4 ||
                                            modalState.newPass2.length < 4 ||
                                            modalState.newPass != modalState.newPass2
                                    attrs.onClick = {
                                        communicator.post<dynamic>(RestUrl.changePassword, object {
                                            val oldPass = modalState.oldPass
                                            val newPass = modalState.newPass
                                        }) { response ->
                                            message.success("Jelszó megváltoztatva")
                                            setComponentState(modalState.copy(
                                                    oldPass = "",
                                                    newPass = "",
                                                    newPass2 = ""
                                            ))
                                        }
                                    }
                                    +"Jelszó megváltoztatása"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.tagsagiSzamField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Tagsági szám")
            Input {
                attrs.value = modalState.tagsagiSzam
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.cimField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Cím")
            Input {
                attrs.value = modalState.cim
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.emailField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Email")
            Input {
                attrs.disabled = true
                attrs.value = modalState.email
            }
        }
    }

    private fun RElementBuilder<ColProps>.kapcsolattartoField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Kapcsolattartó")
            Input {
                attrs.value = modalState.kapcsolatTarto
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.adoSzamField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Adószám")
            Input {
                attrs.value = modalState.adoszam
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.szamlaszamField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Bankszámlaszám")
            Input {
                attrs.value = modalState.szamlaSzam
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.telefonField(modalState: Alvallalkozo) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Telefon")
            Input {
                attrs.value = modalState.phone
                attrs.disabled = true
            }
        }
    }

    private fun RElementBuilder<ColProps>.cegnevField(modalState: Alvallalkozo) {
        FormItem {
            attrs.required = true
            attrs.label = StringOrReactElement.fromString("Cégnév")
            Input {
                attrs.value = modalState.name
                attrs.disabled = true
            }
        }
    }
}
