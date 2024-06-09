package minimalDemo.algo

import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import java.util.*


class DijkstraRelaxation : RelaxationStrategy {
    override fun relax(
        currentNode: Node,
        edge: Edge,
        distances: MutableMap<Node, Int>,
        previousNodes: MutableMap<Node, Node?>,
        priorityQueue: PriorityQueue<Pair<Node, Int>>
    ) {
        val neighbor = edge.target
        val newDistance = distances[currentNode]!! + edge.weight

        if (newDistance < distances[neighbor]!!) {
            distances[neighbor] = newDistance
            previousNodes[neighbor] = currentNode
            priorityQueue.add(neighbor to newDistance)
        }
    }
}