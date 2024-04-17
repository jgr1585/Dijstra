package minimalDemo.json

import com.fasterxml.jackson.annotation.JsonProperty

data class JGraph (
    @JsonProperty("nodes")
    val nodes: List<JNodes>,
    @JsonProperty("edges")
    val edges: List<JEdges>
) {
    data class JNodes(
        val name: String
    )

    data class JEdges(
        val from: String,
        val to: String,
        val weight: Int
    )
}