package minimalDemo.algo

import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import java.util.*

interface RelaxationStrategy {
    fun relax(
        currentNode: Node,
        edge: Edge,
        distances: MutableMap<Node, Int>,
        previousNodes: MutableMap<Node, Node?>,
        priorityQueue: PriorityQueue<Pair<Node, Int>>
    )
}