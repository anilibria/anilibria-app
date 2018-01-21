package ru.radiationx.anilibria.utils.bbparser

import ru.radiationx.anilibria.utils.bbparser.models.BbNode
import ru.radiationx.anilibria.utils.bbparser.models.BbOp
import ru.radiationx.anilibria.utils.bbparser.models.BbTypedOp

class BbSequencer {

    val bbBlocks = arrayOf("IMG", "QUOTE", "CODE")
    private var type: BbOp.Type = BbOp.Type.TEXT
    private val buffer = mutableListOf<BbOp>()
    private val output = mutableListOf<BbTypedOp>()
    private var rootNodeId: Int = 0

    fun toSequence(bbNode: BbNode): List<BbTypedOp> {
        rootNodeId = bbNode.id
        printWidgetRec(bbNode)
        releaseBuffer()
        val result = output.toList()
        buffer.clear()
        output.clear()
        return result
    }

    private fun releaseBuffer(bbParents: MutableList<BbNode> = mutableListOf()) {
        if (type == BbOp.Type.TEXT) {
            val opened = mutableMapOf<Int, BbOp>()
            buffer.forEach {
                if (it.op == BbOp.OPEN) {
                    opened.put(it.node.id, it)
                } else {
                    opened.remove(it.node.id)
                }
            }
            opened.forEach {
                buffer.remove(it.value)
            }
        }
        //println("Out: " + buffer.joinToString())
        output.add(BbTypedOp(type, buffer.toList()))
        buffer.clear()
        bbParents.forEach {
            appendBuffer(it)
        }
    }

    private fun appendBuffer(bbNode: BbNode) {
        if (bbNode.tag == BbNode.NODE_TEXT) {
            buffer.add(BbOp(BbOp.APPEND, bbNode))
        } else {
            buffer.add(BbOp(BbOp.OPEN, bbNode))
        }
    }

    private fun closeBuffer(bbNode: BbNode) {
        if (bbNode.tag == BbNode.NODE_TEXT) {

        } else {
            buffer.add(BbOp(BbOp.CLOSE, bbNode))
        }
    }


    private fun printWidgetRec(bbNode: BbNode, bbParents: MutableList<BbNode> = mutableListOf()) {
        var newBufferType = type
        var bufferReleased = false

        if (bbNode.id != rootNodeId) {
            bbParents.add(bbNode)
            newBufferType = if (bbBlocks.contains(bbNode.tag)) BbOp.Type.BLOCK else BbOp.Type.TEXT
            bufferReleased = type != newBufferType


            if (bbParents.isNotEmpty()) {
                if (bufferReleased) {
                    releaseBuffer(bbParents)
                } else {
                    appendBuffer(bbNode)
                }
            }
        }

        type = newBufferType

        if (!bufferReleased) {
            bbNode.childs.forEach {
                printWidgetRec(it, bbParents)
            }
        }

        if (bbNode.id != rootNodeId) {
            if (bbParents.isNotEmpty()) {
                if (bufferReleased) {
                    //releaseBuffer(bbParents)
                } else {
                    //closeBuffer(bbNode)
                }
                if (type == BbOp.Type.TEXT) {
                    closeBuffer(bbNode)
                }
            }
            bbParents.remove(bbNode)
        }
    }

}