package hu.nevermind.utils.app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.moment
import app.megrendeles.MegrendelesFormState
import app.megrendeles.MegrendelesScreenIds
import app.useEffect
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.ingatlanBovebbTipusaArray
import react.RBuilder
import react.children

data class IngatlanAdataiTabParams(val formState: MegrendelesFormState,
                                   val appState: AppState,
                                   val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                                   val setState: Dispatcher<MegrendelesFormState>)

object IngatlanAdataiTabComponent : DefinedReactComponent<IngatlanAdataiTabParams>() {
    override fun RBuilder.body(props: IngatlanAdataiTabParams) {
        val (tabState, setTabState) = useState(props.formState.megrendeles.copy())
        useEffect {
            props.onSaveFunctions[1] = { globalMegrendeles ->
                globalMegrendeles.copy(
                        szemleIdopontja = tabState.szemleIdopontja,
                        helyszinelo = tabState.helyszinelo,
                        ingatlanBovebbTipus = tabState.ingatlanBovebbTipus,
                        keszultsegiFok = tabState.keszultsegiFok,
                        lakasTerulet = tabState.lakasTerulet,
                        telekTerulet = tabState.telekTerulet,
                        becsultErtek = tabState.becsultErtek,
                        eladasiAr = tabState.eladasiAr,
                        fajlagosBecsultAr = tabState.fajlagosBecsultAr,
                        fajlagosEladAr = tabState.fajlagosEladAr,
                        adasvetelDatuma = tabState.adasvetelDatuma,
                        hetKod = tabState.hetKod
                )
            }
        }
        Collapse {
            attrs.bordered = false
            attrs.defaultActiveKey = arrayOf("Helyszínelés", "Ingatlan")
            Panel("Helyszínelés") {
                attrs.header = StringOrReactElement.fromString("Helyszínelés")
                helyszinelesPanel(tabState, props.appState, setTabState)
            }
            Panel("Ingatlan") {
                attrs.header = StringOrReactElement.fromString("Ingatlan")
                ingatlanPanel(tabState, setTabState)
            }
        }
    }
}


private fun RBuilder.helyszinelesPanel(tabState: Megrendeles, appState: AppState, setTabState: Dispatcher<Megrendeles>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Szemle időpontja")
                Checkbox {
                    attrs.checked = tabState.szemleIdopontja != null
                    attrs.onChange = { checked ->
                        setTabState(tabState.copy(
                                szemleIdopontja = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = tabState.szemleIdopontja == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szemleIdopontja
                    attrs.value = tabState.szemleIdopontja
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setTabState(tabState.copy(szemleIdopontja = date))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Helyszínelő")
                val alv = appState.alvallalkozoState.alvallalkozok[tabState.alvallalkozoId]
                val source = (if (alv != null) {
                    appState.alvallalkozoState.getErtekbecslokOf(alv)
                            .filter { !it.disabled }
                            .map { it.name }
                            .filter { it.isNotEmpty() }
                } else emptyList()).toTypedArray()
                val helpMsg = if (alv != null &&
                        appState.alvallalkozoState.getErtekbecslokOf(alv)
                                .filter { !it.disabled }
                                .none { eb -> eb.name == tabState.helyszinelo })
                    "A megadott név nem szerepel a választható Értékbecslők között"
                else
                    null
                attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                attrs.hasFeedback = helpMsg != null
                attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                AutoComplete(source) {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.helyszinelo
                    attrs.value = tabState.helyszinelo ?: ""
                    attrs.placeholder = "Helyszínelő"
                    attrs.filterOption = { inputString, optionElement ->
                        (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                    }
                    attrs.onChange = { value ->
                        setTabState(tabState.copy(helyszinelo = value))
                    }
                }
            }
        }
    }
}

private fun RBuilder.ingatlanPanel(tabState: Megrendeles, setTabState: Dispatcher<Megrendeles>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Ingatlan bővebb típus")
                Select {
                    attrs.asDynamic().style = jsStyle { minWidth = 300 }
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ingatlanBovebbTipus
                    attrs.value = tabState.ingatlanBovebbTipus
                    attrs.onSelect = { value, option ->
                        setTabState(tabState.copy(
                                ingatlanBovebbTipus = value
                        ))
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
                    attrs.number = tabState.keszultsegiFok?.toLong()
                    attrs.onValueChange = { value ->
                        setTabState(tabState.copy(
                                keszultsegiFok = value?.toInt()
                        ))
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
                    attrs.number = tabState.lakasTerulet?.toLong()
                    attrs.onValueChange = { value ->
                        val (fajlagosBecsultErtek, fajlagosEladasiAr) =
                                if (tabState.ingatlanBovebbTipus != "beépítetlen terület") {
                                    if (value != null && value > 0) {
                                        (tabState.becsultErtek ?: 0).div(value.toInt()) to
                                                (tabState.eladasiAr ?: 0).div(value.toInt())
                                    } else {
                                        tabState.fajlagosBecsultAr to
                                                tabState.fajlagosEladAr
                                    }
                                } else {
                                    tabState.fajlagosBecsultAr to
                                            tabState.fajlagosEladAr
                                }

                        setTabState(tabState.copy(
                                lakasTerulet = value?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek,
                                fajlagosEladAr = fajlagosEladasiAr
                        ))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Telek terület (m²)")
                MyNumberInput {
                    attrs.number = tabState.telekTerulet?.toLong()
                    attrs.onValueChange = { value ->
                        val (fajlagosBecsultErtek, fajlagosEladasiAr) =
                                if (tabState.ingatlanBovebbTipus == "beépítetlen terület") {
                                    if (value != null && value > 0) {
                                        (tabState.becsultErtek ?: 0).div(value.toInt()) to
                                                (tabState.eladasiAr ?: 0).div(value.toInt())
                                    } else {
                                        tabState.fajlagosBecsultAr to
                                                tabState.fajlagosEladAr
                                    }
                                } else {
                                    tabState.fajlagosBecsultAr to
                                            tabState.fajlagosEladAr
                                }

                        setTabState(tabState.copy(
                                telekTerulet = value?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek,
                                fajlagosEladAr = fajlagosEladasiAr
                        ))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Becsült érték (Ft)")
                MyNumberInput {
                    attrs.number = tabState.becsultErtek?.toLong()
                    attrs.onValueChange = { becsultErtek ->
                        val terulet = if (tabState.ingatlanBovebbTipus == "beépítetlen terület")
                            tabState.telekTerulet
                        else
                            tabState.lakasTerulet
                        val fajlagosBecsultErtek = if (terulet != null && terulet > 0) {
                            (becsultErtek ?: 0).div(terulet)
                        } else null

                        setTabState(tabState.copy(
                                becsultErtek = becsultErtek?.toInt(),
                                fajlagosBecsultAr = fajlagosBecsultErtek?.toInt()
                        ))
                    }
                }
            }
        }
        Col(offset = 1, span = 5) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Eladási ár (Ft)")
                MyNumberInput {
                    attrs.number = tabState.eladasiAr?.toLong()
                    attrs.onValueChange = { eladasiAr ->
                        val terulet = if (tabState.ingatlanBovebbTipus == "beépítetlen terület")
                            tabState.telekTerulet
                        else
                            tabState.lakasTerulet
                        val fajlagosEladasiAr = if (terulet != null && terulet > 0) {
                            (eladasiAr ?: 0).div(terulet)
                        } else null
                        setTabState(tabState.copy(
                                eladasiAr = eladasiAr?.toInt(),
                                fajlagosEladAr = fajlagosEladasiAr?.toInt()
                        ))
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Fajlagos becsült ár (Ft)")
                MyNumberInput {
                    attrs.number = tabState.fajlagosBecsultAr?.toLong()
                    attrs.onValueChange = { value ->
                        setTabState(tabState.copy(
                                fajlagosBecsultAr = value?.toInt()
                        ))
                    }
                }
            }
        }
        Col(offset = 1, span = 7) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Fajlagos eladási ár (Ft)")
                MyNumberInput {
                    attrs.number = tabState.fajlagosEladAr?.toLong()
                    attrs.onValueChange = { value ->
                        setTabState(tabState.copy(
                                fajlagosEladAr = value?.toInt()
                        ))
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
                    attrs.checked = tabState.adasvetelDatuma != null
                    attrs.onChange = { checked ->
                        setTabState(tabState.copy(
                                adasvetelDatuma = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = tabState.adasvetelDatuma == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.adasvetelDatuma
                    attrs.value = tabState.adasvetelDatuma
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setTabState(tabState.copy(adasvetelDatuma = date))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("HET kód")
                Input {
                    attrs.value = tabState.hetKod ?: ""
                    attrs.onChange = { e ->
                        setTabState(tabState.copy(
                                hetKod = e.currentTarget.asDynamic().value
                        ))
                    }
                }
            }
        }

    }
}
