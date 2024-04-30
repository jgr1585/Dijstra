package minimalDemo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import minimalDemo.json.JGraph
import minimalDemo.objects.Graph
import minimalDemo.objects.graph.Edge
import minimalDemo.objects.graph.Node
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.coord.coordCartesian
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.geom.extras.arrow
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomSegment
import org.jetbrains.letsPlot.geom.geomText
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.jfx.plot.component.DefaultPlotPanelJfx
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.themes.themeVoid
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.filechooser.FileNameExtensionFilter

fun main() {

    val graph = SimpleObjectProperty<Graph>()
    val plot = SimpleObjectProperty<Plot>()
    val eulerText = JLabel("Euler: Maybe???")
    val controller = Controller(plot)
    val mapper = jacksonObjectMapper()

    val nodes = mutableListOf(
        Node("Living\nThings"),
        Node("Animals"),
        Node("Plants"),
        Node("Dogs"),
        Node("Cows"),
        Node("Herbs")
    )

    nodes[1].addEdge(Edge(nodes[0], 1))
    nodes[2].addEdge(Edge(nodes[0], 1))
    nodes[3].addEdge(Edge(nodes[1], 1))
    nodes[4].addEdge(Edge(nodes[1], 1))
    nodes[4].addEdge(Edge(nodes[5], 1))
    nodes[5].addEdge(Edge(nodes[2], 1))

    graph.addListener { _, _, newValue ->
        val nodesDf = newValue.getDataFrameNodes()
        val edgesDf = newValue.getDataFrameEdges(nodesDf)
        val plotSize = mapOf(
            "minX" to newValue.nodes.minOf { it.x } - 80000,
            "maxX" to newValue.nodes.maxOf { it.x } + 80000,
            "minY" to newValue.nodes.minOf { it.y } - 80000,
            "maxY" to newValue.nodes.maxOf { it.y } + 80000
        )

        println(plotSize)

        plot.set(letsPlot(nodesDf.toMap()) { x = "x"; y = "y" } +
                geomSegment(data = edgesDf.toMap(), sizeEnd = 30, arrow = arrow()) {
                    x = "x"
                    y = "y"
                    xend = "xTo"
                    yend = "yTo"
                    color = "relation"
                } +
                geomPoint(color = "#2166ac", fill = "#d1e5f0", shape = 21, size = 25) +
                scaleColorManual(listOf("#2166ac", "#d6604d")) +
                geomText() { label = "node" } +
                coordCartesian(plotSize["minX"] to plotSize["maxX"], plotSize["minY"] to plotSize["maxY"]) +
                themeVoid()
        )

        controller.rebuildPlotComponent()
        eulerText.text = "Euler: ${if (newValue.hasEulerPath()) "Yes" else "No"}"

        if (newValue.hasEulerPath()) {
            val eulerPath = newValue.getEulerPath()
            println("Euler Path: ${eulerPath.joinToString { it.name }}")
        }
    }

    graph.set(Graph(nodes))

    val window = JFrame("Example App")
    window.defaultCloseOperation = EXIT_ON_CLOSE
    window.contentPane.layout = BoxLayout(window.contentPane, BoxLayout.Y_AXIS)

    // Add reload button
    val controlPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        add(JButton("Reload").apply {
            addActionListener {
                controller.rebuildPlotComponent()
            }
        })

        add(JButton("Load").apply {
            addActionListener {
                //Open a file chooser
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    isAcceptAllFileFilterUsed = false
                    addChoosableFileFilter(FileNameExtensionFilter("Json File", "json"))

                    currentDirectory = java.io.File(".")
                }

                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    val jGraph: JGraph = mapper.readValue(file, JGraph::class.java)

                    graph.set(Graph.fromJson(jGraph))

                    println("Selected file: $file")
                }
            }
        })

        add(eulerText)
    }
    window.contentPane.add(controlPanel)

    // Add plot panel
    val plotContainerPanel = JPanel(GridLayout())
    window.contentPane.add(plotContainerPanel)

    controller.plotContainerPanel = plotContainerPanel
    controller.rebuildPlotComponent()

    SwingUtilities.invokeLater {
        window.pack()
        window.size = Dimension(850, 400)
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}

private class Controller(
    private val plot: SimpleObjectProperty<Plot>
) {
    var plotContainerPanel: JPanel? = null

    fun rebuildPlotComponent() {
        plotContainerPanel?.let {
            val container = plotContainerPanel!!
            // cleanup
            for (component in container.components) {
                if (component is Disposable) {
                    component.dispose()
                }
            }
            container.removeAll()

            // build
            container.add(createPlotPanel())
            container.parent?.revalidate()
        }
    }

    fun createPlotPanel(): JPanel {
        // Make sure JavaFX event thread won't get killed after JFXPanel is destroyed.
        Platform.setImplicitExit(false)

        val rawSpec = plot.get()?.toSpec() ?: return JPanel()
        val processedSpec = MonolithicCommon.processRawSpecs(rawSpec, frontendOnly = false)

        return DefaultPlotPanelJfx(
            processedSpec = processedSpec,
            preserveAspectRatio = false,
            preferredSizeFromPlot = false,
            repaintDelay = 10,
        ) { messages ->
            for (message in messages) {
                println("[Example App] $message")
            }
        }
    }
}