import org.ejml.simple.SimpleMatrix

class InformationConfrontation(
    private val trustMatrix: SimpleMatrix,
    private val agents: List<Agent>,
    private val e: Double,
) {

    var iterations = 0
        private set

    private var resultTrustMatrix = trustMatrix

    fun perform(): SimpleMatrix {
        while (!allHaveSameOpinion()) {
            iterate()
        }
        return resultTrustMatrix
    }

    private fun iterate() {
        val x0 = SimpleMatrix(agents.map { it.opinion }.toDoubleArray())
        val x1 = trustMatrix.mult(x0)
        agents.forEachIndexed { index, agent -> agent.opinion = x1[index, 0] }
        iterations++
        resultTrustMatrix = resultTrustMatrix.mult(trustMatrix)
    }

    private fun allHaveSameOpinion(): Boolean {
        val maxOpinion = agents.maxOf { it.opinion }
        val minOpinion = agents.minOf { it.opinion }
        return (maxOpinion - minOpinion) < e
    }

}