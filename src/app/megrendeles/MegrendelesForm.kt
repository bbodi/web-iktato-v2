package hu.nevermind.iktato.component.megrendeles

import app.megrendeles.MegrendelesScreenIds
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.store.Megrendeles
import kotlinext.js.jsObject
import react.*
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.megyek

private data class MegrendelesFormState(val activeTab: String,
                                        val megrendeles: Megrendeles)

fun megrendelesForm(megrendeles: Megrendeles): ReactElement {
    return createElement(type = { props: dynamic ->
        val megrendeles: Megrendeles = props.megrendeles
        val (state, setState) = useState(MegrendelesFormState(
                activeTab = MegrendelesScreenIds.modal.tab.first,
                megrendeles = megrendeles
        ))
        buildElement {
            Tabs {
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.first
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Alap adatok", color = "black", icon = "list-alt"))
                    Form {
                        Row(gutter = 24) {
                            Col(span = 8) {
                                FormItem {
                                    attrs.required = true
                                    attrs.label = StringOrReactElement.fromString("Megrendelő")
                                    Select {
                                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelo
                                        attrs.defaultValue = megrendeles.megrendelo
                                        attrs.onSelect = { value, option ->
                                            val newMegrendelo = value
                                            TODO()
//                                    setMegrendelo(self, self.state.megrendeles, newMegrendelo) { newState ->
//                                        self.setState(newState)
//                                    }
                                        }
                                        // TODO: sajat ár store
//                                SajatArStore.allMegrendelo.forEach { megrendeloName ->
//                                    option({ value = megrendeloName }) {
//                                        text(megrendeloName)
//                                    }
//                                }
                                        Option { attrs.value = "Fundamenta"; +"Fundamenta" }
                                    }
                                }
                            }
                            Col(span = 8) {
                                FormItem {
                                    attrs.required = true
                                    attrs.label = StringOrReactElement.fromString("Régió")
                                    AutoComplete(megyek.asDynamic()) {
                                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.regio
                                        attrs.placeholder = "Megye"
                                        attrs.filterOption = { inputString, optionElement ->
                                            (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                                        }
                                        attrs.onSelect = { selectedName ->
                                            TODO()
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
                                        attrs.onSelect = { value, option ->
                                            TODO()
                                        }
                                        Option { attrs.value = "Értékbecslés"; +"Értékbecslés" }
                                        Option { attrs.value = "Energetika"; +"Energetika" }
                                        Option { attrs.value = "Értékbecslés&Energetika"; +"Értékbecslés&Energetika" }
                                    }
                                }
                            }
                        }
                    }
//                }
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.ingatlanAdatai
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Ingatlan adatai", color = "red", icon = "home"))
                }
            }
        }
    }, props = jsObject<dynamic> {
        this.megrendeles = megrendeles
    })
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