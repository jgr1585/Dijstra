package minimalDemo.objects.graph

import java.util.UUID

data class Node(
    val name: String,
    val edges: MutableList<Edge> = mutableListOf(),
    val uuid: UUID = UUID.randomUUID(),
    var x: Int = 0,
    var y: Int = 0
) {
    fun addEdge(edge: Edge) {
        edges.add(edge)
    }
}