package app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.Moment
import app.common.TimeUnit
import app.common.moment
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import react.*
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.megyek

private data class MegrendelesFormState(val activeTab: String,
                                        val megrendeles: Megrendeles,
                                        val szamlazhatoDijAfa: Int?,
                                        val azonosito1: String,
                                        val azonosito2: String,
                                        val selectableMunkatipusok: Collection<String>,
                                        val selectableAlvallalkozok: Collection<Alvallalkozo>)

fun megrendelesForm(megrendelesId: Int, appState: AppState): ReactElement {
    return createElement(type = { props: dynamic ->
        //val megrendeles: Megrendeles = props.megrendeles
        val id: Int = props.megrendelesId
        val appState: AppState = props.appState
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
                selectableAlvallalkozok = appState.alvallalkozoState.getSelectableAlvallalkozok(megrendeles.regio),
                selectableMunkatipusok = munkatipusokForRegio(appState.alvallalkozoState, megrendeles.regio)
        ))
        buildElement {
            Tabs {
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.first
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Alap adatok", color = "black", icon = "list-alt"))
                    alapAdatokTab(megrendeles, state, appState, setState)
//                }
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.ingatlanAdatai
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Ingatlan adatai", color = "red", icon = "home"))
                }
            }
        }
    }, props = jsObject<dynamic> {
        this.megrendelesId = megrendelesId
        this.appState = appState
    })
}

private fun RElementBuilder<TabPaneProps>.alapAdatokTab(paramMegrendeles: Megrendeles, state: MegrendelesFormState, appState: AppState,
                                                        setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row(gutter = 24) {
            Col(span = 8) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Megrendelő")
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelo
                        attrs.value = paramMegrendeles.megrendelo
                        attrs.onSelect = { newMegrendelo: String, option ->
                            setMegrendelo(appState, paramMegrendeles, state, newMegrendelo, setState)
                        }
                        appState.sajatArState.allMegrendelo.forEach { megrendeloName ->
                            Option { attrs.value = megrendeloName; +megrendeloName }
                        }
                    }
                }
            }
            Col(span = 8) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Régió")
                    Select {
                        attrs.value = state.megrendeles.regio
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.regio
                        attrs.onSelect = { regio: String, option ->
                            setNewRegio(appState, state, state.megrendeles, regio, setState)
                        }
                        megyek.forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
            Col(span = 8) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Munkatípus")
                    Select {
                        attrs.value = state.megrendeles.munkatipus
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.munkatipus
                        attrs.onSelect = { munkatipus, option ->
                            setMunkatipus(appState, state, state.megrendeles, munkatipus, setState)
                        }
                        state.selectableMunkatipusok.forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
        }
        Row(gutter = 24) {
            Col(span = 8) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Ingatlan típus (munkadíj meghatározásához)")
                    val sajatArak = appState.sajatArState.getSajatArakFor(state.megrendeles.megrendelo, state.megrendeles.munkatipus)
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ingatlanTipusMunkadijMeghatarozasahoz
                        attrs.value = sajatArak.firstOrNull { it.leiras == state.megrendeles.ingatlanTipusMunkadijMeghatarozasahoz }?.id ?: ""
                        attrs.disabled = sajatArak.isEmpty()
                        attrs.onSelect = { sajatArId: Int, option ->
                            setLeiras(appState, state, state.megrendeles, sajatArId, setState)
                        }
                        sajatArak.forEach { sajatAr ->
                            Option { attrs.value = sajatAr.id; +sajatAr.leiras }
                        }
                    }
                }
            }
            Col(span = 8) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Alvállalkozó")
                    val selectableAlvallalkozok = state.selectableAlvallalkozok
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.alvallalkozo
                        attrs.value = if (state.megrendeles.alvallalkozoId == 0) "" else state.megrendeles.alvallalkozoId
                        attrs.disabled = selectableAlvallalkozok.isEmpty()
                        attrs.onSelect = { avId: Int, option ->
                            setState(state.copy(megrendeles = setAlvallalkozoId(appState, state.megrendeles, avId)))
                        }
                        selectableAlvallalkozok.forEach { alv ->
                            Option { attrs.value = alv.id; +alv.name }
                        }
                    }
                }
            }
        }
    }
}

private fun setNewRegio(appState: AppState,
                        oldState: MegrendelesFormState,
                        megr: Megrendeles,
                        newRegio: String,
                        setState: Dispatcher<MegrendelesFormState>) {
    val newSelectableAlvallalkozok = appState.alvallalkozoState.getSelectableAlvallalkozok(newRegio)
    val defaultSelectedAlvallalkozoId = newSelectableAlvallalkozok.firstOrNull()?.id
    val newMegr = setAlvallalkozoId(appState, megr.copy(regio = newRegio), defaultSelectedAlvallalkozoId)

    val munkatipusok = munkatipusokForRegio(appState.alvallalkozoState, newRegio)
    val currentMunkatipus = munkatipusok.firstOrNull { it == newMegr.munkatipus }
    val defaultMunkatipus = munkatipusok.let { munkatipusok ->
        if (munkatipusok.size == 1) {
            munkatipusok.first()
        } else if (currentMunkatipus != null) {
            currentMunkatipus
        } else {
            null
        }
    } ?: ""

    setMunkatipus(appState, oldState, newMegr, defaultMunkatipus) { newState ->
        setState(newState.copy(
                selectableAlvallalkozok = newSelectableAlvallalkozok,
                selectableMunkatipusok = munkatipusok
        ))
    }
}

private fun munkatipusokForRegio(alvallalkozoState: AlvallalkozoState, regio: String): List<String> {
    return alvallalkozoState.regioOsszerendelesek.values
            .filter { regioOssz -> regioOssz.megye == regio }
            .map { it.munkatipus }
            .distinct()
}

private fun setMunkatipus(appState: AppState,
                          oldState: MegrendelesFormState,
                          megr: Megrendeles,
                          newMunkatipus: String,
                          setState: Dispatcher<MegrendelesFormState>) {

    val sajatArId = getOnlySajatArOrNull(appState, megr.megrendelo, newMunkatipus)?.id
    setLeiras(appState, oldState, megr.copy(
            munkatipus = newMunkatipus
    ), sajatArId, setState)
}

private fun setAlvallalkozoId(appState: AppState, megr: Megrendeles, alvId: Int?): Megrendeles {
    val ebId = appState.alvallalkozoState.getErtekbecslokOf(alvId ?: 0).firstOrNull()?.id

    return recalcRegioOsszerendeles(megr.copy(
            alvallalkozoId = alvId ?: 0,
            ertekbecsloId = ebId ?: 0
    ), appState.alvallalkozoState)
}

private fun setMegrendelo(appState: AppState, megrendeles: Megrendeles,
                          oldState: MegrendelesFormState,
                          newMegrendelo: String, setState: Dispatcher<MegrendelesFormState>) {
    val sajatArId = getOnlySajatArOrNull(appState, newMegrendelo, megrendeles.munkatipus)?.id
    setLeiras(appState, oldState, megrendeles.copy(
            megrendelo = newMegrendelo
    ), sajatArId, setState)
}

private fun setLeiras(appState: AppState, oldState: MegrendelesFormState,
                      megr: Megrendeles,
                      sajatArId: Int?,
                      setState: Dispatcher<MegrendelesFormState>) {
    val sajatAr = appState.sajatArState.sajatArak[sajatArId]
    val modifiedMegr = megr.copy(
            ingatlanTipusMunkadijMeghatarozasahoz = sajatAr?.leiras ?: "",
            szamlazhatoDij = sajatAr?.nettoAr
    )

    setState(oldState.copy(
            megrendeles = recalcRegioOsszerendeles(modifiedMegr, appState.alvallalkozoState),
            szamlazhatoDijAfa = sajatAr?.afa
    ))
}

private fun recalcRegioOsszerendeles(megr: Megrendeles, alvallalkozoState: AlvallalkozoState): Megrendeles {
    val newRegioOsszerendeles = alvallalkozoState.getRegioOsszerendelesek(megr.alvallalkozoId).firstOrNull { it.megye == megr.regio && it.munkatipus == megr.munkatipus && it.leiras == megr.ingatlanTipusMunkadijMeghatarozasahoz }
    val ebDija = newRegioOsszerendeles?.nettoAr
    return megr.copy(
            ertekbecsloDija = ebDija
    )
}

private fun getOnlySajatArOrNull(appState: AppState, megrendelo: String, munkatipus: String): SajatAr? {
    return appState.sajatArState.getSajatArakFor(megrendelo, munkatipus).firstOrNull()
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