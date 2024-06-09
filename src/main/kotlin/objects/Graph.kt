package minimalDemo.objects

import minimalDemo.json.JGraph
import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.join
import org.jetbrains.kotlinx.dataframe.api.rename
import kotlin.random.Random

data class Graph(
    val nodes: MutableList<Node> = mutableListOf()
) {
    fun getDataFrameNodes(): AnyFrame {

        nodes.forEach { node ->
            val random = Random(node.uuid.hashCode())
            node.x = random.nextInt(1300) * 1500 // spread out the nodes in the x-axis
            node.y = random.nextInt(1300) * 1500 // spread out the nodes in the y-axis
        }

        return dataFrameOf(
            "id" to nodes.map { it.uuid },
            "node" to nodes.map { it.name },
            "x" to nodes.map { it.x },
            "y" to nodes.map { it.y }
        )
    }

    fun hasEulerPath(): Boolean {
        val oddNodes = nodes.count { it.edges.size % 2 != 0 }
        return oddNodes == 0 || oddNodes == 2
    }

    fun getEulerPath(): List<Node> {
        val startNode = nodes.firstOrNull { it.edges.size % 2 != 0 } ?: nodes.first()
        val path = mutableListOf<Node>()
        val visited = mutableSetOf<Node>()
        val stack = mutableListOf(startNode)

        while (stack.isNotEmpty()) {
            val node = stack.last()
            val edge = node.edges.firstOrNull { !visited.contains(it.target) }
            if (edge != null) {
                stack.add(edge.target)
                visited.add(edge.target)
            } else {
                path.add(stack.removeLast())
            }
        }

        return path
    }

    fun getDataFrameEdges(nodesDf: AnyFrame) = dataFrameOf(
            "from" to nodes.flatMap { node -> node.edges.map { node.name } },
            "to" to nodes.flatMap { node -> node.edges.map { edge -> edge.target.name } },
            "relation" to nodes.flatMap { node -> node.edges.map { edge -> edge.weight } },
            "weight" to nodes.flatMap { node -> node.edges.map { edge -> edge.weight } }
        )
        .add("id") { it.index() }

        .join(nodesDf) { "to" match right["node"] }.rename(Pair("x", "xTo")).rename(Pair("y", "yTo"))
        .join(nodesDf) { "from" match right["node"] }

        .add("midX") { row -> (row["x"] as Int + row["xTo"] as Int) / 2 }
        .add("midY") { row -> (row["y"] as Int + row["yTo"] as Int) / 2 }

    companion object {
        fun fromJson(json: JGraph): Graph {
            val nodes = json.nodes.map { Node(it.name) }.toMutableList()
            nodes.forEach {
                node -> node.edges.addAll(
                    json.edges
                        .filter { it.from == node.name }
                        .map { Edge(nodes.find { node -> node.name == it.to }!!, it.weight) }
                )
            }

            return Graph(nodes)
        }
    }
}