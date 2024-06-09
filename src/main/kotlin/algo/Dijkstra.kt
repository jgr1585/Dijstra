package minimalDemo.algo

import minimalDemo.objects.Graph
import minimalDemo.objects.graph.Node
import java.util.*

fun dijkstra(graph: Graph, startNode: Node): Pair<Map<Node, Int>, Map<Node, Node?>> {
    val distances = mutableMapOf<Node, Int>()
    val previousNodes = mutableMapOf<Node, Node?>()
    val priorityQueue = PriorityQueue<Pair<Node, Int>>(compareBy { it.second })

    for (node in graph.nodes) {
        distances[node] = if (node == startNode) 0 else Int.MAX_VALUE
        previousNodes[node] = null
        priorityQueue.add(node to distances[node]!!)
    }

    while (priorityQueue.isNotEmpty()) {
        val (currentNode, currentDistance) = priorityQueue.poll()

        for (edge in currentNode.edges) {
            val neighbor = edge.target
            val newDistance = currentDistance + edge.weight

            if (newDistance < distances[neighbor]!!) {
                distances[neighbor] = newDistance
                previousNodes[neighbor] = currentNode
                priorityQueue.add(neighbor to newDistance)
            }
        }
    }

    return distances to previousNodes
}