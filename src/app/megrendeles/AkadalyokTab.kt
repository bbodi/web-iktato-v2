package hu.nevermind.utils.app.megrendeles

import app.*
import app.common.Moment
import app.common.TimeUnit
import app.common.moment
import app.megrendeles.MegrendelesScreenIds
import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import kotlinx.html.js.onClickFunction
import org.w3c.dom.get
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.dom.a
import react.dom.b
import react.dom.div
import react.dom.jsStyle
import store.Action
import store.MegrendelesFromServer
import store.addMegrendelesExternalListener
import store.removeListener
import kotlin.browser.document


data class AkadalyokTabParams(val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                              val globalDispatch: (Action) -> Unit,
                              val megrendeles: Megrendeles,
                              val setFormState: Dispatcher<Megrendeles>?)

data class AkadalyokTabComponentState(val akadalyReason: Statusz?,
                                      val hataridoForAkadaly: Moment,
                                      val szovegesMagyarazat: String,
                                      val megjegyzes: String)

object AkadalyokTabComponent : DefinedReactComponent<AkadalyokTabParams>() {
    override fun RBuilder.body(props: AkadalyokTabParams) {
        val (tabState, setTabState) = useState(AkadalyokTabComponentState(
                hataridoForAkadaly = props.megrendeles.hatarido?.clone() ?: moment(),
                akadalyReason = null,
                szovegesMagyarazat = "",
                megjegyzes = props.megrendeles.megjegyzes
        ))
        if (props.onSaveFunctions.size > 2) {
            useEffect {
                props.onSaveFunctions[2] = { globalMegrendeles ->
                    globalMegrendeles.copy(
                            megjegyzes = tabState.megjegyzes
                    )
                }
            }
        }
        val setFormState = props.setFormState
        if (setFormState != null) {
            useEffectWithCleanup(RUN_ONLY_WHEN_MOUNT) {
                addMegrendelesExternalListener("AkadalyokTab") { megr ->
                    setFormState(props.megrendeles.copy(
                            akadalyok = megr.akadalyok,
                            hatarido = if (megr.statusz != Statusz.G4 && megr.statusz != Statusz.G6) {
                                megr.hatarido
                            } else props.megrendeles.hatarido,
                            statusz = megr.statusz
                    ))
                }
                val cleanup: () -> Unit = { removeListener("AkadalyokTab") }
                cleanup
            }
        }
        div {
            Collapse {
                attrs.defaultActiveKey = arrayOf("Akadály közlése", "Megjegyzés")
                if (props.megrendeles.akadalyok.isNotEmpty()) {
                    attrs.defaultActiveKey += arrayOf("Akadályok")
                }
                Panel("Akadályok") {
                    attrs.asDynamic().id = "akadalyokPanelHeader"
                    attrs.header = StringOrReactElement.fromString("Akadályok")
                    Row {
                        Col(span = 24) {
                            table(props.megrendeles.akadalyok)
                        }
                    }
                }
            }
            Row {
                Col(span = 12) {
                    Collapse {
                        attrs.defaultActiveKey = arrayOf("Akadály közlése")
                        Panel("Akadály közlése") {
                            attrs.header = StringOrReactElement.fromString("Akadály közlése")
                            Row {
                                Col(span = 12) {
                                    keslekedesOkaSelect(tabState, setTabState, props)
                                }
                            }
                            Row {
                                Col(span = 11) {
                                    jelenlegiHataridoField(props)
                                }
                                Col(offset = 1, span = 11) {
                                    if (tabState.akadalyReason != Statusz.G4 && tabState.akadalyReason != Statusz.G6) {
                                        javitasiHataridoField(tabState, setTabState)
                                    }
                                }
                            }
                            Row {
                                Col(span = 24) {
                                    szovegesMagyarazatField(tabState, setTabState)
                                }
                            }
                            Row {
                                Col(span = 24) {
                                    akadalyKozleseButton(tabState, setTabState, props)
                                }
                            }
                        }
                    }
                }
                Col(span = 12) {
                    Collapse {
                        attrs.defaultActiveKey = arrayOf("Megjegyzés")
                        Panel("Megjegyzés") {
                            attrs.header = StringOrReactElement.fromString("Megjegyzés")
                            FormItem {
                                attrs.label = StringOrReactElement.fromString("Megjegyzés")
                                TextArea {
                                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megjegyzes
                                    attrs.value = tabState.megjegyzes
                                    attrs.rows = 5
                                    attrs.onChange = { e ->
                                        setTabState(tabState.copy(
                                                megjegyzes = e.currentTarget.asDynamic().value
                                        ))
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.akadalyKozleseButton(tabState: AkadalyokTabComponentState,
                                                               setTabState: Dispatcher<AkadalyokTabComponentState>,
                                                               props: AkadalyokTabParams) {
        Button {
            attrs.asDynamic().id = MegrendelesScreenIds.modal.button.akadalyFeltoltes
            attrs.type = ButtonType.primary
            attrs.disabled = tabState.akadalyReason == null || props.megrendeles.id == 0
            attrs.onClick = {
                communicator.getEntityFromServer<Array<MegrendelesFromServer>, Unit>(RestUrl.akadalyKozles,
                        object {
                            val megrendelesId = props.megrendeles.id
                            val ujHatarido = tabState.hataridoForAkadaly.format(dateTimeFormat)
                            val akadalyOka = tabState.akadalyReason!!.text
                            val szoveg = tabState.szovegesMagyarazat
                        }) { response ->
                    props.globalDispatch(Action.MegrendelesekFromServer(response))
                    // don't even ask... It opens the first Panel so the table appears
                    // to the user
                    document.getElementById("akadalyokPanelHeader")?.children?.get(0)?.apply {
                        if (this.getAttribute("aria-expanded") == "false") {
                            this.asDynamic().click()
                        }
                    }
                    message.success("Akadály rögztíve")
                }
                setTabState(tabState.copy(
                        szovegesMagyarazat = "",
                        akadalyReason = null
                ))
            }
            +"Akadály közlése"
        }
    }

    private fun RElementBuilder<ColProps>.szovegesMagyarazatField(tabState: AkadalyokTabComponentState, setTabState: Dispatcher<AkadalyokTabComponentState>) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Szöveges magyarázat")
            TextArea {
                attrs.asDynamic().id = MegrendelesScreenIds.modal.akadaly.szovegesMagyarazat
                attrs.value = tabState.szovegesMagyarazat
                attrs.rows = 5
                attrs.onChange = { e ->
                    setTabState(tabState.copy(
                            szovegesMagyarazat = e.currentTarget.asDynamic().value
                    ))
                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.jelenlegiHataridoField(props: AkadalyokTabParams) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Jelenlegi határidő")
            DatePicker {
                attrs.allowClear = false
                attrs.disabled = true
                attrs.value = props.megrendeles.hatarido
            }
        }
    }

    private fun RElementBuilder<ColProps>.javitasiHataridoField(tabState: AkadalyokTabComponentState, setTabState: Dispatcher<AkadalyokTabComponentState>) {
        FormItem {
            attrs.label = StringOrReactElement.fromString("Javítási határidő")
            DatePicker {
                attrs.asDynamic().id = MegrendelesScreenIds.modal.akadaly.ujHatarido
                attrs.allowClear = false
                attrs.value = tabState.hataridoForAkadaly
                attrs.onChange = { date, str ->
                    if (date != null) {
                        setTabState(tabState.copy(
                                hataridoForAkadaly = date
                        ))
                    }
                }
            }
        }
    }

    private fun RElementBuilder<ColProps>.keslekedesOkaSelect(tabState: AkadalyokTabComponentState, setTabState: Dispatcher<AkadalyokTabComponentState>, props: AkadalyokTabParams) {
        FormItem {
            attrs.required = true
            attrs.label = StringOrReactElement.fromString("Késlekedés oka")
            Select {
                attrs.asDynamic().style = jsStyle { minWidth = 300 }
                attrs.asDynamic().id = MegrendelesScreenIds.modal.akadaly.keslekedesOka
                attrs.value = tabState.akadalyReason?.name
                attrs.onSelect = { statuszName: String, option ->
                    val statusz = Statusz.valueOf(statuszName)

                    val newHataridoOffset = when (statusz) {
                        Statusz.G1 -> 5
                        Statusz.G2 -> 5
                        Statusz.G3 -> 7
                        Statusz.G4 -> 0
                        Statusz.G5 -> 7
                        Statusz.G6 -> 0
                        else -> 0
                    }

                    setTabState(tabState.copy(
                            akadalyReason = statusz,
                            hataridoForAkadaly = (props.megrendeles.hatarido ?: moment())
                                    .clone().add(newHataridoOffset, TimeUnit.Days)
                    ))
                }
                arrayOf(Statusz.G1, Statusz.G2, Statusz.G3, Statusz.G4, Statusz.G5, Statusz.G6).forEach {
                    val akadalyStr = it.text
                    Option(it.name, akadalyStr)
                }
            }
        }
    }

    private fun RBuilder.table(akadalyok: Array<Akadaly>) {
        val columns = arrayOf(
                ColumnProps {
                    title = "Rögzítve"
                    dataIndex = "rogzitve"
                    width = 125
                    render = { cell: Moment?, _, _ ->
                        buildElement {
                            +(cell?.format(dateTimeFormat) ?: "")
                        }
                    }
                },
                ColumnProps {
                    title = "Új határidő"
                    dataIndex = "ujHatarido"
                    render = { cell: Moment?, _, _ ->
                        buildElement {
                            +(cell?.format(dateFormat) ?: "")
                        }
                    }
                    width = 75
                },
                ColumnProps {
                    title = "Státusz"
                    dataIndex = "statusz"
                    width = 75
                    render = { cell: Statusz, _, _ ->
                        buildElement {
                            +cell.text
                        }
                    }
                },
                ColumnProps {
                    title = "Leírás"
                    dataIndex = "leiras"
                    width = 175
                    render = { cell: String, record: Akadaly, _ ->
                        buildElement {
                            if (cell.length > 50) {
                                a(href = null) {
                                    attrs.onClickFunction = {
                                        Modal.info {
                                            title = "Rögzítve: ${record.rogzitve.format(dateTimeFormat)}"
                                            content = StringOrReactElement.from {
                                                div {
                                                    attrs.jsStyle = jsStyle {
                                                        whiteSpace = "pre-wrap"
                                                    }
                                                    +cell
                                                }
                                            }
                                        }
                                    }
                                    +(cell.substring(0, 50))
                                    b { +"..." }
                                }
                            } else +cell
                        }
                    }
                }
        )

        Table {
            attrs.columns = columns
            attrs.dataSource = akadalyok
            attrs.rowKey = "id"
            attrs.onRow = { account ->
                jsObject {
                    //this.asDynamic().onClick = { globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal((account as Account).id))) }
                }
            }
            attrs.asDynamic().size = "small"
        }
    }
}