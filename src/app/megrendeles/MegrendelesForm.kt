package app.megrendeles

import app.AccountScreenIds
import app.AppState
import app.Dispatcher
import app.common.Moment
import app.common.TimeUnit
import app.common.moment
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.megrendeles.alapAdatokTab
import hu.nevermind.utils.app.megrendeles.munkatipusokForRegio
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import kotlinx.html.js.onClickFunction
import react.*
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

fun megrendelesForm(megrendelesId: Int, appState: AppState, paramGlobalDispatch: (Action) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        //val megrendeles: Megrendeles = props.megrendeles
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
        buildElement {
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
                }
            }
        }
    }, props = jsObject<dynamic> {
        this.megrendelesId = megrendelesId
        this.appState = appState
        this.globalDispatch = paramGlobalDispatch
    })
}


fun RBuilder.ingatlanAdataiTab(state: MegrendelesFormState, appState: AppState, setState: Dispatcher<MegrendelesFormState>) {
    Collapse {
        attrs.bordered = false
        attrs.defaultActiveKey = arrayOf("Helyszínelés", "Ingatlan")
        Panel("Helyszínelés") {
            attrs.header = StringOrReactElement.fromString("Helyszínelés")
            helyszinelesPanel(state, appState, setState)
        }
        Panel("Ingatlan") {
            attrs.header = StringOrReactElement.fromString("Ingatlan")
            ingatlanPanel(state, appState, setState)
        }
    }
}

fun RBuilder.helyszinelesPanel(state: MegrendelesFormState, appState: AppState, setState: Dispatcher<MegrendelesFormState>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Szemle időpontja")
                Checkbox {
                    attrs.checked = state.megrendeles.szemleIdopontja != null
                    attrs.onChange = { checked ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                szemleIdopontja = if (checked) moment() else null
                        )))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = state.megrendeles.szemleIdopontja == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szemleIdopontja
                    attrs.value = state.megrendeles.szemleIdopontja
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setState(state.copy(megrendeles = state.megrendeles.copy(szemleIdopontja = date)))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Helyszínelő")
                val alv = appState.alvallalkozoState.alvallalkozok[state.megrendeles.alvallalkozoId]
                val source = (if (alv != null) {
                    appState.alvallalkozoState.getErtekbecslokOf(alv)
                            .filter { !it.disabled }
                            .map { it.name }
                            .filter { it.isNotEmpty() }
                } else emptyList()).toTypedArray()
                val helpMsg = if (alv != null &&
                        appState.alvallalkozoState.getErtekbecslokOf(alv)
                                .filter { !it.disabled }
                                .none { eb -> eb.name == state.megrendeles.helyszinelo })
                    "A megadott név nem szerepel a választható Értékbecslők között"
                else
                    null
                attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                attrs.hasFeedback = helpMsg != null
                attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                AutoComplete(source) {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.helyszinelo
                    attrs.value = state.megrendeles.helyszinelo ?: ""
                    attrs.placeholder = "Helyszínelő"
                    attrs.filterOption = { inputString, optionElement ->
                        (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                    }
                    attrs.onChange = { value ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(helyszinelo = value)))
                    }
                }
            }
        }
    }
}

fun RBuilder.ingatlanPanel(state: MegrendelesFormState, appState: AppState, setState: Dispatcher<MegrendelesFormState>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Ingatlan bővebb típus")
                Select {
                    attrs.asDynamic().style = jsStyle { minWidth = 300 }
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ingatlanBovebbTipus
                    attrs.value = state.megrendeles.ingatlanBovebbTipus
                    attrs.onSelect = { value, option ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                ingatlanBovebbTipus = value
                        )))
                    }
                    ingatlanBovebbTipusaArray.forEach { ingatlanBt ->
                        Option { attrs.value = ingatlanBt; +(ingatlanBt) }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Ingatlan készültségi foka")
                MyNumberInput {
                    attrs.number = state.megrendeles.keszultsegiFok?.toLong()
                    attrs.onValueChange = { value ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                keszultsegiFok = value?.toInt()
                        )))
                    }
                }
            }
        }
    }
    Row {
        Col(span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Lakás terület (m²)")
                MyNumberInput {
                    attrs.number = state.megrendeles.lakasTerulet?.toLong()
                    attrs.onValueChange = { value ->
                        val (fajlagosBecsultErtek, fajlagosEladasiAr) =
                                if (state.megrendeles.ingatlanBovebbTipus != "beépítetlen terület") {
                                    if (value != null && value > 0) {
                                        (state.megrendeles.becsultErtek ?: 0).div(value.toInt()) to
                                                (state.megrendeles.eladasiAr ?: 0).div(value.toInt())
                                    } else {
                                        state.megrendeles.fajlagosBecsultAr to
                                                state.megrendeles.fajlagosEladAr
                                    }
                                } else {
                                    state.megrendeles.fajlagosBecsultAr to
                                            state.megrendeles.fajlagosEladAr
                                }

                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                lakasTerulet = value?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek,
                                fajlagosEladAr = fajlagosEladasiAr
                        )))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Telek terület (m²)")
                MyNumberInput {
                    attrs.number = state.megrendeles.telekTerulet?.toLong()
                    attrs.onValueChange = { value ->
                        val (fajlagosBecsultErtek, fajlagosEladasiAr) =
                                if (state.megrendeles.ingatlanBovebbTipus == "beépítetlen terület") {
                                    if (value != null && value > 0) {
                                        (state.megrendeles.becsultErtek ?: 0).div(value.toInt()) to
                                                (state.megrendeles.eladasiAr ?: 0).div(value.toInt())
                                    } else {
                                        state.megrendeles.fajlagosBecsultAr to
                                                state.megrendeles.fajlagosEladAr
                                    }
                                } else {
                                    state.megrendeles.fajlagosBecsultAr to
                                            state.megrendeles.fajlagosEladAr
                                }

                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                telekTerulet = value?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek,
                                fajlagosEladAr = fajlagosEladasiAr
                        )))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Becsült érték (Ft)")
                MyNumberInput {
                    attrs.number = state.megrendeles.becsultErtek?.toLong()
                    attrs.onValueChange = { becsultErtek ->
                        val terulet = if (state.megrendeles.ingatlanBovebbTipus == "beépítetlen terület")
                            state.megrendeles.telekTerulet
                        else
                            state.megrendeles.lakasTerulet
                        val fajlagosBecsultErtek = if (terulet != null && terulet > 0) {
                            (becsultErtek ?: 0).div(terulet)
                        } else null

                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                becsultErtek = becsultErtek?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek?.toInt()
                        )))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Eladási ár (Ft)")
                MyNumberInput {
                    attrs.number = state.megrendeles.eladasiAr?.toLong()
                    attrs.onValueChange = { eladasiAr ->
                        val terulet = if (state.megrendeles.ingatlanBovebbTipus == "beépítetlen terület")
                            state.megrendeles.telekTerulet
                        else
                            state.megrendeles.lakasTerulet
                        val fajlagosEladasiAr = if (terulet != null && terulet > 0) {
                            (eladasiAr ?: 0).div(terulet)
                        } else null
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                eladasiAr = eladasiAr?.toInt(),
                                fajlagosEladAr = fajlagosEladasiAr?.toInt()
                        )))
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Fajlagos becsült ár (Ft)")
                MyNumberInput {
                    attrs.number = state.megrendeles.fajlagosBecsultAr?.toLong()
                    attrs.onValueChange = { value ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                fajlagosBecsultAr = value?.toInt()
                        )))
                    }
                }
            }
        }
        Col(offset = 1, span = 7) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Fajlagos eladási ár (Ft)")
                MyNumberInput {
                    attrs.number = state.megrendeles.fajlagosEladAr?.toLong()
                    attrs.onValueChange = { value ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                fajlagosEladAr = value?.toInt()
                        )))
                    }
                }
            }
        }
    }
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Adásvétel dátuma")
                Checkbox {
                    attrs.checked = state.megrendeles.adasvetelDatuma != null
                    attrs.onChange = { checked ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                adasvetelDatuma = if (checked) moment() else null
                        )))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = state.megrendeles.adasvetelDatuma == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.adasvetelDatuma
                    attrs.value = state.megrendeles.adasvetelDatuma
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setState(state.copy(megrendeles = state.megrendeles.copy(adasvetelDatuma = date)))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("HET kód")
                Input {
                    attrs.value = state.megrendeles.hetKod ?: ""
                    attrs.onChange = { e ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                hetKod = e.currentTarget.asDynamic().value
                        )))
                    }
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