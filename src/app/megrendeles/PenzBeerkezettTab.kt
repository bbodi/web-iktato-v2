package hu.nevermind.utils.app.megrendeles

import app.*
import app.common.moment
import app.megrendeles.MegrendelesScreenIds
import hu.nevermind.antd.*
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Megrendeles
import react.RBuilder
import store.addMegrendelesExternalListener
import store.removeListener


data class PenzBeerkezettTabParams(val megrendeles: Megrendeles,
                                   val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>)


object PenzBeerkezettTabComponent : DefinedReactComponent<PenzBeerkezettTabParams>() {
    override fun RBuilder.body(props: PenzBeerkezettTabParams) {
        val (tabState, setTabState) = useState(props.megrendeles.copy())
        useEffectWithCleanup(RUN_ONLY_WHEN_MOUNT) {
            addMegrendelesExternalListener("FajlokTab") { megr ->
                setTabState(
                        props.megrendeles.copy(
                                keszpenzesBefizetes = megr.keszpenzesBefizetes,
                                penzBeerkezettDatum = megr.penzBeerkezettDatum,
                                szamlaSorszama = megr.szamlaSorszama
                        )
                )
            }
            val cleanup: () -> Unit = { removeListener("FajlokTab") }
            cleanup
        }

        useEffect {
            props.onSaveFunctions[4] = { globalMegrendeles ->
                globalMegrendeles.copy(
                        keszpenzesBefizetes = tabState.keszpenzesBefizetes,
                        penzBeerkezettDatum = tabState.penzBeerkezettDatum,
                        szamlaSorszama = tabState.szamlaSorszama
                )
            }
        }
        Collapse {
            attrs.bordered = false
            attrs.defaultActiveKey = arrayOf("Számla")
            Panel("Számla") {
                attrs.header = StringOrReactElement.fromString("Számla")
                szamlaPanel(tabState, setTabState)
            }
        }
    }
}

private fun RBuilder.szamlaPanel(megrendeles: Megrendeles, setState: Dispatcher<Megrendeles>) {
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Készpénzes befizetés")
                Checkbox {
                    attrs.checked = megrendeles.keszpenzesBefizetes != null
                    attrs.onChange = { checked ->
                        setState(megrendeles.copy(
                                keszpenzesBefizetes = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = megrendeles.keszpenzesBefizetes == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.keszpenzesKifizetes
                    attrs.value = megrendeles.keszpenzesBefizetes
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setState(megrendeles.copy(keszpenzesBefizetes = date))
                        }
                    }
                }
            }
        }
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Pénz beérkezett számlára")
                Checkbox {
                    attrs.checked = megrendeles.penzBeerkezettDatum != null
                    attrs.onChange = { checked ->
                        setState(megrendeles.copy(
                                penzBeerkezettDatum = if (checked) moment() else null
                        ))
                    }
                }
                DatePicker {
                    attrs.allowClear = false
                    attrs.disabled = megrendeles.penzBeerkezettDatum == null
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.penzBeerkezettSzamlara
                    attrs.value = megrendeles.penzBeerkezettDatum
                    attrs.onChange = { date, str ->
                        if (date != null) {
                            setState(megrendeles.copy(penzBeerkezettDatum = date))
                        }
                    }
                }
            }
        }
    }
    Row {
        Col(span = 8) {
            FormItem {
                attrs.label = StringOrReactElement.fromString("Számla sorszáma")
                Input {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szamlaSorszama
                    attrs.value = megrendeles.szamlaSorszama
                    attrs.onChange = { e ->
                        setState(megrendeles.copy(
                                szamlaSorszama = e.currentTarget.asDynamic().value
                        ))
                    }
                }
            }
        }
    }
}