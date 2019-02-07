package app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.Moment
import app.common.TimeUnit
import app.common.moment
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.app.megrendeles.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import react.RBuilder
import react.RElementBuilder
import react.ReactElement
import react.buildElement
import react.dom.*
import store.Action
import store.diff

data class MegrendelesFormState(val activeTab: String,
                                val importTextAreaVisible: Boolean,
                                val importedText: String,
                                val importedTextModified: Moment?,
                                val megrendeles: Megrendeles,
                                val megrendelesFieldsFromExcel: MegrendelesFieldsFromExternalSource? = null
)

data class MegrendelesFormScreenParams(val megrendeles: Megrendeles?,
                                       val appState: AppState,
                                       val globalDispatch: (Action) -> Unit)

object MegrendelesFormScreenComponent : DefinedReactComponent<MegrendelesFormScreenParams>() {
    override fun RBuilder.body(props: MegrendelesFormScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val megrendeles = if (props.megrendeles == null)
            Megrendeles(
                    megrendelo = appState.sajatArState.allMegrendelo.first(),
                    foVallalkozo = "Presting Zrt.",
                    munkatipus = Munkatipusok.Ertekbecsles.str,
                    ingatlanTipusMunkadijMeghatarozasahoz = appState.sajatArState.getSajatArakFor("Presting Zrt.", Munkatipusok.Ertekbecsles.str).firstOrNull()?.leiras
                            ?: "",
                    hitelTipus = "vásárlás",
                    ingatlanBovebbTipus = "lakás",
                    hatarido = getNextWeekDay(5),
                    ellenorizve = true,
                    statusz = Statusz.B1
            ) else props.megrendeles.copy()
        val (state, setState) = useState(MegrendelesFormState(
                activeTab = MegrendelesScreenIds.modal.tab.first,
                importedText = "",
                importedTextModified = null,
                importTextAreaVisible = false,
                megrendeles = megrendeles
        ))
        val (onSaveFunctions, _) = useState(Array<(Megrendeles) -> Megrendeles>(6) { size -> { megr -> megr } })
        div {
            Row {
                val leftSideSpan = if (state.importTextAreaVisible) 9 else 6
                Col(span = leftSideSpan) {
                    Affix {
                        attrs.offsetTop = 30
                        if (state.megrendeles.id == 0) {
                            Row { Col(span = 24) { br {} }/* empty row so the text area is aligned with the form next to it*/ }
                            Row { Col(span = 24) { br {} }/* empty row so the text area is aligned with the form next to it*/ }
                            Row { Col(span = 24) { br {} }/* empty row so the text area is aligned with the form next to it*/ }
                            Row { Col(span = 24) { br {} }/* empty row so the text area is aligned with the form next to it*/ }
                            Row {
                                Col(span = 24) {
                                    if (state.importTextAreaVisible) {
                                        importEmailTextArea(state, setState)
                                    } else {
                                        importEmailButton(state, setState)
                                    }
                                }
                            }
                        } else {
                            h1 {
                                +state.megrendeles.azonosito
                            }
                            steps(state.megrendeles, appState)
                        }
                    }
                }
                Col(span = 24 - leftSideSpan) {
                    Row {
                        val offsetSize = if (state.importTextAreaVisible) 0 else 12
                        val spanSize = if (state.importTextAreaVisible) 16 else 8
                        Col(offset = offsetSize, span = spanSize) {
                            if (megrendeles.id != 0) {
                                resendEmailButton(state)
                            } else {
                                ellenorizveCheckbox(state, setState)
                            }
                        }
                        Col(span = 24 - (offsetSize + spanSize)) {
                            saveAndBackButton(onSaveFunctions, state, globalDispatch)
                        }
                    }
                    Row {
                        Col(span = 24) {
                            megrendelesForm(state, appState, setState, onSaveFunctions, globalDispatch)
                        }
                    }
                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.importEmailButton(state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
        Button {
            attrs.asDynamic().id = MegrendelesScreenIds.addButton
            attrs.type = ButtonType.default
            attrs.onClick = {
                setState(state.copy(
                        importTextAreaVisible = true
                ))
            }
            Icon("mail")
            +" Importálás szövegből"
        }
    }

    private fun RElementBuilder<ColProps>.importEmailTextArea(state: MegrendelesFormState,
                                                              setState: Dispatcher<MegrendelesFormState>) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Importálni kívánt e-mail szövege")
            TextArea {
                attrs.value = state.importedText
                attrs.rows = 30
                attrs.onChange = { e ->
                    val newValue = e.currentTarget.asDynamic().value
                    setState(state.copy(
                            importedText = newValue,
                            importedTextModified = moment()
                    ))
                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.ellenorizveCheckbox(state: MegrendelesFormState, setState: Dispatcher<MegrendelesFormState>) {
        Form {
            FormItem {
                attrs.labelCol = ColProperties { span = 20 }
                attrs.wrapperCol = ColProperties { span = 4 }
                attrs.label = StringOrReactElement.fromString("E-mail küldése az alvállalkozónak")
                Checkbox {
                    attrs.checked = state.megrendeles.ellenorizve
                    attrs.onChange = { checked ->
                        setState(state.copy(megrendeles = state.megrendeles.copy(
                                ellenorizve = checked
                        )))
                    }
                }
            }

        }
    }

    private fun RBuilder.steps(megr: Megrendeles, appState: AppState) {
        val current = if (megr.zarolva != null || megr.feltoltveMegrendelonek != null) {
            5
        } else if (megr.penzBeerkezettDatum != null || megr.keszpenzesBefizetes != null) {
            4
        } else if (megr.alvallalkozoFeltoltotteFajlokat) {
            3
        } else if (megr.szemleIdopontja != null) {
            2
        } else if (megr.megrendelesMegtekint != null) {
            1
        } else 0
        Steps {
            attrs.current = current
            attrs.status = if (megr.isAkadalyos()) {
                StepsStatus.error
            } else if (current == 5 && megr.zarolva != null) {
                StepsStatus.finish
            } else {
                StepsStatus.process
            }
            attrs.size = "small"
            attrs.direction = StepsDirection.vertical
            Step {
                attrs.title = StringOrReactElement.fromString("Átvett")
                val avNev = appState.alvallalkozoState.alvallalkozok[megr.alvallalkozoId]?.name ?: ""
                attrs.description = if (megr.megrendelesMegtekint != null)
                    StringOrReactElement.fromString("${megr.megrendelesMegtekint.format(dateFormat)} - $avNev")
                else null
            }
            Step {
                attrs.title = StringOrReactElement.fromString("Szemle")
                attrs.description = if (megr.szemleIdopontja != null)
                    StringOrReactElement.fromString(
                            "${megr.szemleIdopontja.format(dateFormat)} - ${megr.helyszinelo ?: ""}"
                    )
                else null
            }
            Step {
                attrs.title = StringOrReactElement.fromString("Fájlok")
                listOf(megr.ertekbecslesFeltoltve, megr.energetikaFeltoltve).filterNotNull().map {

                }
                attrs.description = if (megr.alvallalkozoFeltoltotteFajlokat) StringOrReactElement.from {
                    div {
                        if (megr.ertekbecslesFeltoltve != null) {
                            +"${megr.ertekbecslesFeltoltve.format(dateFormat)} - Értékbecslés feltöltve"
                        }
                        if (megr.energetikaFeltoltve != null) {
                            if (megr.ertekbecslesFeltoltve != null) {
                                br { }
                            }
                            +"${megr.energetikaFeltoltve.format(dateFormat)} - Energetika feltöltve"
                        }
                    }
                } else null
            }
            Step {
                attrs.title = StringOrReactElement.fromString("Utalás")
                val date = megr.penzBeerkezettDatum ?: megr.keszpenzesBefizetes
                attrs.description = if (date != null) {
                    StringOrReactElement.fromString(date.format(dateFormat))
                } else null

            }
            Step {
                attrs.title = StringOrReactElement.fromString("Ellenőrizve")
                attrs.description = if (megr.feltoltveMegrendelonek != null)
                    StringOrReactElement.fromString(
                            "${megr.feltoltveMegrendelonek.format(dateFormat)} - Feltöltve megrendelőnek"
                    )
                else null
            }
            Step {
                attrs.title = StringOrReactElement.fromString("Archiválva")
                attrs.description = if (megr.zarolva != null)
                    StringOrReactElement.fromString(megr.zarolva.format(dateFormat))
                else null
            }
        }
    }

    private fun RElementBuilder<ColProps>.resendEmailButton(state: MegrendelesFormState) {
        Button {
            attrs.type = ButtonType.default
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

    private fun RElementBuilder<ColProps>.saveAndBackButton(onSaveFunctions: Array<(Megrendeles) -> Megrendeles>, state: MegrendelesFormState, globalDispatch: (Action) -> Unit) {
        Affix {
            attrs.offsetTop = 30
            attrs.asDynamic().className = "affix-hack-noinline"
            Button {
                attrs.asDynamic().id = MegrendelesScreenIds.modal.button.save
                attrs.type = ButtonType.primary
                attrs.onClick = {
                    val finalMegrendeles = onSaveFunctions.fold(state.megrendeles) { megrendeles, onSaveFunctionOfTab ->
                        onSaveFunctionOfTab(megrendeles)
                    }
                    val diffs = diff(state.megrendeles, finalMegrendeles)
                    diffs?.forEach { diffNode ->
                        if (diffNode.kind[0] == 'E') {
                            console.info(" ${diffNode.path[0]}: ${diffNode.lhs} -> ${diffNode.rhs}")
                        }
                    }
                }
                +"Mentés"
            }
            Button {
                attrs.asDynamic().id = MegrendelesScreenIds.modal.button.close
                attrs.type = ButtonType.danger
                attrs.onClick = {
                    globalDispatch(Action.ChangeURL(Path.megrendeles.root))
                }
                +"Vissza"
            }
        }
    }
}

private fun RBuilder.megrendelesForm(state: MegrendelesFormState,
                                     appState: AppState,
                                     setFormState: Dispatcher<MegrendelesFormState>,
                                     onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                                     globalDispatch: (Action) -> Unit) {
    Tabs {
        attrs.activeKey = state.activeTab
        attrs.onChange = { key ->
            val finalMegrendeles = onSaveFunctions.fold(state.megrendeles) { megrendeles, onSaveFunctionOfTab ->
                onSaveFunctionOfTab(megrendeles)
            }
            setFormState(state.copy(
                    activeTab = key,
                    megrendeles = finalMegrendeles
            ))
        }
        TabPane {
            attrs.key = MegrendelesScreenIds.modal.tab.first
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Alap adatok", color = "black", icon = "list-alt"))
            AlapAdatokTabComponent.insert(this, AlapAdatokTabParams(
                    state.megrendeles,
                    state.importedTextModified,
                    state.importedText,
                    state.megrendelesFieldsFromExcel,
                    appState,
                    onSaveFunctions,
                    setFormState
            ))
        }
        TabPane {
            attrs.key = MegrendelesScreenIds.modal.tab.ingatlanAdatai
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Ingatlan adatai", color = "red", icon = "home"))
            IngatlanAdataiTabComponent.insert(this, IngatlanAdataiTabParams(state, appState, onSaveFunctions, setFormState))
        }
        TabPane {
            attrs.key = MegrendelesScreenIds.modal.tab.akadalyok
            val akadalyok = state.megrendeles.akadalyok
            val akadalyokSize = maxOf(akadalyok.size, if (state.megrendeles.megjegyzes.isNotEmpty()) 1 else 0)
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Megjegyzés/Akadály", color = "black", icon = "message", badgeNum = akadalyokSize))
            AkadalyokTabComponent.insert(this,
                    AkadalyokTabParams(state.megrendeles.hatarido,
                            onSaveFunctions, globalDispatch,
                            state,
                            setFormState))
        }
        TabPane {
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Fájlok", color = "black", icon = "upload", badgeNum = state.megrendeles.files.size))
            FajlokTabComponent.insert(this, FajlokTabParams(state,
                    onSaveFunctions,
                    globalDispatch,
                    appState,
                    setFormState,
                    appState.alvallalkozoState))
        }
        TabPane {
            attrs.key = MegrendelesScreenIds.modal.tab.penzBeerkezett
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Pénz beérkezett", color = "black", icon = "dollar"))
            PenzBeerkezettTabComponent.insert(this, PenzBeerkezettTabParams(state.megrendeles, onSaveFunctions))
        }
        TabPane {
            attrs.key = MegrendelesScreenIds.modal.tab.feltoltesZarolas
            attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Feltöltés/Zárolás", color = "black", icon = "file-done"))
            ZarolasTabComponent.insert(this, ZarolasTabParams(
                    state,
                    setFormState,
                    onSaveFunctions))
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
            Badge(badgeNum) {
                attrs.asDynamic().style = jsStyle {
                    backgroundColor = "#1890ff"
                }
            }
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