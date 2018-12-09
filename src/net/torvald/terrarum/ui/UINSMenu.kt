package net.torvald.terrarum.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.*
import java.lang.Error

/**
 * Nextstep-themed menu bar with mandatory title line
 *
 * Created by minjaesong on 2018-12-08.
 */
class UINSMenu(
        var title: String = "",
        val minimumWidth: Int,
        treeRepresentation: Yaml,

        val titleBackCol: Color = Color(0f,0f,0f,.77f),
        val titleTextCol: Color = Color.WHITE,
        val titleBlendMode: String = BlendMode.NORMAL,

        val allowDrag: Boolean = true
) : UICanvas() {

    override var openCloseTime: Second = 0f
    val LINE_HEIGHT = 30
    val TEXT_OFFSETX = 3f
    val TEXT_OFFSETY = (LINE_HEIGHT - Terrarum.fontGame.lineHeight) / 2f
    val CHILD_ARROW = "${0x2023.toChar()}"


    val tree = treeRepresentation.parse()
    override var width = 0
    override var height = 0
    //override var width = maxOf(minimumWidth, tree.getLevelData(1).map { Terrarum.fontGame.getWidth(it ?: "") }.max() ?: 0)
    //override var height = LINE_HEIGHT * (tree.children.size + 1)


    private val listStack = ArrayList<MenuPack>()
    private var currentDepth = 0

    private data class MenuPack(val title: String, val list: UIItemTextButtonList)

    private fun ArrayList<MenuPack>.push(item: MenuPack) { this.add(item) }
    private fun ArrayList<MenuPack>.pop() = this.removeAt(this.lastIndex)!!
    private fun ArrayList<MenuPack>.peek() = this.last()!!


    val selectedIndex: Int?
        get() = listStack.peek().list.selectedIndex

    init {
        addSubMenu(tree)
    }

    private fun addSubMenu(tree: QNDTreeNode<String>) {
        val stringsFromTree = Array<String>(tree.children.size) {
            tree.children[it].toString() + if (tree.children[it].children.isNotEmpty()) "  $CHILD_ARROW" else ""
        }

        val listWidth = maxOf(minimumWidth, tree.getLevelData(1).map { Terrarum.fontGame.getWidth(it ?: "") }.max() ?: 0)
        val listHeight = stringsFromTree.size * LINE_HEIGHT

        val list = UIItemTextButtonList(
                this,
                stringsFromTree,
                width, LINE_HEIGHT,
                listWidth, listHeight,
                textAreaWidth = listWidth - (2 * TEXT_OFFSETX.toInt()),
                alignment = UIItemTextButton.Companion.Alignment.LEFT,
                activeBackCol = Color(0x242424_80),//Color(1f,0f,.75f,1f),
                inactiveCol = Color(.94f,.94f,.94f,1f),
                itemHitboxSize = LINE_HEIGHT

        )

        // List selection change listener
        list.selectionChangeListener = { old, new ->
            // if the selection has a child...
            if (tree.children[new].children.isNotEmpty()) {
                // 1. pop as far as possible
                // 2. push the new menu

                // 1. pop as far as possible
                while (listStack.peek().list != list) {
                    popSubMenu()
                }

                // 2. push the new menu
                addSubMenu(tree.children[new])
            }
        }
        // END List selection change listener


        // push the processed list
        listStack.push(MenuPack(tree.data ?: title, list))
        // increment the memoized width
        width += listWidth
        currentDepth += 1
    }

    private fun popSubMenu() {
        if (listStack.size == 1) throw Error("Tried to pop root menu")

        val poppedUIItem = listStack.pop()
        width -= poppedUIItem.list.width
    }

    override fun updateUI(delta: Float) {
        /*listStack.forEach {
            it.list.update(delta)
        }*/ // fucking concurrent modification

        var c = 0
        while (c < listStack.size) {
            listStack[c].list.update(delta)
            c += 1
        }
    }

    override fun renderUI(batch: SpriteBatch, camera: Camera) {
        listStack.forEach {
            // draw title bar
            batch.color = titleBackCol
            BlendMode.resolve(titleBlendMode, batch)
            batch.fillRect(it.list.posX.toFloat(), it.list.posY.toFloat() - LINE_HEIGHT, it.list.width.toFloat(), LINE_HEIGHT.toFloat())

            batch.color = titleTextCol
            blendNormal(batch)
            Terrarum.fontGame.draw(batch, it.title, TEXT_OFFSETX + it.list.posX, TEXT_OFFSETY + it.list.posY - LINE_HEIGHT)

            // draw the list
            batch.color = Color.WHITE
            it.list.render(batch, camera)
        }

    }

    override fun dispose() {
        listStack.forEach { it.list.dispose() }
    }

    fun mouseOnTitleBar() =
            relativeMouseX in 0 until width && relativeMouseY in 0 until LINE_HEIGHT

    override fun doOpening(delta: Float) {
    }

    override fun doClosing(delta: Float) {
    }

    override fun endOpening(delta: Float) {
    }

    override fun endClosing(delta: Float) {
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return super.mouseMoved(screenX, screenY)
    }

    private var dragOriginX = 0 // relative mousepos
    private var dragOriginY = 0 // relative mousepos
    private var dragForReal = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!allowDrag) return false

        if (mouseInScreen(screenX, screenY)) {
            if (dragForReal) {
                handler.setPosition(screenX - dragOriginX, screenY - dragOriginY)
                //println("drag $screenX, $screenY")
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (mouseOnTitleBar()) {
            dragOriginX = relativeMouseX
            dragOriginY = relativeMouseY
            dragForReal = true
        }
        else {
            dragForReal = false
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchUp(screenX, screenY, pointer, button)
    }

    override fun scrolled(amount: Int): Boolean {
        return super.scrolled(amount)
    }

    override fun keyDown(keycode: Int): Boolean {
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        return super.keyUp(keycode)
    }

    override fun keyTyped(character: Char): Boolean {
        return super.keyTyped(character)
    }

    override fun resize(width: Int, height: Int) {
    }
}