package hu.nevermind.iktato

import hu.nevermind.utils.hu.nevermind.antd.message
import store.UploadData
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.MegrendelesFieldsFromExternalSource
import org.w3c.xhr.FormData
import store.MegrendelesFromServer

object RestUrl {
    const val login = "/login"
    const val logout = "/logout"
    const val authenticate = "/user"

    const val getAccountsFromServer = "/getAllAccounts"
    const val saveAccount = "/saveAccount"

    const val getAllSajatAr = "/getAllSajatAr"
    const val saveSajatAr = "/saveSajatAr"

    const val getAlvallalkozoForAccount = "/getAlvallalkozoForAccount"
    const val getAlvallalkozokFromServer = "/getAllAlvallalkozo"
    const val saveAlvallalkozo = "/saveAlvallalkozo"

    const val getAllErtekbecslo = "/getAllErtekbecslo"
    const val saveErtekBecslo = "/saveErtekBecslo"

    const val getAllRegioOsszerendeles = "/getAllRegioOsszerendeles"
    const val saveRegioOsszerendeles = "/saveRegioOsszerendeles"

    const val saveMegrendeles = "/saveMegrendeles"
    const val getMegrendelesFromServer = "/getAllMegrendeles"
    const val getMegrendelesByIdFromServer = "/getMegrendelesByIdFromServer"
    const val getFilteredMegrendelesFromServer = "/getFilteredMegrendelesFromServer"
    const val getMegrendelesekOfAlvallalkozo = "/getMegrendelesekOfAlvallalkozo"
    const val megrendelesAtvetele = "/megrendelesAtvetele"
    const val megrendelesMegnyitasa = "/megrendelesMegnyitasa"
    const val megrendelesTablazatOszlop = "/megrendelesTablazatOszlop"
    const val megrendelesFilter = "/megrendelesFilter"
    const val getMegrendelesUpdates = "/getMegrendelesUpdates"
    const val akadalyKozles = "/akadalyKozles"

    const val getVarosok = "/getVarosok"
    const val getIrszamok = "/getIrszamok"
    const val changePassword = "/changePassword"

    const val parseExcel = "/parseExcel"
    const val emailKuldeseUjra = "/emailKuldeseUjra"
}

data class AjaxRequest(val url: String,
                       val type: String = "POST",
                       val data: Any,
                       val contentType: String = "application/json; charset=utf-8",
                       val dataType: String = "json",
                       val async: Boolean = true,
                       val success: ((Any) -> Unit))

public data class AjaxResult<T>(val status: Boolean, val data: T)

val Jq: JqDef = kotlinext.js.require("jquery")

external interface JqDef {
    fun ajax(req: Any): Unit
    fun <T> get(url: String,
                data: Any? = definedExternally,
                success: ((response: T) -> Unit)?,
                dataType: String? = definedExternally
    ): dynamic

//    fun blockUI(param: Any)
}

class JqueryAjaxPoster() : AjaxPoster {

    override fun <RESULT> ajaxPost(url: String,
                                   type: String,
                                   data: Any?,
                                   contentType: String,
                                   dataType: String,
                                   async: Boolean,
                                   otherParams: (dynamic) -> Unit,
                                   after: (Result<RESULT, String>) -> Unit
    ) {
        val error = { jqXHR: dynamic, textStatus: String, errorThrown: String ->
            console.error("ERROR: $url")
            after(Error(jqXHR.responseJSON?.message ?: ""))
        }
        val success = { data: RESULT ->
            console.log("SUCCESS: $url")
            after(Ok(data))
        }
        val ajaxRequest: dynamic = object {
            val url = url
            val type = type
            val data = data
            val contentType = contentType
            val dataType = dataType
            val async = async
            val error = error
            val success = success
        }
        otherParams(ajaxRequest)
        Jq.ajax(ajaxRequest)
    }


}

open class Result<OK, ERR>() {
    fun withOkOrError(body: (OK) -> Unit) {
        require(this is Ok)
        body((this as Ok).ok)
    }

    fun ifOk(body: (OK) -> Unit) {
        if (this is Ok) {
            body(this.ok)
        }
    }

    fun ifError(body: (ERR) -> Unit) {
        if (this is Error) {
            body(this.error)
        }
    }
}

class Ok<OK, ERR>(val ok: OK) : Result<OK, ERR>()
class Error<OK, ERR>(val error: ERR) : Result<OK, ERR>()

interface AjaxPoster {
    fun <RESULT> ajaxPost(
            url: String,
            type: String = "POST",
            data: Any? = null,
            contentType: String = "application/json; charset=utf-8",
            dataType: String = "json",
            async: Boolean = true,
            otherParams: (dynamic) -> Unit = {},
            after: ((Result<RESULT, String>) -> Unit)
    ): Unit
}

class Communicator(val ajaxPoster: AjaxPoster) {

    fun <T, R> getEntitiesFromServer(url: String, data: Any = "", callback: (Array<T>) -> R): R {
//        Jq.blockUI(object {val baseZ = 2000})
        var returnValue = 1.asDynamic()
        ajaxPoster.ajaxPost(
                url = url,
                type = "POST",
                data = JSON.stringify(data),
                async = false) { result: Result<Array<dynamic>, String> ->
//            js("$.unblockUI()")
            result.withOkOrError { returnedEntities ->
                returnValue = callback(returnedEntities)
            }
        }
        return returnValue
    }

    fun <T, R> getEntityFromServer(url: String, data: Any = "", callback: (T) -> R): R {
        var returnValue = 1.asDynamic()
        ajaxPoster.ajaxPost(
                url = url,
                type = "POST",
                data = JSON.stringify(data),
                async = false) { result: Result<dynamic, String> ->
            result.withOkOrError { returnedEntities ->
                returnValue = callback(returnedEntities)
            }
        }
        return returnValue
    }

    fun <T> asyncPost(url: String, data: Any = "", callback: (Result<T, String>) -> Unit) {
        ajaxPoster.ajaxPost(
                url = url,
                type = "POST",
                data = JSON.stringify(data),
                async = true) { result: Result<dynamic, String> ->
            callback(result)
        }
    }

    fun <T> post(url: String, data: Any = "", callback: (T) -> Unit) {
//        Jq.blockUI(object {val baseZ = 2000})
        ajaxPoster.ajaxPost(
                url = url,
                type = "POST",
                data = JSON.stringify(data),
                async = false) { result: Result<dynamic, String> ->
//            js("$.unblockUI()")
            result.ifOk { response ->
                callback(response)
            }
            result.ifError { response ->
                message.error("Hiba: $response")
            }
        }
    }

    fun <OUT : Any, IN : Any> saveEntity(url: String, entity: OUT, callback: (IN) -> Unit) {
//        Jq.blockUI(object {val baseZ = 2000})
        ajaxPoster.ajaxPost(
                url = url,
                data = JSON.stringify(entity),
                async = false) { result: Result<IN, String> ->
//            js("$.unblockUI()")
            result.ifOk { response ->
                callback(response)
            }
            result.ifError { response ->
//                Actions.notification(Notification("Danger", "Hiba: $response"))
            }
        }
    }

    fun deleteEntity(entityType: String, id: Int, callback: () -> Unit = {}) {
        val data = object {
            val entityType = entityType
            val id = id
        }
//        Jq.blockUI(object {val baseZ = 2000})
        ajaxPoster.ajaxPost(
                url = "/deleteEntity",
                data = JSON.stringify(data),
                async = false) { result: Result<Any, String> ->
//            js("$.unblockUI()")
            result.ifOk { response ->
                callback()
            }
            result.ifError { response ->
//                Actions.notification(Notification("Danger", "Hiba: $response"))
            }
        }
    }

    fun getRequest(url: String, after: (Result<Any, String>) -> Unit) {
        ajaxPoster.ajaxPost(
                url = url,
                async = false,
                after = { result: Result<Any, String> ->
                    after(result)
                },
                type = "GET"
        )
    }

    fun parseExcel(azonosito: String, filename: String, callback: (MegrendelesFieldsFromExternalSource) -> Unit) {
//        TODO Jq.blockUI(object {val baseZ = 2000})
        ajaxPoster.ajaxPost(
                url = RestUrl.parseExcel,
                data = JSON.stringify(object {
                    val azonosito = azonosito
                    val filename = filename
                }),
                async = false) { result: Result<MegrendelesFieldsFromExternalSource, String> ->
//            js("$.unblockUI()")
            result.ifOk { response ->
                callback(response)
            }
            result.ifError { response ->
//                Actions.notification(Notification("Danger", "Hiba: $response"))
            }
        }
    }

    fun upload(uploadData: UploadData, callback: (MegrendelesFromServer) -> Unit) {
        val data = FormData()
        uploadData.files.forEach {
            data.append("file", it)
        }
        data.append("megrendelesId", uploadData.megr.id.toString())
        data.append("ertekbecslestTartalmazza", uploadData.ertekbecslestTartalmazza.toString())
        data.append("energetikatTartalmazza", uploadData.energetikatTartalmazza.toString())
        ajaxPoster.ajaxPost(
                url = "/upload",
                data = data,
                dataType = "",
                otherParams = { req ->
                    req.cache = false
                    req.enctype = "multipart/form-data"
                    req.processData = false
                    req.contentType = false
                },
                async = true) { result: Result<MegrendelesFromServer, String> ->
            result.ifOk { response ->
                callback(response)
            }
            result.ifError { response ->
                message.error("Hiba: $response")
            }
        }
    }
}