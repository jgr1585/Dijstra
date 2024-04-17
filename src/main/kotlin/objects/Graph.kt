package minimalDemo.objects

import minimalDemo.json.JGraph
import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.join
import org.jetbrains.kotlinx.dataframe.api.rename
import kotlin.math.cos
import kotlin.math.sin

data class Graph(
    val nodes: MutableList<Node> = mutableListOf()
) {
    fun getDataFrameNodes(): AnyFrame {

        val radius = 10.0  // Define a constant radius value
        val angleStep = 2 * Math.PI / nodes.size  // Calculate the angle step

        nodes.forEachIndexed { index, node ->
            val angle = angleStep * index  // Calculate the angle for this node
            node.x = (radius * cos(angle)).toInt()  // Convert polar to Cartesian coordinates
            node.y = (radius * sin(angle)).toInt()
        }

        return dataFrameOf(
            "id" to nodes.map { it.uuid },
            "node" to nodes.map { it.name },
            "x" to nodes.map { it.x },
            "y" to nodes.map { it.y }
        )
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