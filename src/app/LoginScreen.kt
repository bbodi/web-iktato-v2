package app

import hu.nevermind.antd.*
import hu.nevermind.iktato.JqueryAjaxPoster
import hu.nevermind.iktato.Result
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import kotlinx.html.InputType
import react.RBuilder
import react.dom.a
import react.dom.br
import react.dom.jsStyle
import store.Action

private data class LoginScreenState(val username: String,
                                    val password: String,
                                    val rememberMe: Boolean)


data class LoginScreenParams(val appState: AppState,
                             val globalDispatch: (Action) -> Unit)

object LoginScreenComponent : DefinedReactComponent<LoginScreenParams>() {
    override fun RBuilder.body(props: LoginScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val (state, setState) = useState(LoginScreenState(
                username = "",
                password = "",
                rememberMe = true
        ))

        Row {
            Col(offset = 6, span = 10) {
                Form {
                    attrs.onSubmit = { e ->
                        e.preventDefault()
                        val data = object {
                            val username = state.username
                            val password = state.password
                        }

                        JqueryAjaxPoster().ajaxPost(
                                contentType = "application/x-www-form-urlencoded; charset=UTF-8",
                                url = "/login",
                                data = data,
                                type = "POST",
                                async = false) { result: Result<Any, String> ->
                            result.ifOk { response ->
                            }
                            result.ifError { response ->
                                globalDispatch(Action.SetLoggedInUser(null))
                            }
                        }
                    }
                    FormItem {
                        attrs.required = true
                        Input {
                            attrs.prefix = StringOrReactElement.from {
                                Icon("user") {
                                    attrs.style = jsStyle { color = "rgba(0,0,0,.25)" }
                                }
                            }
                            attrs.placeholder = "Felhasználónév"
                            attrs.value = state.username
                            attrs.onChange = { e ->
                                setState(state.copy(
                                        username = e.currentTarget.asDynamic().value
                                ))
                            }
                        }
                    }
                    FormItem {
                        attrs.required = true
                        Input {
                            attrs.type = InputType.password.realValue
                            attrs.prefix = StringOrReactElement.from {
                                Icon("lock") {
                                    attrs.style = jsStyle { color = "rgba(0,0,0,.25)" }
                                }
                            }
                            attrs.placeholder = "Jelszó"
                            attrs.value = state.password
                            attrs.onChange = { e ->
                                setState(state.copy(
                                        password = e.currentTarget.asDynamic().value
                                ))
                            }
                        }
                    }
                    FormItem {
                        Checkbox {
                            attrs.checked = state.rememberMe
                            attrs.onChange = { checked ->
                                setState(state.copy(
                                        rememberMe = checked
                                ))
                            }
                            +"Emlékezz rám"
                        }
                        a(href = "", classes = "login-form-forgot") {
                            attrs.jsStyle = jsStyle { float = "right" }
                            +"Elfelejtettem a jelszavam"
                        }
                        br { }
                        Button {
                            attrs.disabled = state.username.isEmpty() || state.password.isEmpty()
                            attrs.type = ButtonType.primary
                            attrs.block = true
                            attrs.htmlType = "submit"
                            Icon("login")
                            +"Bejelentkezés"
                        }
                    }
                }

            }
        }
    }
}