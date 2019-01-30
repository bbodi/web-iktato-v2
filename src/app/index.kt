package app

import app.common.moment
import app.megrendeles.MegrendelesFormScreenComponent
import app.megrendeles.MegrendelesFormScreenParams
import app.megrendeles.MegrendelesScreenComponent
import app.megrendeles.MegrendelesScreenParams
import hu.nevermind.antd.*
import hu.nevermind.iktato.JqueryAjaxPoster
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.iktato.Result
import hu.nevermind.utils.hu.nevermind.antd.Menu
import hu.nevermind.utils.hu.nevermind.antd.MenuItem
import hu.nevermind.utils.hu.nevermind.antd.MenuMode
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.require
import kotlinext.js.requireAll
import react.RProps
import react.ReactElement
import react.buildElement
import react.dom.div
import store.*
import kotlin.browser.document
import kotlin.browser.window

data class Geo(val irszamok: List<Irszam>, val varosok: List<Varos>)

data class AppState(val megrendelesState: MegrendelesState,
                    val alvallalkozoState: AlvallalkozoState,
                    val currentScreen: AppScreen,
                    val urlData: UrlData,
                    val sajatArState: SajatArState,
                    val accountStore: AccountStore,
                    val maybeLoggedInUser: LoggedInUser?,
                    val geoData: Geo)

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

    fun geoReducer(state: Geo, action: Action): Geo {
        return when (action) {
            is Action.MegrendelesekFromServer -> state
            is Action.SetLoggedInUser -> state.copy(
                    varosok = if (action.data != null && action.data.isAdmin) {
                        communicator.getEntitiesFromServer(RestUrl.getVarosok) { returnedArray: Array<dynamic> ->
                            returnedArray.map { json ->
                                Varos(
                                        id = json.id,
                                        telepules = json.telepules,
                                        irszam = json.irszam,
                                        utcanev = json.utcanev,
                                        kerulet = json.kerulet,
                                        utotag = json.utotag
                                )
                            }
                        }
                    } else emptyList(),
                    irszamok = if (action.data != null && action.data.isAdmin) {
                        communicator.getEntitiesFromServer(RestUrl.getIrszamok) { returnedArray: Array<dynamic> ->
                            returnedArray.map { json ->
                                Irszam(
                                        id = json.id,
                                        telepules = json.telepules,
                                        irszam = json.irszam,
                                        megye = json.megye
                                )
                            }
                        }
                    } else emptyList()

            )
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
                    geoData = geoReducer(currentState.geoData, action),
                    sajatArState = sajatArActionHandler(currentState.sajatArState, action),
                    accountStore = accountActionHandler(currentState.accountStore, action)
            )
        }, AppState(MegrendelesState(emptyMap()), AlvallalkozoState(),
                maybeLoggedInUser = null,
                currentScreen = AppScreen.LoginAppScreen(),
                urlData = UrlData(window.location.hash.substring(1), emptyMap()),
                sajatArState = SajatArState(),
                geoData = Geo(emptyList(), emptyList()),
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

        buildElement {
            div {
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
                                MegrendelesScreenComponent.createElement(MegrendelesScreenParams(
                                        appState,
                                        dispatch
                                ))
                            } else {
                                val megr = appState.megrendelesState.megrendelesek[currentScreen.editingMegrendelesId]
                                MegrendelesFormScreenComponent.createElement(MegrendelesFormScreenParams(
                                        megr,
                                        appState,
                                        dispatch
                                ))
                            }
                        } else null
                    }
                    is AppScreen.LoginAppScreen -> null
                    is AppScreen.AlvallalkozoAppScreen ->
                        AlvallalkozoScreenComponent.createElement(AlvallalkozoScreenParams(
                                currentScreen.avId,
                                appState,
                                dispatch))
                    is AppScreen.AccountAppScreen ->
                        AccountScreenComponent.createElement(AccountScreenParams(
                                currentScreen.editingAccountId,
                                appState,
                                dispatch
                        ))
                    is AppScreen.RegioAppScreen -> {
                        RegioScreenComponent.createElement(RegioScreenParams(
                                currentScreen.alvallalkozoId,
                                currentScreen.regioId,
                                appState,
                                dispatch
                        ))
                    }
                    is AppScreen.ErtekbecsloAppScreen ->
                        ErtekbecsloScreenComponent.createElement(ErtekbecsloScreenParams(
                                currentScreen.avId,
                                currentScreen.ebId,
                                appState,
                                dispatch
                        ))
                    is AppScreen.SajatArAppScreen ->
                        SajatArScreenComponent.createElement(SajatArScreenParams(
                                currentScreen.editingSajatArId,
                                appState,
                                dispatch
                        ))
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