package app

import app.common.moment
import app.megrendeles.*
import hu.nevermind.antd.*
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.runIntegrationTests
import hu.nevermind.utils.hu.nevermind.antd.Menu
import hu.nevermind.utils.hu.nevermind.antd.MenuItem
import hu.nevermind.utils.hu.nevermind.antd.MenuMode
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.require
import kotlinext.js.requireAll
import org.w3c.dom.events.Event
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
                    val megrendelesTableFilterState: MegrendelesTableFilterState,
                    val currentScreen: AppScreen,
                    val urlData: UrlData,
                    val sajatArState: SajatArState,
                    val accountStore: AccountStore,
                    val maybeLoggedInUser: LoggedInUser?,
                    val geoData: Geo)

interface IdProps : RProps {
    var id: Int
}

val globalEventListeners: MutableList<(AppState) -> Unit> = arrayListOf()

fun main(args: Array<String>) {
    // HACK: operator invoke is translated as `invoke()` function call in Javascript -_-'
    moment.asDynamic().invoke = moment
    moment.asDynamic().fn.toJSON = {
        js("return this.format('$dateTimeFormat');")
    }

    requireAll(require.context("src", true, js("/\\.css$/")))

    fun appReducer(state: LoggedInUser?, action: Action): LoggedInUser? {
        return when (action) {
            is Action.DeleteAlvallalkozo -> state
            is Action.DeleteMegrendeles -> state
            is Action.MegrendelesekFromServer -> state
            is Action.SetLoggedInUser -> action.data
            is Action.ChangeURL -> state
            is Action.changeURLSilently -> state
            is Action.SetActiveFilter -> state
            is Action.SajatArFromServer -> state
            is Action.AccountFromServer -> state
            is Action.AlvallalkozoFromServer -> state
            is Action.ErtekbecsloFromServer -> state
            is Action.RegioOsszerendelesFromServer -> state
            is Action.DeleteRegioOsszerendeles -> state
        }
    }

    fun checkAuthenticationAndSetLoggedInUser(globalDispatch: (Action) -> Unit) {
        communicator.getRequest(RestUrl.authenticate) { authResult ->
            authResult.ifOk { response ->
                val principal = response.asDynamic()
                globalDispatch(Action.SetLoggedInUser(LoggedInUser(
                        principal.username,
                        principal.fullName,
                        principal.alvallalkozoId,
                        Role.valueOf(principal.role),
                        stringToColumnDefArray(principal.megrendelesTableColumns)
                )))
            }
            authResult.ifError {
                globalDispatch(Action.SetLoggedInUser(null))
            }
        }

    }

    fun megrendelesTableFilterReducer(state: MegrendelesTableFilterState, action: Action): MegrendelesTableFilterState {
        return when (action) {
            is Action.DeleteAlvallalkozo -> state
            is Action.DeleteMegrendeles -> state
            is Action.MegrendelesekFromServer -> state
            is Action.SetLoggedInUser -> state.copy(
                    haviTeljesites = if (action.data != null && action.data.isAlvallalkozo) {
                        HaviTeljesites(action.data.alvallalkozoId, moment())
                    } else null
            )
            is Action.ChangeURL -> state
            is Action.changeURLSilently -> state
            is Action.SetActiveFilter -> when (action.payload) {
                is SetActiveFilterPayload.SimpleFilter -> state.copy(
                        activeFilter = action.payload.activeFilter
                )
                is SetActiveFilterPayload.HaviTeljesites -> state.copy(
                        activeFilter = MegrendelesScreen.haviTeljesitesFilter,
                        haviTeljesites = HaviTeljesites(
                                action.payload.alvallalkozoId,
                                action.payload.date
                        )
                )
                is SetActiveFilterPayload.MindFilter -> state.copy(
                        activeFilter = MegrendelesScreen.mindFilter,
                        mindFilteredMegrendelesIds = action.payload.ids,
                        szuroMezok = action.payload.szuromezok
                )
            }
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
            is Action.DeleteAlvallalkozo -> state
            is Action.DeleteMegrendeles -> state
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
            is Action.SetActiveFilter -> state
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
            val newUrlData = routerStoreHandler(currentState.urlData, action)
            val maybeLoggedInUser = appReducer(currentState.maybeLoggedInUser, action)
            currentState.copy(
                    megrendelesState = megrendelesekFromServer(currentState.megrendelesState, action),
                    alvallalkozoState = alvallalkozoActionHandler(currentState.alvallalkozoState, action),
                    urlData = newUrlData,
                    currentScreen = route(maybeLoggedInUser, newUrlData.path),
                    maybeLoggedInUser = maybeLoggedInUser,
                    megrendelesTableFilterState = megrendelesTableFilterReducer(currentState.megrendelesTableFilterState, action),
                    geoData = geoReducer(currentState.geoData, action),
                    sajatArState = sajatArActionHandler(currentState.sajatArState, action),
                    accountStore = accountActionHandler(currentState.accountStore, action)
            ).also { state ->
                globalEventListeners.forEach { it(state) }
            }
        }, AppState(MegrendelesState(emptyMap()), AlvallalkozoState(),
                maybeLoggedInUser = null,
                currentScreen = AppScreen.LoginAppScreen(),
                urlData = UrlData(window.location.hash.substring(1), emptyMap()),
                sajatArState = SajatArState(),
                geoData = Geo(emptyList(), emptyList()),
                megrendelesTableFilterState = MegrendelesTableFilterState(
                        activeFilter = atNemVettFilter,
                        mindFilteredMegrendelesIds = emptyList(),
                        szuroMezok = emptyList(),
                        haviTeljesites = null
                ),
                accountStore = AccountStore(emptyArray())))
        useEffectWithCleanup(RUN_ONLY_WHEN_MOUNT) {
            console.info("pollServerForChanges")
            var pollTimer = 0
            pollTimer = window.setTimeout({
                pollTimer = pollServerForChanges(moment(), dispatch)
            }, 60_000)
            val cleanup = {
                console.info("CLEANUP - pollServerForChanges $pollTimer")
                window.clearTimeout(pollTimer)
            }
            cleanup
        }
        useEffectWithCleanup {
            val callback = { e: Event ->
                val externalChange = appState.urlData.path != window.location.hash.substring(1)
                if (externalChange) {
                    dispatch(Action.ChangeURL(window.location.hash.substring(1)))
                }
            }
            window.addEventListener("hashchange", callback, false)
            val cleanup = {
                window.removeEventListener("hashchange", callback)
            }
            cleanup
        }
        useEffect(RUN_ONLY_WHEN_MOUNT) {
            checkAuthenticationAndSetLoggedInUser(dispatch)
        }

        buildElement {
            div {
                val currentScreen = appState.currentScreen
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
                                        currentScreen.showAlvallalkozoSajatAdataiModal,
                                        null,
                                        dispatch
                                ))
                            } else {
                                if (appState.maybeLoggedInUser.isAdmin) {
                                    MegrendelesFormScreenComponent.createElement(MegrendelesFormScreenParams(
                                            currentScreen.editingMegrendelesId,
                                            appState,
                                            dispatch
                                    ))
                                } else {
                                    MegrendelesScreenComponent.createElement(MegrendelesScreenParams(
                                            appState,
                                            currentScreen.showAlvallalkozoSajatAdataiModal,
                                            currentScreen.editingMegrendelesId,
                                            dispatch
                                    ))
                                }
                            }
                        } else null
                    }
                    is AppScreen.LoginAppScreen -> LoginScreenComponent.createElement(LoginScreenParams(
                            appState,
                            dispatch
                    ))
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
                            attrs.onClick = { event ->
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
                                    Path.alvallalkozo.sajatAdatok -> {
                                        dispatch(Action.ChangeURL(Path.alvallalkozo.sajatAdatok))
                                    }
                                    "User" -> {
                                        val accountId = appState.accountStore.accounts.firstOrNull { it.username == appState.maybeLoggedInUser?.username }?.id
                                        if (appState.maybeLoggedInUser.isAdmin && accountId != null) {
                                            dispatch(Action.ChangeURL(Path.account.withOpenedEditorModal(accountId)))
                                        } else {
                                            dispatch(Action.ChangeURL(Path.alvallalkozo.sajatAdatok))
                                        }
                                    }
                                    "Logout" -> {
                                        Modal.confim {
                                            title = "Biztos elhagyja az alkalmazást?"
                                            okText = "Igen"
                                            cancelText = "Mégsem"
                                            okType = ButtonType.primary
                                            onOk = {
                                                communicator.getRequest(RestUrl.logout) { authResult ->
                                                }
                                                dispatch(Action.SetLoggedInUser(null))
                                                null
                                            }
                                        }
                                    }
                                }
                            }
                            if (appState.maybeLoggedInUser != null) {
                                MenuItem(AppScreen.MegrendelesAppScreen::class.simpleName!!) {
                                    attrs.asDynamic().id = MegrendelesScreenIds.menu.megrendelesek
                                    +"Megrendelések"
                                }
                                if (appState.maybeLoggedInUser.isAdmin) {
                                    MenuItem(AppScreen.SajatArAppScreen::class.simpleName!!) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.menu.sajatar
                                        +"Sajár ár"
                                    }
                                    MenuItem(AppScreen.AlvallalkozoAppScreen::class.simpleName!!) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.menu.alvallalkozok
                                        +"Alvállalkozók"
                                    }
                                    MenuItem(AppScreen.ErtekbecsloAppScreen::class.simpleName!!) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.menu.ertekbecslok
                                        +"Értékbecslők"
                                    }
                                    MenuItem(AppScreen.AccountAppScreen::class.simpleName!!) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.menu.felhasznalok
                                        +"Felhasználók"
                                    }
                                    MenuItem(AppScreen.RegioAppScreen::class.simpleName!!) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.menu.regiok
                                        +"Régiók"
                                    }
                                }
                                MenuItem("Logout") {
                                    attrs.asDynamic().style = jsStyle {
                                        float = "right"
                                    }
                                    Icon("logout")
                                    +"Kijelentkezés"
                                }
                                MenuItem("User") {
                                    attrs.asDynamic().style = jsStyle {
                                        float = "right"
                                    }
                                    Icon("user")
                                    +appState.maybeLoggedInUser.fullName
                                }
                            } else {
                                MenuItem("Bejelentkezés") {
                                    Icon("user")
                                    +"Bejelentkezés"
                                }
                            }
                        }
                    }
                    Content {
                        attrs.style = jsStyle {
                            margin = "24px 16px"
                            padding = "0px"
                            minHeight = "600px"
                        }
                        if (content != null) {
                            child(content)
                        }
                    }
                    Footer {
                        +"Presting Iktató ©2019 Created by NeverMind Software Kft"
                        if (appState.maybeLoggedInUser?.username == "admin") {
                            Button {
                                attrs.type = ButtonType.primary
                                attrs.onClick = {
                                    runIntegrationTests(dispatch, appState)
                                }
                                +"Tests"
                            }
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

private fun route(maybeLoggedInUser: LoggedInUser?,
                  path: String): AppScreen {
    console.log("Route: $path")
    val loggingDispatcher: (AppScreen) -> AppScreen = { screen: AppScreen ->
        console.log("-- match: $screen")
        screen
    }
    return if (!maybeLoggedInUser.isLoggedIn)
        loggingDispatcher(AppScreen.LoginAppScreen())
    else if (maybeLoggedInUser.isAlvallalkozo) {
        RouterStore.match(path,
                Path.login to { params ->
                    loggingDispatcher(AppScreen.LoginAppScreen())
                },
                Path.alvallalkozo.sajatAdatok to { params ->
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null, showAlvallalkozoSajatAdataiModal = true))
                },
                Path.megrendeles.tableConfig to { params ->
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null, showTableConfigurationModal = true))
                },
                "${Path.megrendeles.root}:megrendelesId" to { params ->
                    val id = (params["megrendelesId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(id))
                },
                Path.alvallalkozo.root to { params ->
                    loggingDispatcher(AppScreen.AlvallalkozoAppScreen(null))
                })
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
                Path.megrendeles.tableConfig to { params ->
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(null, showTableConfigurationModal = true))
                },
                "${Path.megrendeles.root}:megrendelesId" to { params ->
                    val id = (params["megrendelesId"] ?: "").toIntOrNull()
                    loggingDispatcher(AppScreen.MegrendelesAppScreen(id))
                },
                Path.alvallalkozo.root to { params ->
                    loggingDispatcher(AppScreen.AlvallalkozoAppScreen(null))
                }
        )
    } ?: loggingDispatcher(AppScreen.MegrendelesAppScreen(null))
}