package ru.radiationx.anilibria.ui.widgets.bbwidgets

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.utils.bbparser.models.BbNode
import ru.radiationx.anilibria.utils.bbparser.models.BbOp
import ru.radiationx.anilibria.utils.bbparser.models.BbTypedOp
import kotlin.math.max
import kotlin.math.min

/**
 * Created by radiationx on 21.01.18.
 */
open class BbView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = LinearLayout.VERTICAL
    }

    fun setContent(node: BbNode) {
        setContent(node.toSequence())
    }

    fun setContent(ops: List<BbTypedOp>) {
        removeAllViews()
        ops.forEach {
            when (it.type) {
                BbOp.Type.TEXT -> {
                    addTextContent(it.bbOps)
                }
                BbOp.Type.BLOCK -> {
                    addBlockContent(it.bbOps)
                }
            }
        }
    }

    fun addTextContent(bbOp: List<BbOp>): String {
        var result = ""
        bbOp.forEach {
            val node = it.node
            when (it.op) {
                BbOp.OPEN -> {
                    val tag = getTagName(node)
                    val attributes = getAttributes(node)
                    result += "<${tag.toLowerCase()}$attributes>"
                }
                BbOp.APPEND -> {
                    val text = if (node.text == "\n") "<br>" else node.text
                    result += text
                }
                BbOp.CLOSE -> {
                    val tag = getTagName(node)
                    if (tag != "li") {
                        result += "</${tag.toLowerCase()}>"
                    }
                }
            }
        }
        println("addTextContent: '${result}'")
        val newTextView = TextView(context)
        newTextView.setTextColor(ContextCompat.getColor(context, R.color.textDefault))
        newTextView.text = Html.fromHtml(result)
        newTextView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(newTextView)
        return result
    }

    fun addBlockContent(bbOp: List<BbOp>) {

        if (bbOp.last().node.tag == "QUOTE") {
            val newViewView = BbQuote(context)
            newViewView.setContent(bbOp.last().node)
            newViewView.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (resources.displayMetrics.density * 8).toInt()
                bottomMargin = (resources.displayMetrics.density * 8).toInt()
            }
            addView(newViewView)
        } else if (bbOp.last().node.tag == "IMG") {
            val newTextView = BbImageView(context)
            newTextView.setBbImage(bbOp)
            newTextView.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (resources.displayMetrics.density * 8).toInt()
                bottomMargin = (resources.displayMetrics.density * 8).toInt()
            }
            addView(newTextView)
        } else {
            var result = ""
            val styles = mutableListOf<String>()
            bbOp.forEach {
                val node = it.node
                when (it.op) {
                    BbOp.OPEN -> {
                        if (node.tag != "IMG" && node.tag != "QUOTE") {
                            styles.add(node.tag)
                        } else {
                            result += "Block ${node.tag} with {${styles.joinToString()}} and ${node.childs.joinToString { it.text }}"
                        }
                    }
                    BbOp.APPEND -> {
                        result += "!!!WTF APPEND!!!"
                    }
                    BbOp.CLOSE -> {
                        result += "!!!WTF CLOSE!!!"
                    }
                }
            }
            println("addBlockContent: '${result}'")
            val newTextView = TextView(context)
            newTextView.text = result
            newTextView.setBackgroundColor(Color.argb(48, 0, 255, 0))
            newTextView.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (resources.displayMetrics.density * 8).toInt()
                bottomMargin = (resources.displayMetrics.density * 8).toInt()
            }
            addView(newTextView)
        }
    }


    fun getTagName(node: BbNode): String = when (node.tag) {
        "CENTER", "LEFT", "RIGHT", "JUSTIFY" -> "div"
        "LIST" -> {
            val attr = node.attributes.get(node.tag)
            if (attr == null || attr != "1") {
                "ul"
            } else {
                "ol"
            }
        }
        "*" -> "li"
        "URL", "USER" -> "a"
        "COLOR" -> "span"
        "SIZE" -> {
            var header = "h"
            try {
                val attr = node.attributes.get(node.tag)
                if (attr == null) {
                    header = "p"
                } else {
                    val hval = max(min(attr.toInt(), 6), 1)
                    header += (7 - hval).toString()
                }
            } catch (ex: Exception) {
                header = "p"
            }
            header
        }
        else -> node.tag
    }

    fun getAttributes(node: BbNode): String {
        val attributes = mutableMapOf<String, String>()
        when (node.tag) {
            "LEFT", "JUSTIFY" -> appendAttr(attributes, "style", "text-align:start;")
            "RIGHT" -> appendAttr(attributes, "style", "text-align:end;")
            "CENTER" -> appendAttr(attributes, "style", "text-align:center;")
            "COLOR" -> {
                val color = node.attributes.get(node.tag)
                if (color != null) {
                    appendAttr(attributes, "style", "color:$color;")
                }
            }
            "URL" -> {
                val href = node.attributes.get(node.tag)
                if (href != null) {
                    appendAttr(attributes, "href", href)
                }
            }
            "USER" -> {
                val href = node.attributes.get(node.tag)
                if (href != null) {
                    appendAttr(attributes, "href", "/user/$href/")
                }
            }
        }
        var result = ""
        attributes.forEach {
            result += " $${it.key}=\"${it.value}\""
        }
        return result
    }

    fun appendAttr(attrs: MutableMap<String, String>, key: String, value: String) {
        val attr = attrs.get(key)
        if (attr == null) {
            attrs.put(key, value)
        } else {
            attrs[key] = "$attr $value"
        }
    }

}