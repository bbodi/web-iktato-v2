package hu.nevermind.utils.app.megrendeles

import app.Dispatcher
import app.megrendeles.downloadFile
import app.useEffect
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import kotlinx.html.js.onClickFunction
import org.w3c.files.File
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.dom.*
import store.Action
import store.UploadData
import kotlin.js.Promise


data class FajlokTabParams(val megrendeles: Megrendeles,
                           val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                           val globalDispatch: (Action) -> Unit)

data class FajlokTabComponentState(val uploadModalVisible: Boolean,
                                   val megrendeles: Megrendeles,
                                   val tartalmazEnergetika: Boolean?,
                                   val tartalmazErtekbecsles: Boolean?,
                                   val uploadingFiles: List<File>)

object FajlokTabComponent : DefinedReactComponent<FajlokTabParams>() {
    override fun RBuilder.body(props: FajlokTabParams) {
        val (tabState, setTabState) = useState(FajlokTabComponentState(
                uploadModalVisible = false,
                tartalmazEnergetika = null,
                tartalmazErtekbecsles = null,
                megrendeles = props.megrendeles.copy(),
                uploadingFiles = emptyList()
        ))
        useEffect {
            props.onSaveFunctions[3] = { globalMegrendeles ->
                globalMegrendeles.copy(
//                        keszpenzesBefizetes = tabState.keszpenzesBefizetes,
                )
            }
        }
        div {
            Collapse {
                attrs.bordered = false
                attrs.defaultActiveKey = arrayOf("Fájl feltöltés", "Feltöltött fájlok")
                Panel("Fájl feltöltés") {
                    attrs.header = StringOrReactElement.fromString("Fájl feltöltés")
                    feltoltesPanel(tabState, setTabState)
                }
                Panel("Feltöltött fájlok") {
                    attrs.header = StringOrReactElement.fromString("Feltöltött fájlok")
                    Row {
                        Col(offset = 5, span = 14) {
                            fileTable(tabState)
                        }
                    }
                }
            }
            uploadModal(tabState, setTabState, props.globalDispatch)
        }
    }

    private fun RBuilder.fileTable(tabState: FajlokTabComponentState) {
        val columns = arrayOf(
                ColumnProps {
                    title = "Név"
                    dataIndex = "name"
                    width = 100
                },
                ColumnProps {
                    title = "Méret"
                    dataIndex = "humanReadableSize"
                    align = ColumnAlign.right
                    width = 50
                    render = { cell: String, _ ->
                        buildElement {
                            span {
                                attrs.jsStyle = jsStyle {
                                    fontFamily = """"Lucida Console", Monaco, monospace"""
                                }
                                +cell
                            }
                        }
                    }
                },
                ColumnProps {
                    title = ""
                    key = "action"
                    align = ColumnAlign.right
                    width = 150
                    render = { _, record: FileData ->
                        buildElement {
                            span {
                                a(href = null) {
                                    attrs.onClickFunction = {
                                        val x = XMLHttpRequest()
                                        val str = record.name
                                        val encodedFilename = js("btoa(encodeURIComponent(str))")
                                        val url = "/download/${tabState.megrendeles.azonosito}/$encodedFilename/"
                                        x.open("GET", "url", true)
                                        x.responseType = XMLHttpRequestResponseType.BLOB
                                        x.onload = { e ->
                                            downloadFile(e.target.asDynamic().response, record.name)
                                        }
                                        x.send()
                                    }
                                    +"Letöltés"
                                }
                                if (record.name.endsWith(".xlsx")) {
                                    Divider(type = DividerType.vertical)
                                    a() {
                                        +"Adatok betöltése"
                                    }
                                }

                            }
                        }
                    }

                }
        )

        Table {
            attrs.columns = columns
            attrs.dataSource = tabState.megrendeles.files
            attrs.rowKey = "name"
            attrs.onRow = { account ->
                jsObject {
                    //this.asDynamic().onClick = { globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal((account as Account).id))) }
                }
            }
            attrs.asDynamic().size = "middle"
        }
    }

    private fun Int.toMB() = this / 1048576

    private fun RBuilder.uploadModal(tabState: FajlokTabComponentState,
                                     setTabState: Dispatcher<FajlokTabComponentState>,
                                     globalDispatch: (Action) -> Unit) {
        Modal {
            attrs.title = StringOrReactElement.fromString("Fájlok feltöltése")
            attrs.visible = tabState.uploadModalVisible
            attrs.okText = "Feltöltés"
            attrs.okButtonProps = jsObject {
                disabled = tabState.uploadingFiles.any { it.size.toMB() > 6 }
            }
            attrs.onOk = {
                communicator.upload(
                        UploadData(
                                tabState.megrendeles,
                                tabState.uploadingFiles,
                                tabState.tartalmazErtekbecsles ?: false,
                                tabState.tartalmazEnergetika ?: false
                        )) { response ->
                    setTabState(tabState.copy(uploadingFiles = emptyList(), uploadModalVisible = false))
                    globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
                    message.success("Fájlok sikeresen feltöltve")
                }
            }
            attrs.onCancel = {
                setTabState(tabState.copy(uploadModalVisible = false))
            }
            Form {
                if (tabState.megrendeles.munkatipus.isEnergetika()) {
                    FormItem {
                        attrs.label = StringOrReactElement.fromString("A feltölteni kívánt fájlok tartalmazzák az Energetikai tanusítványt")
                        RadioGroup {
                            attrs.value = tabState.tartalmazEnergetika
                            attrs.onChange = { e ->
                                setTabState(tabState.copy(
                                        tartalmazEnergetika = e.target.asDynamic().value
                                ))
                            }
                            Radio(true) { +"Igen" }
                            Radio(false) { +"Nem" }
                        }
                    }
                }
                if (tabState.megrendeles.munkatipus.isErtekbecsles()) {
                    FormItem {
                        attrs.label = StringOrReactElement.fromString("A feltölteni kívánt fájlok tartalmazzák az Értékbecslést")
                        RadioGroup {
                            attrs.value = tabState.tartalmazErtekbecsles
                            attrs.onChange = { e ->
                                setTabState(tabState.copy(
                                        tartalmazErtekbecsles = e.target.asDynamic().value
                                ))
                            }
                            Radio(true) { +"Igen" }
                            Radio(false) { +"Nem" }
                        }
                    }
                }
                val energetikaChosen = !tabState.megrendeles.munkatipus.isEnergetika() || tabState.tartalmazEnergetika != null
                val ebChosen = !tabState.megrendeles.munkatipus.isErtekbecsles() || tabState.tartalmazErtekbecsles != null
                if (energetikaChosen && ebChosen) {
                    uploader(setTabState, tabState)
                }
            }
        }
    }

    private fun RElementBuilder<FormProps>.uploader(setTabState: Dispatcher<FajlokTabComponentState>, tabState: FajlokTabComponentState) {
        Dragger {
            attrs.name = "file"
            attrs.multiple = true
            attrs.onRemove = { removingFile ->
                setTabState(tabState.copy(uploadingFiles = tabState.uploadingFiles.filterIndexed { index, file ->
                    index.toString() != removingFile.uid
                }))
                false
            }
            attrs.fileList = tabState.uploadingFiles.mapIndexed { index, file ->
                val tooBig = file.size.toMB() > 6
                DefaultFileListItem(
                        uid = index.toString(),
                        name = file.name + if (tooBig) "(${file.size.toMB()} MB)" else "",
                        status = if (tooBig) "error" else "done",
                        response = if (tooBig) "A fájlok maximális mérete 6 MB lehet!" else null,
                        url = "#"
                )
            }.toTypedArray()
            attrs.action = { file: File ->
                setTabState(tabState.copy(uploadingFiles = tabState.uploadingFiles + file))
                Promise<Any> { resolve, reject ->
                    resolve(0)
                }
            }
            attrs.onChange = { info ->

            }
            p(classes = "ant-upload-drag-icon") {
                Icon("inbox")
            }
            p(classes = "ant-upload-text") {
                +"Kattints vagy mozgass egy fájlt ide a feltöltéshez"
            }
            p(classes = "ant-upload-hint") {
                +"Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files"
            }
        }
    }
}

private fun RBuilder.feltoltesPanel(state: FajlokTabComponentState, setTabState: Dispatcher<FajlokTabComponentState>) {
    Button {
        attrs.type = ButtonType.default
        attrs.onClick = {
            setTabState(state.copy(uploadModalVisible = true))
        }
        Icon("upload")
        +" Fájlok feltöltése"
    }
}