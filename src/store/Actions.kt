package store

import app.common.Moment
import app.megrendeles.MegrendelesFilter
import app.megrendeles.SzuroMezo
import hu.nevermind.utils.store.LoggedInUser
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.RegioOsszerendeles
import hu.nevermind.utils.store.SajatAr
import org.w3c.files.File

data class AkadalyKozles(val megr: Megrendeles,
                         val reason: String,
                         val hatarido: Moment,
                         val comment: String)

data class UploadData(val megr: Megrendeles,
                      val files: List<File>,
                      val ertekbecslestTartalmazza: Boolean,
                      val energetikatTartalmazza: Boolean)

sealed class SetActiveFilterPayload {
    data class SimpleFilter(val activeFilter: MegrendelesFilter) : SetActiveFilterPayload()
    data class HaviTeljesites(val alvallalkozoId: Int, val date: Moment) : SetActiveFilterPayload()
    data class MindFilter(val ids: List<Int>, val szuromezok: List<SzuroMezo>) : SetActiveFilterPayload()
}

sealed class Action {
    data class MegrendelesekFromServer(val data: Array<dynamic>) : Action()
    data class SetLoggedInUser(val data: LoggedInUser?) : Action()
    data class ChangeURL(val url: String) : Action()
    data class changeURLSilently(val url: String) : Action()
    data class SetActiveFilter(val payload: SetActiveFilterPayload) : Action()
    data class SajatArFromServer(val sajatAr: SajatAr) : Action()
    data class AccountFromServer(val response: dynamic) : Action()
    data class AlvallalkozoFromServer(val response: dynamic) : Action()
    data class ErtekbecsloFromServer(val response: dynamic) : Action()
    data class RegioOsszerendelesFromServer(val response: dynamic) : Action()
    data class DeleteRegioOsszerendeles(val regioOsszerendeles: RegioOsszerendeles) : Action()
}

object Actions {

//    val notification = ActionDef<Notification>()
//
//    val accountFromServer = ActionDef<dynamic>()
//    val megrendelesTableOszlopFromServer = ActionDef<String>()
//
//
//    val megrendelesekFromServer = ActionDef<Array<dynamic>>()
//    class MegrendelesekFromServer(val data: Array<dynamic>): RAction {
//    }
}

//    val regioOsszerendelesFromServer = ActionDef<dynamic>()

