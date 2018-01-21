package ru.radiationx.anilibria.utils.bbparser

import ru.radiationx.anilibria.utils.bbparser.models.BbNode

class BbOptimizer {

    fun optimize2(bbNode: BbNode) {
        optimize2Rec(bbNode)
        println("TEXT " + bbNode.childs[0].text)
        suka.forEach {
            println("SUKS ${it.key} => ${it.value.id}")
        }
    }

    val suka = mutableMapOf<Int, BbNode>()
    fun optimize2Rec(bbNode: BbNode): List<BbNode> {
        val result = ArrayList<BbNode>(0)

        if (isDoOpt(bbNode)) {
            result.add(bbNode)
        }

        val forRemove = mutableListOf<BbNode>()

        while (true) {
            bbNode.childs.forEach {
                val recRes = optimize2Rec(it)
                forRemove.addAll(recRes)
            }
            if (forRemove.isEmpty()) {
                println("NOT REMOVE IN [${bbNode.id}]")
                println("\t\t\t\tSUKA[${bbNode.id}] = ${bbNode.tag != BbNode.NODE_TEXT && !isDoOpt(bbNode)}")
                if (bbNode.tag != BbNode.NODE_TEXT && !isDoOpt(bbNode)) {
                    println("\t\t\t\tTRY ACCEPT ${bbNode.id}:${suka.get(bbNode.id)?.id}")
                    suka.get(bbNode.id)?.let {
                        println("\t\t\t\tACCEPT APPEND [${bbNode.id}] <= [${it.id}]")
                        bbNode.childs.add(it)
                        //forRemove.add(it)
                        println("\t\t\t\tREMOVE MAP [${bbNode.id}]")
                        suka.remove(bbNode.id)
                    }
                }
                break
            } else {
                println("DO REMOVE IN [${bbNode.id}], size=${bbNode.childs.size}, items={${bbNode.childs.joinToString() { "[${it.id}]" }}}")

                suka.get(bbNode.id)?.let {
                    bbNode.parent?.let { it1 ->
                        println("\t\tAPPEND MAP [${it1.id}] <= [${it.id}]")
                        suka.put(it1.id, it)
                        suka.remove(bbNode.id)
                    }
                    println("\t\t\t\tREMOVE MAP [${bbNode.id}]")

                }
                forRemove.forEachIndexed { index, item ->
                    println("\tREMOVE [${item.id}]")
                    if (item.tag == BbNode.NODE_TEXT) {

                        bbNode.parent?.let { it1 ->
                            println("\t\tAPPEND [${it1.id}] <= [${item.id}]")
                            suka.put(it1.id, item)
                        }
                    }
                    bbNode.childs.remove(item)
                }
            }
            forRemove.clear()
        }

        if (!result.contains(bbNode)) {
            if (isDoOpt(bbNode)) {
                result.add(bbNode)
            }
        }
        return result
    }

    fun isDoOpt(bbNode: BbNode): Boolean {
        //println("DoOpt: [${bbNode.tag}:${bbNode.id}]=${bbNode.childs.size}:${bbNode.parent?.id}")
        if (bbNode.tag == BbNode.NODE_TEXT && bbNode.text == "kek") {
            bbNode.parent?.let {
                it.childs.forEach { child ->
                    if (child.tag != BbNode.NODE_TEXT) {
                        return false
                    }
                }
            }
            return true
        }
        if (bbNode.tag != BbNode.NODE_TEXT && bbNode.childs.isEmpty()) {

            return true
        }
        return false
    }

}