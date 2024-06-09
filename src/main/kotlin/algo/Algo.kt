package minimalDemo.algo

import minimalDemo.objects.Graph
import minimalDemo.objects.graph.Node
import java.util.*


fun runAlgorithm(
    graph: Graph,
    startNode: Node,
    relaxationStrategy: RelaxationStrategy
): Pair<Map<Node, Int>, Map<Node, Node?>> {
    val distances = mutableMapOf<Node, Int>()
    val previousNodes = mutableMapOf<Node, Node?>()
    val priorityQueue = PriorityQueue<Pair<Node, Int>>(compareBy { it.second })

    for (node in graph.nodes) {
        distances[node] = if (node == startNode) 0 else Int.MAX_VALUE
        previousNodes[node] = null
        priorityQueue.add(node to distances[node]!!)
    }

    while (priorityQueue.isNotEmpty()) {
        val (currentNode, _) = priorityQueue.poll()

        for (edge in currentNode.edges) {
            relaxationStrategy.relax(currentNode, edge, distances, previousNodes, priorityQueue)
        }
    }

    return distances to previousNodes
}

fun printAlgoResults(distances: Map<Node, Int>, previousNodes: Map<Node, Node?>) {
    println("Dijkstra-Distances:")
    for ((node, distance) in distances) {
        println("${node.name}: $distance")
    }

    println("\nDijkstra-Paths:")
    for ((node, previousNode) in previousNodes) {
        if (previousNode != null) {
            println("${previousNode.name} -> ${node.name}")
        } else {
            println("${node.name} is the start node.")
        }
    }
}