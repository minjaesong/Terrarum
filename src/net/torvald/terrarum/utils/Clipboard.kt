package net.torvald.terrarum.utils

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * Created by minjaesong on 2016-07-31.
 */
object Clipboard {
    fun fetch(): String =
            Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String

    fun paste(s: String) {
        val selection = StringSelection(s)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, selection)
    }
}