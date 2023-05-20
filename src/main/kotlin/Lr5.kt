import agentsprovider.InfluencedAgentsProvider
import agentsprovider.RandomAgentsProvider
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import trustmatrixprovider.TrustMatrix
import utils.FormattedPrinter
import utils.RandomUtils
import java.io.File
import kotlin.math.abs

object Lr5 {

    private val mapper = ObjectMapper().apply {
        registerModule(kotlinModule())
        enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    private val task = mapper.readValue(File("lr5.json"), Lr5Task::class.java).also {
        RandomUtils.setSeed(it.seed)
        FormattedPrinter.setScale(it.formatting.scale)
    }

    private val initialAgents = RandomAgentsProvider(
        opinionsRange = task.opinionsRange.minValue until task.opinionsRange.maxValue
    ).agents(task.agentsCount)

    private val trustMatrix = TrustMatrix.createRandom(task.agentsCount).also {
        FormattedPrinter.print("Случайно сгенерированная матрица доверия", it)
    }

    fun run() {
        runConfrontationWithoutInfluence()
        runInfluencedConfrontation()
    }

    private fun runConfrontationWithoutInfluence() {
        println()
        val agents = copyInitialAgents()
        val confrontation = InformationConfrontation(trustMatrix, agents, task.maxEpsilon)
        val resultMatrix = confrontation.perform()
        FormattedPrinter.print("Результирующая матрица доверия", resultMatrix)
        println()
        println("Случайно сгенерированные мнения агентов (без влияния):")
        FormattedPrinter.print("X(0)", copyInitialAgents())
        println("Результирующие мнения агентов (без влияния):")
        FormattedPrinter.print("X(${confrontation.iterations})", agents)
    }

    private fun runInfluencedConfrontation() {
        println()
        val players = task.influencedConfrontation.playersOpinions.mapIndexed { index, opinionRange ->
            Player(index + 1, RandomUtils.instance.nextInt(opinionRange[0], opinionRange[1]).toDouble())
        }
        val influencedAgents = InfluencedAgentsProvider(
            players = players,
            agents = copyInitialAgents(),
            noInfluenceProbability = task.influencedConfrontation.noInfluenceProbability
        ).agents(task.agentsCount)
        for (p in players) {
            println("Агенты игрока ${p.id} (с мнением ${FormattedPrinter.roundDouble(p.opinion)}): ${p.agents.map { it.id }}")
        }
        println("Мнения агентов с учетом влияния:")
        FormattedPrinter.print("X(0)", influencedAgents)
        val confrontation = InformationConfrontation(trustMatrix, influencedAgents, task.maxEpsilon)
        confrontation.perform()
        println("Результирующие мнения агентов с учетом влияния:")
        FormattedPrinter.print("X(${confrontation.iterations})", influencedAgents)
        val agentsInfluencers = influencedAgents.map { agent ->
            players.minByOrNull { player ->
                abs(player.opinion - agent.opinion)
            }
        }
        val influencersCounts = mutableMapOf<Player?, Int>()
        for (influencer in agentsInfluencers) {
            influencersCounts[influencer] = 1 + (influencersCounts[influencer] ?: 0)
        }
        val maxInfluencerCount = influencersCounts.maxOf { it.value }
        val winners = influencersCounts.filter { it.value == maxInfluencerCount }.mapNotNull { it.key }
        for (winner in winners) {
            println("Победитель - игрок ${winner.id} (разница мнения от сформированного - ${FormattedPrinter.roundDouble(abs(winner.opinion - influencedAgents.first().opinion))})")
        }
        val losers = players.toMutableList().apply { removeAll(winners) }
        for (loser in losers) {
            println("Проигравший - игрок ${loser.id} (разница мнения от сформированного - ${FormattedPrinter.roundDouble(abs(loser.opinion - influencedAgents.first().opinion))})")
        }

    }

    private fun copyInitialAgents() = initialAgents.map { it.copy() }
}

fun main() {
    Lr5.run()
}
