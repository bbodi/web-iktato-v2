package hu.nevermind.iktato
import hu.nevermind.utils.store.Alvallalkozo

object Path {

    val root: String = "#"
    val login = "login/"
    object account {
        val root = "account/"
        val withOpenedEditorModal =  { id: Int -> "$root$id/"}
    }
    object megrendelo {
        val root = "megrendelo/"
        val withOpenedEditorModal =  { name: String -> "$root$name/"}
    }
    object megrendeles {
        val root = "megrendeles/"
        val tableConfig = "tableConfig/"
        val edit =  { id: Int -> "$root$id/"}
    }
    object sajatAr {
        val root = "sajatAr/"
        val withOpenedEditorModal =  { id: Int -> "$root$id/"}
    }
    object munkatipus {
        val root = "munkatipus/"
        val withOpenedEditorModal =  { name: String -> "$root$name/"}
    }
    object ertekbecslo {
        val r = "ertekbecslo/"
        val root =  { allvallalkozoId: Int -> "${r}$allvallalkozoId/ebs/"}
        val withOpenedErtekbecsloEditorModal =  { avId: Int, ertekbecsloId: Int -> "${root(avId)}$ertekbecsloId/"}
    }
    object alvallalkozo {
        val root = "alvallalkozo/"
        val sajatAdatok = "${root}sajatAdatok/"
        val regio = { alvallallkozoId: Int -> "${root}regio/$alvallallkozoId/"}
        val withOpenedRegioOsszerendelesModal =  { allvallalkozoId: Int, regioOsszerendelesId: Int ->
            "${regio(allvallalkozoId)}$regioOsszerendelesId/"
        }
        val withOpenedAlvallalkozoEditorModal =  { allvallalkozoId: Int -> "${root}av/$allvallalkozoId/"}
    }
}