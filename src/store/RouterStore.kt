package store

import app.AppScreen
import kotlin.browser.window

data class UrlData(val path: String, val params: Map<String, String>)

fun routerStoreHandler(state: UrlData, action: Action): UrlData {
    return when (action) {
        is Action.ChangeURL -> {
            window.location.hash = action.url
            state.copy(
                    path = action.url
            )
        }
        is Action.MegrendelesekFromServer -> state
        is Action.SetLoggedInUser -> state
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

object RouterStore {


    //private var urlChangeOccurredByDispatchAction = false

    fun init() {
//        window.addEventListener("hashchange", {
//            val externalChange = urlChangeOccurredByDispatchAction == false
//            if (externalChange) {
//                path = window.location.hash.substring(1)
//                emitChange()
//            }
//            urlChangeOccurredByDispatchAction = false
//        }, false);
//        register(globalDispatcher, Actions.changeURL) { url ->
//            changeURL(url)
//            emitChange()
//        }
//        register(globalDispatcher, Actions.changeURLSilently) { url ->
//            changeURL(url)
//        }
    }

    fun match(path: String, vararg patterns: Pair<String, (Map<String, String>) -> AppScreen>): AppScreen? {
        val screen = patterns.map { patternPair ->
            val (pattern, predicate) = patternPair
            val screen = match(path, pattern, predicate)
            screen
        }.filterNotNull().firstOrNull()
        return screen;
    }

    fun match(path: String, pattern: String, body: (Map<String, String>) -> AppScreen): AppScreen? {
        val patternParts = pattern.split("/")
        val pathParts = path.split("/")
        val params = hashMapOf<String, String>()
        var ok = true
        patternParts.withIndex().forEach { (index, value) ->
            val part = if (pathParts.size > index) pathParts[index] else null
            if (value.startsWith("?")) {
                if (part != null && part.isNotEmpty()) {
                    params.put(value.substring(1), part)
                }
            } else if (value.startsWith(":") && part != null && part.isNotEmpty()) {
                params.put(value.substring(1), part)
            } else {
                if (pathParts.size <= index || part != value) {
                    ok = false
                    return null // KotlinJS sucks: it does nothing more than a simple continue...
                }
            }
        }
        return if (ok) {
            body(params)
        } else null
    }

}

class RouterStoreTest {

    fun tests() {
//        var matchResult = ""
//        val runMatcher = { path: String ->
//            RouterStore.match(path,
//                    Path.login to { params ->
//                        matchResult = "login"
//                    },
//                    "${Path.account.root}?id" to { params ->
//                        matchResult = "account with modal"
//                    },
//                    "${Path.alvallalkozo.root}regio/?alvallalkozoId/?regioOsszerendelesId" to { params ->
//                        matchResult = "regio av ossz"
//                    },
//                    "${Path.alvallalkozo.root}:alvallalkozoId/:ertekbecsloId" to { params ->
//                        matchResult = "av + eb"
//                    },
//                    "${Path.alvallalkozo.root}:alvallalkozoId" to { params ->
//                        matchResult = "av with modal"
//                    },
//                    Path.alvallalkozo.root to { params ->
//                        matchResult = "av"
//                    },
//                    otherwise = {
//                        matchResult = "other"
//                    }
//            )
//        }
        // TODO: tests
//        given("the URL = root") {
//            on("routing to the main screen") {
//                runMatcher(Path.root)
//                it("should not match") {
//                    assertEquals("other", matchResult)
//                }
//            }
//            on("routing to the login screen") {
//                runMatcher(Path.login)
//                it("should match to login") {
//                    assertEquals("login", matchResult)
//                }
//            }
//            on("routing to the keyValue editor screen") {
//                runMatcher(Path.account.withOpenedEditorModal(69))
//                it("should match to login") {
//                    assertEquals(1, RouterStore.pathParams.size)
//                    assertEquals("69", RouterStore.pathParams["id"])
//                    assertEquals("account with modal", matchResult)
//                }
//            }
//            on("alvallalkozo/7/") {
//                runMatcher("alvallalkozo/7/")
//                it("") {
//                    assertEquals(1, RouterStore.pathParams.size)
//                    assertEquals("7", RouterStore.pathParams["alvallalkozoId"])
//                    assertEquals("av with modal", matchResult)
//                }
//            }
//            on("alvallalkozo/7/3") {
//                runMatcher("alvallalkozo/7/3")
//                it("") {
//                    assertEquals("av + eb", matchResult)
//                    assertEquals(2, RouterStore.pathParams.size)
//                    assertEquals("7", RouterStore.pathParams["alvallalkozoId"])
//                    assertEquals("3", RouterStore.pathParams["ertekbecsloId"])
//                }
//            }
//            on("alvallalkozo/") {
//                runMatcher("alvallalkozo/")
//                it("") {
//                    assertEquals(0, RouterStore.pathParams.size)
//                    assertEquals("av", matchResult)
//                }
//            }
//            on("alvallalkozo/regio/") {
//                runMatcher("alvallalkozo/regio/")
//                it("") {
//                    assertEquals(0, RouterStore.pathParams.size)
//                    assertEquals("regio av ossz", matchResult)
//                }
//            }
//            on("alvallalkozo/regio/1//") {
//                runMatcher("alvallalkozo/regio/1/")
//                it("") {
//                    assertEquals(1, RouterStore.pathParams.size)
//                    assertEquals("1", RouterStore.pathParams["alvallalkozoId"])
//                    assertEquals("regio av ossz", matchResult)
//                }
//            }
//            on("alvallalkozo/regio/1/2") {
//                runMatcher("alvallalkozo/regio/1/2")
//                it("") {
//                    assertEquals(2, RouterStore.pathParams.size)
//                    assertEquals("1", RouterStore.pathParams["alvallalkozoId"])
//                    assertEquals("2", RouterStore.pathParams["regioOsszerendelesId"])
//                    assertEquals("regio av ossz", matchResult)
//                }
//            }
//        }
    }
}