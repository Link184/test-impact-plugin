package dev.sunnyday.test.impact.plugin.task.testimpact

import com.github.lalyos.jfiglet.FigletFont
import dev.sunnyday.test.impact.plugin.domain.graph.ImpactProjectGraph
import dev.sunnyday.test.impact.plugin.domain.model.ImpactProject
import dev.sunnyday.test.impact.plugin.extension.ChangesSource
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault
open class TestImpactTask : DefaultTask() {

    @get:Nested
    internal lateinit var changesSource: ChangesSource

    @get:Input
    internal lateinit var inputImpactGraph: ImpactProjectGraph

    @get:Internal
    internal lateinit var taskOutput: TestImpactTaskOutput

    @get:OutputFile
    internal val outputFile: File
        get() = taskOutput.file

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun run() {
        // TODO: parse packages and collect necessary tests (at least by package)

        val graph = inputImpactGraph.clone()
        markChangedProjects(graph, changesSource.getChangedFiles())

        taskOutput.writeImpactGraph(graph)
    }

    private fun markChangedProjects(
        graph: ImpactProjectGraph,
        changedFilesPaths: List<String>,
    ): Iterable<ImpactProject> {
        val changedProjects = changedFilesPaths
            .mapNotNullTo(mutableSetOf(), graph::getProjectByRelativePath)
        if (changedProjects.isEmpty()) {
            return changedProjects
        }

        logger.lifecycle(FigletFont.convertOneLine("${project.rootProject.name} test impact"))
        logger.lifecycle("[TestImpact] Changed or affected by changes modules:")
        logger.lifecycle("[TestImpact] -----------------------------------------------------------")
        val queue = ArrayDeque(changedProjects)
        while (queue.isNotEmpty()) {
            val project = queue.removeFirst()
            project.hasChanges = true
            logger.lifecycle("[TestImpact] $project")

            graph.getDependentProjects(project).forEach { dependentProject ->
                if (changedProjects.add(dependentProject)) {
                    queue.add(dependentProject)
                }
            }
        }
        logger.lifecycle("[TestImpact] -----------------------------------------------------------")

        return changedProjects
    }
}