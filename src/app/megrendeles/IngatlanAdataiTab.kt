package hu.nevermind.utils.app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.moment
import app.megrendeles.MegrendelesFormState
import app.megrendeles.MegrendelesScreenIds
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.ingatlanBovebbTipusaArray
import react.RBuilder
import react.children


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

private fun RBuilder.helyszinelesPanel(state: MegrendelesFormState, appState: AppState, setState: Dispatcher<MegrendelesFormState>) {
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

private fun RBuilder.ingatlanPanel(state: MegrendelesFormState, appState: AppState, setState: Dispatcher<MegrendelesFormState>) {
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
