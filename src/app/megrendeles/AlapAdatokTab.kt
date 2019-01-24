package hu.nevermind.utils.app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.moment
import app.megrendeles.*
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.AlvallalkozoState
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.SajatAr
import hu.nevermind.utils.store.isEnergetika
import react.RElementBuilder
import react.children
import react.dom.div
import react.dom.jsStyle
import store.kozteruletJellegek
import store.megyek
import kotlin.math.roundToLong

fun RElementBuilder<TabPaneProps>.alapAdatokTab(state: MegrendelesFormState, appState: AppState,
                                                        setState: Dispatcher<MegrendelesFormState>) {
    Collapse {
        attrs.bordered = false
        attrs.defaultActiveKey = arrayOf("Megrendelés", "Ügyfél", "Értesítendő személy", "Cím", "Hitel")
        Panel("Megrendelés") {
            attrs.header = StringOrReactElement.fromString("Megrendelés")
            megrendelesPanel(appState, state, setState)
        }
        Panel("Ügyfél") {
            attrs.header = StringOrReactElement.fromString("Ügyfél")
            ugyfelPanel(state, setState)
        }
        Panel("Értesítendő személy") {
            attrs.header = StringOrReactElement.fromString("Értesítendő személy")
            ertesitendoSzemelyPanel(state, setState)
        }
        Panel("Cím") {
            attrs.header = StringOrReactElement.fromString("Cím")
            cimPanel(appState, state, setState)
        }
        Panel("Hitel") {
            attrs.header = StringOrReactElement.fromString("Hitel")
            hitelPanel(state, setState)
        }
    }
}

private fun RElementBuilder<PanelProps>.cimPanel(appState: AppState, state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Helyrajzi szám")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.helyrajziSzam
                        attrs.value = state.megrendeles.hrsz
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(hrsz = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Irányítószám")
                    val helpMsg = state.megrendeles.irsz.let { inputIrsz ->
                        if (inputIrsz.isNullOrEmpty()) {
                            null
                        } else {
                            val irsz = appState.geoData.irszamok.firstOrNull { it.irszam == inputIrsz }
                            if (irsz == null) {
                                "Nem létezik ilyen irányítószám az adatbázisban!"
                            } else if (irsz.megye != state.megrendeles.regio) {
                                "A megadott irányítószám nem létezik a kiválasztott régióban!"
                            } else if (irsz.telepules != state.megrendeles.telepules) {
                                "A megadott irányítószám nem létezik a kiválasztott településen!"
                            } else {
                                null
                            }
                        }
                    }
                    attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                    attrs.hasFeedback = helpMsg != null
                    attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                    val source: Array<Any> = appState.geoData.irszamok.let { irszamok ->
                        val unknownRegio = state.megrendeles.regio !in megyek
                        if (unknownRegio) {
                            irszamok
                        } else {
                            irszamok.filter { it.megye == state.megrendeles.regio }
                        }.map { it.irszam }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.iranyitoszam
                        attrs.value = state.megrendeles.irsz
                        attrs.placeholder = "Irányítószám"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            val irsz = appState.geoData.irszamok.firstOrNull { it.irszam == value }
                            if (irsz != null) {
                                setState(state.copy(megrendeles = state.megrendeles.copy(
                                        irsz = value,
                                        telepules = irsz.telepules
                                )))
                            } else {
                                setState(state.copy(megrendeles = state.megrendeles.copy(irsz = value)))
                            }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Település")
                    val helpMsg = state.megrendeles.irsz.let { inputIrsz ->
                        val inputTelepules = state.megrendeles.telepules
                        if (inputTelepules.isNullOrEmpty() || inputIrsz.isNullOrEmpty()) {
                            null
                        } else {
                            val telepules = appState.geoData.irszamok.firstOrNull { it.telepules == state.megrendeles.telepules }
                            val telepulesWithIrszam = appState.geoData.irszamok.firstOrNull { it.telepules == state.megrendeles.telepules && it.irszam == state.megrendeles.irsz }
                            if (telepules == null) {
                                "Nem létezik ilyen település az adatbázisban!"
                            } else if (telepulesWithIrszam == null) {
                                "A megadott irányítószám nem található a településen!"
                            } else {
                                null
                            }
                        }
                    }
                    attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                    attrs.hasFeedback = helpMsg != null
                    attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                    val source: Array<Any> = appState.geoData.irszamok.let { irszamok ->
                        if (state.megrendeles.irsz.isNullOrEmpty()) {
                            irszamok
                        } else {
                            irszamok.filter { it.irszam == state.megrendeles.irsz }
                        }.map { it.telepules }.distinct()
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.telepules
                        attrs.value = state.megrendeles.telepules
                        attrs.placeholder = "Település"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 3) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(telepules = value)))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Kerület")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (state.megrendeles.telepules.isNullOrEmpty()) {
                            varosok
                        } else {
                            varosok.filter {
                                it.telepules == state.megrendeles.telepules &&
                                        it.irszam == state.megrendeles.irsz
                            }
                        }.map { it.kerulet }
                    }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kerulet
                        attrs.value = state.megrendeles.kerulet
                        attrs.placeholder = "Kerület"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(kerulet = value)))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Közterület neve")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (state.megrendeles.irsz.isNullOrEmpty()) {
                            emptyList<String>()
                        } else {
                            varosok.filter {
                                it.kerulet == state.megrendeles.kerulet &&
                                        it.irszam == state.megrendeles.irsz
                            }.map { it.utcanev }
                        }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kozteruletNeve
                        attrs.value = state.megrendeles.utca
                        attrs.placeholder = "Közterület neve"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            val utcaJellegek = appState.geoData.varosok.filter {
                                it.utcanev == value &&
                                        it.irszam == state.megrendeles.irsz
                            }.map { it.utotag }
                            val utcaJelleg = if (utcaJellegek.size == 1) utcaJellegek.first() else ""

                            setState(state.copy(megrendeles = state.megrendeles.copy(
                                    utca = value,
                                    utcaJelleg = utcaJelleg
                            )))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Közterület jellege")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (state.megrendeles.utca.isNullOrEmpty()) {
                            kozteruletJellegek.asList()
                        } else {
                            varosok.filter {
                                it.utcanev == state.megrendeles.utca &&
                                        it.irszam == state.megrendeles.irsz
                            }.map { it.utotag }
                        }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kozteruletJellege
                        attrs.value = state.megrendeles.utcaJelleg
                        attrs.placeholder = "Közterület jellege"
                        attrs.filterOption = { inputString, optionElement ->
                            (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(utcaJelleg = value)))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 6) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Házszám")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hazszam
                        attrs.value = state.megrendeles.hazszam
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(hazszam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Lépcsőház")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.lepcsohaz
                        attrs.value = state.megrendeles.lepcsohaz
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(lepcsohaz = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Emelet")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.emelet
                        attrs.value = state.megrendeles.emelet
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(emelet = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Ajtó")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ajto
                        attrs.value = state.megrendeles.ajto
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ajto = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
        }
    }
}

private fun RElementBuilder<PanelProps>.hitelPanel(state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row {
            Col(span = 10) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Ajánlatszám")
                    val beillesztett = true
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ajanlatSzam
                        attrs.value = state.megrendeles.ajanlatSzam
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ajanlatSzam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 2, span = 10) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Szerződésszám")
                    val beillesztett = true
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szerzodesSzam
                        attrs.value = state.megrendeles.szerzodesSzam
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(szerzodesSzam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }

        }
    }
}

private fun RElementBuilder<PanelProps>.ertesitendoSzemelyPanel(state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row {
            Col(span = 24) {
                FormItem {
                    attrs.labelCol = ColProperties { span = 8 }
                    attrs.wrapperCol = ColProperties { span = 16 }
                    attrs.label = StringOrReactElement.fromString("Értesítendő személy azonos az ügyféllel")
                    Checkbox {
                        attrs.checked = state.ertesitendoSzemelyAzonos
                        attrs.onChange = { checked ->
                            setState(state.copy(ertesitendoSzemelyAzonos = checked))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Név")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertesitendoNev
                        attrs.disabled = state.ertesitendoSzemelyAzonos
                        attrs.value = if (state.ertesitendoSzemelyAzonos) state.megrendeles.ugyfelNeve else state.megrendeles.ertesitesiNev
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ertesitesiNev = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Telefonszám")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertesitendoTel
                        attrs.disabled = state.ertesitendoSzemelyAzonos
                        attrs.value = if (state.ertesitendoSzemelyAzonos) state.megrendeles.ugyfelTel else state.megrendeles.ertesitesiTel
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ertesitesiTel = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
        }
    }
}


private fun RElementBuilder<PanelProps>.ugyfelPanel(state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.required = true
                    val beillesztett = true
                    attrs.label = StringOrReactElement.fromString("Név")
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else if (state.megrendeles.ugyfelNeve.isEmpty()) ValidateStatus.error else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelNev
                        attrs.value = state.megrendeles.ugyfelNeve
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ugyfelNeve = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.required = true
                    val beillesztett = true
                    attrs.label = StringOrReactElement.fromString("Telefonszám")
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else if (state.megrendeles.ugyfelTel.isEmpty()) ValidateStatus.error else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelTel
                        attrs.value = state.megrendeles.ugyfelTel
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ugyfelTel = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    val beillesztett = true
                    attrs.label = StringOrReactElement.fromString("Email cím")
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelEmail
                        attrs.value = state.megrendeles.ugyfelEmail
                        attrs.onChange = { e ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ugyfelEmail = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }

        }
    }
}

private fun RElementBuilder<PanelProps>.megrendelesPanel(appState: AppState, state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Megrendelő")
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelo
                        attrs.value = state.megrendeles.megrendelo
                        attrs.onSelect = { newMegrendelo: String, option ->
                            setMegrendelo(appState, state.megrendeles, state, newMegrendelo, setState)
                        }
                        appState.sajatArState.allMegrendelo.forEach { megrendeloName ->
                            Option { attrs.value = megrendeloName; +megrendeloName }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
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
            Col(offset = 1, span = 7) {
                FormItem {
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
        Row {
            Col(span = 7) {
                FormItem {
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
            Col(offset = 1, span = 7) {
                FormItem {
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
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Értékbecslő")
                    val selectableAlvallalkozok = state.selectableAlvallalkozok
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertekbecslo
                        attrs.value = if (state.megrendeles.ertekbecsloId == 0) "" else state.megrendeles.ertekbecsloId
                        attrs.disabled = selectableAlvallalkozok.isEmpty()
                        attrs.onSelect = { ebId: Int, option ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(ertekbecsloId = ebId)))
                        }
                        appState.alvallalkozoState.alvallalkozok[state.megrendeles.alvallalkozoId]?.let { alvallalkozo ->
                            appState.alvallalkozoState.getErtekbecslokOf(alvallalkozo).filter { !it.disabled }.forEach { eb ->
                                Option { attrs.value = eb.id; +eb.name }
                            }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertekbecsloDija
                    attrs.label = StringOrReactElement.fromString("Értékbecslő díja(Ft)")
                    MyNumberInput {
                        attrs.number = state.megrendeles.ertekbecsloDija?.toLong()
                        attrs.onValueChange = { value -> setState(state.copy(megrendeles = state.megrendeles.copy(ertekbecsloDija = value?.toInt()))) }
                    }

                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szamlazhatoDij
                    attrs.label = StringOrReactElement.fromString("Számlázható díj (Ft)")
                    MyNumberInput {
                        attrs.number = state.megrendeles.szamlazhatoDij?.toLong()
                        attrs.addonAfter = StringOrReactElement.from {
                            if (state.megrendeles.szamlazhatoDij != null) {
                                val afa = (state.megrendeles.szamlazhatoDij * 1.27).roundToLong()
                                +"+ ÁFA(27%) = ${parseGroupedStringToNum(afa.toString()).second}"
                            }
                        }
                        attrs.onValueChange = { value -> setState(state.copy(megrendeles = state.megrendeles.copy(szamlazhatoDij = value?.toInt()))) }
                    }

                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.required = state.megrendeles.munkatipus.isEnergetika()
                    val beillesztett = true
                    attrs.label = StringOrReactElement.fromString("Energetika Azonosító")
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else if (state.megrendeles.munkatipus.isEnergetika() && state.azonosito2.isEmpty()) ValidateStatus.error else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.etAzonosito
                        attrs.value = state.azonosito2
                        attrs.onChange = { e ->
                            setState(state.copy(azonosito2 = e.target.asDynamic().value as String? ?: ""))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Fővállalkozó")
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.fovallalkozo
                        attrs.value = state.megrendeles.foVallalkozo
                        attrs.onSelect = { value: String, option ->
                            setState(state.copy(megrendeles = state.megrendeles.copy(foVallalkozo = value)))
                        }
                        arrayOf("", "Presting Zrt.", "Viridis Kft.", "Estating Kft.").forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 8) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Megrendelés dátuma")
                    DatePicker {
                        attrs.allowClear = false
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelesDatuma
                        attrs.value = state.megrendeles.megrendelve?.let { moment(it) } ?: moment()
                        attrs.onChange = { date, str ->
                            if (date != null) {
                                setState(state.copy(megrendeles = state.megrendeles.copy(megrendelve = date)))
                            }
                        }
                    }
                }
            }
            Col(span = 8) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Határidő")
                    val beillesztett = true
                    attrs.hasFeedback = beillesztett
                    attrs.validateStatus = if (beillesztett) ValidateStatus.success else null
                    attrs.help = if (beillesztett) StringOrReactElement.from {
                        div {
                            attrs.jsStyle = jsStyle { color = "green" }
                            +"Beillesztett szövegből importálva"
                        }
                    } else null
                    DatePicker {
                        attrs.allowClear = false
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hatarido
                        attrs.value = state.megrendeles.hatarido?.let { moment(it) } ?: moment()
                        attrs.onChange = { date, str ->
                            if (date != null) {
                                setState(state.copy(megrendeles = state.megrendeles.copy(hatarido = date)))
                            }
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

fun munkatipusokForRegio(alvallalkozoState: AlvallalkozoState, regio: String): List<String> {
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