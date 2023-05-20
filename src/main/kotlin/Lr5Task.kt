data class Lr5Task(
    val seed: Int,
    val agentsCount: Int,
    val opinionsRange: OpinionsRange,
    val maxEpsilon: Double,
    val influencedConfrontation: InfluencedConfrontation,
    val formatting: Formatting,
) {
    data class Formatting(
        val scale: Int
    )

    data class InfluencedConfrontation(
        val playersOpinions: List<List<Int>>,
        val noInfluenceProbability: Double,
    )

    data class OpinionsRange(
        val minValue: Int,
        val maxValue: Int,
    )
}