package utils

import Agent
import org.ejml.simple.SimpleMatrix
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

object FormattedPrinter {

    private var SCALE = 3

    fun setScale(scale: Int) {
        SCALE = scale
    }

    fun print(label: String?, agents: List<Agent>) {
        printLabel(label)
        println(agents.joinToString(prefix = "(", postfix = ")") { roundDouble(it.opinion) })
    }

    fun print(label: String?, trustMatrix: SimpleMatrix) {
        printLabel(label, true)
        val numWidth = maxNumWidth(trustMatrix)
        for (row in 0 until trustMatrix.numRows) {
            printRow(trustMatrix.getRow(row), numWidth)
        }
    }

    private fun maxNumWidth(trustMatrix: SimpleMatrix): Int {
        val maxNum = roundDouble(trustMatrix.elementMax())
        val minNum = roundDouble(trustMatrix.elementMin())
        return max(maxNum.length, minNum.length)
    }

    private fun printRow(row: SimpleMatrix, numWidth: Int) {
        for (col in 0 until row.numCols) {
            print(formatDouble(row[col], numWidth))
            print("  ")
        }
        println()
    }

    fun roundDouble(n: Double, roundIfInt: Boolean = true): String {
        val str = BigDecimal.valueOf(n).setScale(SCALE, RoundingMode.HALF_EVEN).toString()
        var lastNonZeroIndex = str.length - 1
        while (lastNonZeroIndex > 0 && str[lastNonZeroIndex] == '0') {
            lastNonZeroIndex--
        }
        if (roundIfInt && lastNonZeroIndex > 0 && str[lastNonZeroIndex] == '.') {
            lastNonZeroIndex--
        }
        val result = str.substring(0, lastNonZeroIndex + 1)
        val pointIndex = result.lastIndexOf('.')
        if (pointIndex < 0) {
            return result
        }
        val scale = result.length - 1 - pointIndex
        return result + "0".repeat(SCALE - scale)
    }

    private fun formatDouble(n: Double, width: Int): String {
        val str = roundDouble(n, false)
        return " ".repeat(max(0, width - str.length)) + str
    }

    private fun printLabel(label: String?, newLine: Boolean = false) {
        label?.let {
            if (newLine) {
                println("$it:")
            } else {
                print("$it: ")
            }
        }
    }
}