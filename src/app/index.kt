package app

import app.common.moment
import app.megrendeles.megrendelesScreen
import hu.nevermind.antd.*
import hu.nevermind.iktato.JqueryAjaxPoster
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.iktato.Result
import hu.nevermind.iktato.component.megrendeles.megrendelesForm
import hu.nevermind.utils.hu.nevermind.antd.*
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.require
import kotlinext.js.requireAll
import kotlinx.html.InputType
import react.RProps
import react.ReactElement
import react.buildElement
import react.dom.div
import react.dom.p
import store.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.roundToLong

data class AppState(val megrendelesState: MegrendelesState,
                    val alvallalkozoState: AlvallalkozoState,
                    val currentScreen: AppScreen,
                    val urlData: UrlData,
                    val sajatArState: SajatArState,
                    val accountStore: AccountStore,
                    val maybeLoggedInUser: LoggedInUser?)

//val rootReducer = combineReducers<AppState, RAction>(mapOf(
//        "megrendelesState" to megrendelesReducer
//))
//
////val store = createStore(megrendelesReducer, emptyArray(), rEnhancer())
//val store = createStore(rootReducer,
//        AppState(
//                MegrendelesState(emptyMap())),
//        rEnhancer()
//)

interface IdProps : RProps {
    var id: Int
}

fun main(args: Array<String>) {
    // HACK: operator invoke is translated as `invoke()` function call in Javascript -_-'
    moment.asDynamic().invoke = moment
    requireAll(require.context("src", true, js("/\\.css$/")))

    val data = object {
        val username = "admin"
        val password = "admin"
    }
//        Jq.blockUI(object {val baseZ = 2000})

    fun appReducer(state: LoggedInUser?, action: Action): LoggedInUser? {
        return when (action) {
            is Action.MegrendelesekFromServer -> state
            is Action.SetLoggedInUser -> action.data
            is Action.ChangeURL -> state
            is Action.changeURLSilently -> state
            is Action.FilterMegrendelesek -> state
            is Action.SajatArFromServer -> state
            is Action.AccountFromServer -> state
            is Action.AlvallalkozoFromServer -> state
            is Action.ErtekbecsloFromServer -> state
            is Action.RegioOsszerendelesFromServer -> state
            is Action.DeleteRegioOsszerendeles -> state
        }
    }

    val AppComponent: () -> ReactElement? = {
        val (appState, dispatch) = useReducer<AppState, Action>({ currentState, action ->
            console.log("Message arrived: $action")
            currentState.copy(
                    megrendelesState = megrendelesekFromServer(currentState.megrendelesState, action),
                    alvallalkozoState = alvallalkozoActionHandler(currentState.alvallalkozoState, action),
                    urlData = routerStoreHandler(currentState.urlData, action),
                    maybeLoggedInUser = appReducer(currentState.maybeLoggedInUser, action),
                    sajatArState = sajatArActionHandler(currentState.sajatArState, action),
                    accountStore = accountActionHandler(currentState.accountStore, action)
            )
        }, AppState(MegrendelesState(emptyMap()), AlvallalkozoState(),
                maybeLoggedInUser = null,
                currentScreen = AppScreen.LoginAppScreen(),
                urlData = UrlData(window.location.hash.substring(1), emptyMap()),
                sajatArState = SajatArState(),
                accountStore = AccountStore(emptyArray()))) // TODO: parse current uRL
        useEffect {
            var urlChangeOccurredByDispatchAction = false
            window.addEventListener("hashchange", {
                val externalChange = urlChangeOccurredByDispatchAction == false
                if (externalChange) {
//                    RouterStore.path = window.location.hash.substring(1)
                    // appState.copy(urlData = appState.urlData.copy(path = window.location.hash.substring(1)))
//                    dispatch(Action.ChangeURL(window.location.hash.substring(1)))
                }
                urlChangeOccurredByDispatchAction = false
            }, false)
        }

        useEffect(emptyArray<Any>()) {
            JqueryAjaxPoster().ajaxPost(
                    contentType = "application/x-www-form-urlencoded; charset=UTF-8",
                    url = "/login",
                    data = data,
                    type = "POST",
                    async = false) { result: Result<Any, String> ->
                result.ifOk { response ->

                }
                result.ifError { response ->
                    communicator.authenticate { authResult ->
                        authResult.ifOk { response ->
                            val principal = response.asDynamic()
                            dispatch(Action.SetLoggedInUser(LoggedInUser(
                                    principal.username,
                                    principal.fullName,
                                    principal.alvallalkozoId,
                                    Role.valueOf(principal.role),
                                    stringToColumnDefArray(principal.megrendelesTableColumns)
                            )))
                        }
                    }

                }
            }
        }

        useEffect(RUN_ONLY_WHEN_MOUNT) {
            communicator.getEntitiesFromServer(RestUrl.getMegrendelesFromServer) { returnedArray: Array<dynamic> ->
                dispatch(Action.MegrendelesekFromServer(returnedArray))
            }
        }

        val (inputNum, numDispatch) = useState(0L)
        buildElement {
            div {
                InputNumber {
                    attrs.decimalSeparator = " "
                    attrs.onChange = { value -> numDispatch(value.toLong()) }
                }
                Input {
                    attrs.type = InputType.number
                    attrs.addonAfter = StringOrReactElement.from {
                        +"+ ÁFA(27%) = ${(inputNum * 1.27).roundToLong()}"
                    }
                    attrs.onChange = { e -> numDispatch(e.currentTarget.asDynamic().value) }
                }
                MyNumberInput {
                    attrs.number = inputNum
                    attrs.addonAfter = StringOrReactElement.from {
                        +"+ ÁFA(27%) = ${(inputNum * 1.27).roundToLong()}"
                    }
                    attrs.onValueChange = { value -> numDispatch(value ?: 0) }
                }
                div {
                    Steps {
                        attrs.current = 2
                        val akadalyos = true
                        attrs.status = if (akadalyos) StepsStatus.error else StepsStatus.wait
                        attrs.size = "small"
                        // TODO: use icons
                        Step {
                            attrs.title = StringOrReactElement.fromString("Átvett")
                            attrs.description = ""
                        }
                        Step {
                            attrs.title = StringOrReactElement.fromString("Szemle")
                            attrs.description = ""
                        }
                        Step {
                            attrs.title = StringOrReactElement.fromString("Utalás")
                            attrs.description = if (akadalyos) "Akadályos" else null
                        }
                        Step {
                            attrs.title = StringOrReactElement.fromString("Ellenőrizve")
                            attrs.description = ""
                        }
                        Step {
                            attrs.title = StringOrReactElement.fromString("Archiválva")
                            attrs.description = ""
                        }
                    }
                }
                div {
                    Dragger {
                        attrs.name = "file"
                        attrs.multiple = true
                        attrs.onRemove = { file ->
                            message.success("${file.name} was removed")
                            false
                        }
                        attrs.defaultFileList = arrayOf(DefaultFileListItem(
                                uid = "1",
                                name = "asd",
                                status = "done",
                                url = "asd.com"
                        ),
                                DefaultFileListItem(
                                        uid = "2",
                                        name = "bsd",
                                        status = "error",
                                        url = "asd.com",
                                        response = "Túl nagy fájlméret (nagyobb mint 20MB)"
                                )
                        )
                        attrs.action = "//jsonplaceholder.typicode.com.shitaka/posts/"
                        attrs.onChange = { info ->
                            val status = info.file.status
                            if (status !== "uploading") {
                                console.log(info.file, info.fileList)
                            }
                            if (status === "done") {
                                message.success("${info.file.name} feltöltése sikeresen befejeződött.")
                            } else if (status === "error") {
                                message.error("${info.file.name} feltöltése sikertelen.")
                            }
                        }
                        p(classes = "ant-upload-drag-icon") {
                            Icon("inbox")
                        }
                        p(classes = "ant-upload-text") {
                            +"Kattints vagy mozgass egy fájlt ide a feltöltéshez"
                        }
                        p(classes = "ant-upload-hint") {
                            +"Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files"
                        }
                    }
                    val (currentScreen: AppScreen, changeScreenDispatcher: Dispatcher<AppScreen>) = useState(AppScreen.LoginAppScreen() as AppScreen)
                    useEffect(changeSet = arrayOf(appState.urlData)) {
                        route(appState.megrendelesState.megrendelesek,
                                appState.maybeLoggedInUser,
                                appState.urlData.path,
                                changeScreenDispatcher,
                                dispatch)
                    }
                    val content = when (currentScreen) {
//                    is AccountAppScreen -> AccountScreen({ this.editingAccountId = screen.editingAccountId })
//                    is SajatArAppScreen -> SajatArScreen({ this.editingSajatArId = screen.editingSajatArId })
//                    LoginAppScreen -> LoginScreen()
//                    is AlvallalkozoAppScreen -> AlvallalkozoScreen({
//                        this.editingAlvallalkozoId = screen.avId
//                        this.editingErtekbecsloId = screen.ebId
//                    })
                        is AppScreen.MegrendelesAppScreen -> {
                            if (appState.maybeLoggedInUser != null) {
                                if (currentScreen.editingMegrendelesId == null) {
                                    megrendelesScreen(appState.maybeLoggedInUser,
                                            appState,
                                            dispatch)
                                } else {
                                    megrendelesForm(appState.megrendelesState.megrendelesek[currentScreen.editingMegrendelesId]!!)
                                }
                            } else null
                        }
                        is AppScreen.LoginAppScreen -> null
                        is AppScreen.AlvallalkozoAppScreen -> alvallalkozoScreen(currentScreen.avId,
                                appState.maybeLoggedInUser!!,
                                appState,
                                dispatch)
                        is AppScreen.AccountAppScreen -> accountScreen(currentScreen.editingAccountId, appState.maybeLoggedInUser!!,
                                appState,
                                dispatch)
                        is AppScreen.RegioAppScreen -> {
                            regioScreen(
                                    currentScreen.alvallalkozoId,
                                    currentScreen.regioId,
                                    appState.maybeLoggedInUser!!,
                                    appState,
                                    dispatch
                            )
                        }
                        is AppScreen.ErtekbecsloAppScreen -> ertekbecsloScreen(
                                currentScreen.avId,
                                currentScreen.ebId, appState.maybeLoggedInUser!!,
                                appState,
                                dispatch
                        )
                        is AppScreen.SajatArAppScreen -> sajatArScreen(
                                currentScreen.editingSajatArId,
                                appState.maybeLoggedInUser!!,
                                appState,
                                dispatch
                        )
                    }
                    Layout {
                        Header {
                            attrs.style = jsStyle {
                                height = "auto"
                            }
                            div("logo") { }
                            Menu {
                                attrs.mode = MenuMode.horizontal
                                attrs.theme = Theme.dark
                                attrs.selectedKeys = arrayOf(currentScreen::class.simpleName!!)
                                attrs.onSelect = { event ->
                                    when (event.key) {
                                        AppScreen.MegrendelesAppScreen::class.simpleName -> dispatch(Action.ChangeURL(Path.megrendeles.root))
                                        AppScreen.SajatArAppScreen::class.simpleName -> dispatch(Action.ChangeURL(Path.sajatAr.root))
                                        AppScreen.AlvallalkozoAppScreen::class.simpleName -> dispatch(Action.ChangeURL(Path.alvallalkozo.root))
                                        AppScreen.AccountAppScreen::class.simpleName -> dispatch(Action.ChangeURL(Path.account.root))
                                        AppScreen.RegioAppScreen::class.simpleName -> {
                                            val initialAlvallalkozoId = appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.first().id
                                            dispatch(Action.ChangeURL(Path.alvallalkozo.regio(initialAlvallalkozoId)))
                                        }
                                        AppScreen.ErtekbecsloAppScreen::class.simpleName -> {
                                            val initialAlvallalkozoId = appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.first().id
                                            dispatch(Action.ChangeURL(Path.ertekbecslo.root(initialAlvallalkozoId)))
                                        }
                                    }
                                }
                                MenuItem(AppScreen.MegrendelesAppScreen::class.simpleName!!) { +"Megrendelések" }
                                MenuItem(AppScreen.SajatArAppScreen::class.simpleName!!) { +"Sajár ár" }
                                MenuItem(AppScreen.AlvallalkozoAppScreen::class.simpleName!!) { +"Alvállalkozó/Régió" }
                                MenuItem(AppScreen.ErtekbecsloAppScreen::class.simpleName!!) { +"Értékbecslők" }
                                MenuItem(AppScreen.AccountAppScreen::class.simpleName!!) { +"Felhasználók" }
                                MenuItem(AppScreen.RegioAppScreen::class.simpleName!!) { +"Régiók" }
                            }
                        }
                        Content {
                            attrs.style = jsStyle {
                                margin = "24px 16px"
                                padding = "0px"
                            }
                            if (content != null) {
                                child(content)
                            }
                        }
                        Footer {
                            +"Presting Iktató ©2019 Created by NeverMind Software Kft"
                        }
                    }
                }
            }
        }
    }

    val myRender = require("react-dom").render
    val element = require("react").createElement(AppComponent, null)
    myRender(element, document.getElementById("root"))
}

sealed class AppScreen {
    data class MegrendelesAppScreen(
            val editingMegrendelesId: Int?,
            val showAlvallalkozoSajatAdataiModal: Boolean = false,
            val showTableConfigurationModal: Boolean = false) : AppScreen()

    class LoginAppScreen : AppScreen()

    data class AlvallalkozoAppScreen(val avId: Int?) : AppScreen()
    data class ErtekbecsloAppScreen(val avId: Int, val ebId: Int?) : AppScreen()
    data class AccountAppScreen(val editingAccountId: Int?) : AppScreen()
    data class RegioAppScreen(val alvallalkozoId: Int, val regioId: Int?) : AppScreen()
    data class SajatArAppScreen(val editingSajatArId: Int?) : AppScreen()
}

private fun route(megrendelesek: Map<Int, Megrendeles>,
                  maybeLoggedInUser: LoggedInUser?,
                  path: String,
                  screenDispatcher: Dispatcher<AppScreen>,
                  appStateDispatcher: Dispatcher<Action>) {
    console.log("Route: $path")
    val loggingDispatcher = { screen: AppScreen ->
        console.log("-- match: $screen")
        screenDispatcher(screen)
    }
    if (false && !maybeLoggedInUser.isLoggedIn) {
        appStateDispatcher(Action.ChangeURL(Path.login))
    } else {
        RouterStore.match(path,
                Path.login to { params ->
                    loggingDispatcher(AppScreen.LoginAppScreen())
                },
                "${Path.account.root}?editedAccountId" to { params ->
                    val id = (params["editedAccountId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.AccountAppScreen(id))
                },
                "${Path.sajatAr.root}?editedSajatArId" to { params ->
                    val id = (params["editedSajatArId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.SajatArAppScreen(id))
                },
                "${Path.alvallalkozo.root}regio/?alvallalkozoId/?regioOsszerendelesId" to { params ->
                    val avId = (params["alvallalkozoId"]!!).toInt()
                    val regioId = (params["regioOsszerendelesId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.RegioAppScreen(avId, regioId))
                },
                "${Path.alvallalkozo.root}av/:alvallalkozoId" to { params ->
                    val avId = (params["alvallalkozoId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.AlvallalkozoAppScreen(avId))
                },
                "${Path.ertekbecslo.r}:alvallalkozoId/ebs/?ertekbecsloId" to { params ->
                    val avId = (params["alvallalkozoId"] ?: "").toInt()
                    val ebId = (params["ertekbecsloId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.ErtekbecsloAppScreen(avId, ebId))
                },
                Path.alvallalkozo.sajatAdatok to { params ->
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null, showAlvallalkozoSajatAdataiModal = true))
                },
                Path.megrendeles.tableConfig to { params ->
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null, showTableConfigurationModal = true))
                },
                "${Path.megrendeles.root}:megrendelesId" to { params ->
                    val id = (params["megrendelesId"] ?: "").toIntOrNull()
                    if (id != 0 && id !in megrendelesek) {
                        communicator.getEntityFromServer<dynamic, Unit>(RestUrl.getMegrendelesByIdFromServer,
                                object {
                                    val megrendelesId = id
                                }) { response ->
                            appStateDispatcher(Action.MegrendelesekFromServer(response))
                        }
                    }
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(id))
                },
                Path.alvallalkozo.root to { params ->
                    loggingDispatcher(AppScreen.AlvallalkozoAppScreen(null))
                },
                otherwise = {
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null))
                }
        )
    }
}