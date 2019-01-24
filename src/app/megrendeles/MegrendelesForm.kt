package app.megrendeles

import app.AccountScreenIds
import app.AppState
import app.common.Moment
import app.common.TimeUnit
import app.common.moment
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.app.megrendeles.alapAdatokTab
import hu.nevermind.utils.app.megrendeles.ingatlanAdataiTab
import hu.nevermind.utils.app.megrendeles.munkatipusokForRegio
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.ReactElement
import react.buildElement
import react.dom.a
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.Action

data class MegrendelesFormState(val activeTab: String,
                                val megrendeles: Megrendeles,
                                val szamlazhatoDijAfa: Int?,
                                val azonosito1: String,
                                val azonosito2: String,
                                val ertesitendoSzemelyAzonos: Boolean,
                                val selectableMunkatipusok: Collection<String>,
                                val selectableAlvallalkozok: Collection<Alvallalkozo>)

data class MegrendelesFormScreenParams(val megrendelesId: Int,
                                       val appState: AppState,
                                       val globalDispatch: (Action) -> Unit)

object MegrendelesFormScreenComponent : DefinedReactComponent<MegrendelesFormScreenParams>() {
    override fun RBuilder.body(props: MegrendelesFormScreenParams) {
        val id: Int = props.megrendelesId
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val megrendeles = if (id == 0)
            Megrendeles(
                    megrendelo = appState.sajatArState.allMegrendelo.first(),
                    foVallalkozo = "Presting Zrt.",
                    munkatipus = Munkatipusok.Ertekbecsles.str,
                    ingatlanTipusMunkadijMeghatarozasahoz = appState.sajatArState.getSajatArakFor("Presting Zrt.", Munkatipusok.Ertekbecsles.str).firstOrNull()?.leiras
                            ?: "",
                    hitelTipus = "Vásárlás",
                    ingatlanBovebbTipus = "lakás",
                    hatarido = getNextWeekDay(5),
                    statusz = Statusz.B1
            ) else appState.megrendelesState.megrendelesek[id]!!.copy()
        val sameName = !megrendeles.ertesitesiNev.isNullOrEmpty() && megrendeles.ertesitesiNev == megrendeles.ugyfelNeve
        val sameTel = !megrendeles.ertesitesiTel.isNullOrEmpty() && megrendeles.ertesitesiTel == megrendeles.ugyfelTel

        val azonosito1: String
        val azonosito2: String
        if (megrendeles.munkatipus == Munkatipusok.EnergetikaAndErtekBecsles.str) {
            azonosito1 = megrendeles.getErtekbecslesId()
            azonosito2 = megrendeles.getEnergetikaId()
        } else if (megrendeles.munkatipus == Munkatipusok.Energetika.str) {
            azonosito1 = ""
            azonosito2 = megrendeles.azonosito
        } else {
            azonosito1 = megrendeles.azonosito
            azonosito2 = ""
        }
        val sajatArak = appState.sajatArState.getSajatArakFor(megrendeles.megrendelo, megrendeles.munkatipus)
        val sajatAr = sajatArak.firstOrNull { it.leiras == megrendeles.ingatlanTipusMunkadijMeghatarozasahoz }

        val (state, setState) = useState(MegrendelesFormState(
                activeTab = MegrendelesScreenIds.modal.tab.first,
                megrendeles = megrendeles,
                szamlazhatoDijAfa = sajatAr?.afa,
                azonosito1 = azonosito1,
                azonosito2 = azonosito2,
                ertesitendoSzemelyAzonos = id == 0 || (sameName && sameTel),
                selectableAlvallalkozok = appState.alvallalkozoState.getSelectableAlvallalkozok(megrendeles.regio),
                selectableMunkatipusok = munkatipusokForRegio(appState.alvallalkozoState, megrendeles.regio)
        ))
        div {
            Breadcrumb {
                BreadcrumbItem {
                    a(href = "#") {
                        attrs.onClickFunction = {
                            globalDispatch(Action.ChangeURL(Path.megrendeles.root))
                            false
                        }
                        +"Megrendelések"
                    }
                }
                BreadcrumbItem {
                    +createAzonosito(state)
                    Button {
                        attrs.asDynamic().id = AccountScreenIds.addButton
                        attrs.type = ButtonType.primary
                        attrs.asDynamic().style = jsStyle { marginLeft = "300px" }
                        attrs.onClick = {
                            val msg = object {
                                val id = state.megrendeles.id
                            }
                            communicator.getEntityFromServer<dynamic, Unit>(RestUrl.emailKuldeseUjra, msg) {
                                message.success("E-mail elküldve")
                            }
                        }
                        Icon("mail")
                        +" Email küldése újra"
                    }
                }
            }
            Tabs {
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.first
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Alap adatok", color = "black", icon = "list-alt"))
                    alapAdatokTab(state, appState, setState)
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.ingatlanAdatai
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Ingatlan adatai", color = "red", icon = "home"))
                    ingatlanAdataiTab(state, appState, setState)
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.akadalyok
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Megjegyzés/Akadály", color = "black", icon = "message"))
                }
                TabPane {
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Fájlok", color = "black", icon = "upload"))
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.penzBeerkezett
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Pénz beérkezett", color = "black", icon = "dollar"))
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.feltoltesZarolas
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Feltöltés/Zárolás", color = "black", icon = "file-done"))
                }
            }
        }
    }
}


private fun tabTitle(text: String, icon: String? = null, badgeNum: Int? = null, color: String? = null): ReactElement = buildElement {
    div {
        if (icon != null) {
            Icon(icon)
        }
        colorText(color ?: "white", " $text")
        if (badgeNum != null) {
            Badge(badgeNum)
        }
    }
}!!

fun colorTextElement(c: String, str: String) = buildElement { span { attrs.jsStyle { color = c }; +str } }

fun RBuilder.colorText(c: String, str: String): Unit {
    span { attrs.jsStyle { color = c }; +str }
}

fun getNextWeekDay(dayCount: Int): Moment {
    var day = moment()
    var step = 0
    while (step < dayCount) {
        day = day.add(1, TimeUnit.Days)
        if (day.isoWeekday() != 6 && day.isoWeekday() != 7) {
            ++step
        }
    }
    return day
}

private fun createAzonosito(state: MegrendelesFormState): String {
    return if (state.megrendeles.munkatipus == Munkatipusok.EnergetikaAndErtekBecsles.str) {
        "EB${state.azonosito1}_ET${state.azonosito2}"
    } else if (state.megrendeles.munkatipus == Munkatipusok.Energetika.str) {
        state.azonosito2
    } else {
        state.azonosito1
    }
}