package hu.nevermind.utils.app.megrendeles

import app.Dispatcher
import app.common.Moment
import app.common.moment
import app.megrendeles.MegrendelesFormState
import app.megrendeles.MegrendelesScreenIds
import app.useEffect
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.Statusz
import react.RBuilder


data class ZarolasTabParams(val formState: MegrendelesFormState,
                            val setFormState: Dispatcher<MegrendelesFormState>,
                            val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>)


object ZarolasTabComponent : DefinedReactComponent<ZarolasTabParams>() {
    override fun RBuilder.body(props: ZarolasTabParams) {
        val (tabState, setTabState) = useState(props.formState.megrendeles.copy())
        useEffect {
            props.onSaveFunctions[5] = { globalMegrendeles ->
                globalMegrendeles.copy(
                        feltoltveMegrendelonek = tabState.feltoltveMegrendelonek,
                        zarolva = tabState.zarolva
                )
            }
        }
        Collapse {
            attrs.bordered = false
            attrs.defaultActiveKey = arrayOf("Megrendelés", "Dátumok")
            Panel("Megrendelés") {
                attrs.header = StringOrReactElement.fromString("Megrendelés")
                megrPanel(tabState, props.formState, props.setFormState)
            }
            Panel("Dátumok") {
                attrs.header = StringOrReactElement.fromString("Dátumok")
                datumPanel(tabState, setTabState)
            }
        }
    }
}

private fun RBuilder.megrPanel(tabState: Megrendeles,
                               formState: MegrendelesFormState,
                               setFormState: Dispatcher<MegrendelesFormState>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Státusz")
                Select {
                    attrs.asDynamic().style = jsStyle { minWidth = 100 }
                    attrs.value = formState.megrendeles.statusz.name
                    attrs.onSelect = { value, option ->
                        setFormState(formState.copy(megrendeles = formState.megrendeles.copy(
                                statusz = Statusz.valueOf(value)
                        )))
                    }
                    Statusz.values().forEach { status ->
                        Option { attrs.value = status.name; +(status.name) }
                    }
                }
            }
        }
        Col(offset = 1, span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Megrendelés átvétel ideje")
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = true
                    attrs.placeholder = ""
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelesAtvetelIdeje
                    attrs.value = tabState.megrendelesMegtekint
                }
            }
        }
    }
}

private fun RBuilder.datumPanel(megrendeles: Megrendeles, setState: Dispatcher<Megrendeles>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Feltöltve megrendelőnek")
                Checkbox {
                    attrs.checked = megrendeles.feltoltveMegrendelonek != null
                    attrs.onChange = { checked ->
                        setState(megrendeles.copy(
                                feltoltveMegrendelonek = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = megrendeles.feltoltveMegrendelonek == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.feltoltveMegrendelonek
                    attrs.value = megrendeles.feltoltveMegrendelonek
                    attrs.onChange = { date, str ->
                        val newStatus = determineStatusz(date, megrendeles.zarolva, megrendeles.statusz)
                        if (date != null) {
                            setState(megrendeles.copy(
                                    statusz = newStatus,
                                    feltoltveMegrendelonek = date
                            ))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Zárolva")
                Checkbox {
                    attrs.checked = megrendeles.zarolva != null
                    attrs.onChange = { checked ->
                        setState(megrendeles.copy(
                                zarolva = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = megrendeles.zarolva == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.zarolva
                    attrs.value = megrendeles.zarolva
                    attrs.onChange = { date, str ->
                        val newStatus = determineStatusz(date, megrendeles.zarolva, megrendeles.statusz)
                        if (date != null) {
                            setState(megrendeles.copy(
                                    statusz = newStatus,
                                    zarolva = date
                            ))
                        }
                    }
                }
            }
        }
    }
}

private fun determineStatusz(feltoltveDate: Moment?, zarolvaDate: Moment?, statusz: Statusz): Statusz {
    return if (zarolvaDate != null) {
        Statusz.D1
    } else if (feltoltveDate != null) {
        Statusz.C1
    } else {
        statusz
    }
}