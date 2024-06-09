package minimalDemo.algo

import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import java.util.*


class PrimRelaxation : RelaxationStrategy {
    override fun relax(
        currentNode: Node,
        edge: Edge,
        distances: MutableMap<Node, Int>,
        previousNodes: MutableMap<Node, Node?>,
        priorityQueue: PriorityQueue<Pair<Node, Int>>
    ) {
        val neighbor = edge.target
        val newWeight = edge.weight

        if (newWeight < distances[neighbor]!!) {
            distances[neighbor] = newWeight
            previousNodes[neighbor] = currentNode
            priorityQueue.add(neighbor to newWeight)
        }
    }
}