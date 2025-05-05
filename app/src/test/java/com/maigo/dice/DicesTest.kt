package com.maigo.dice

import com.maigo.dice.dices.data.DiceProperty
import com.maigo.dice.dices.data.Dices
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*

/**
 * Dices クラスの単体テスト
 * - 複数のpieces構成でサイコロを振った際、結果が正しい形式かどうかを検証
 */
class DicesTest {

    private val dicesList = mutableListOf<Dices>()

    @Before
    fun setUp() {
        for (pieces in 1..20) {
            val property = DiceProperty(pieces = pieces)
            dicesList.add(Dices(property))
        }
    }

    @Test
    fun setProperty() {
        val dice = Dices()
        val custom = DiceProperty(pieces = 3, values = listOf(1, 2))
        dice.setProperty(custom)

        val result = dice.throwDices()
        assertEquals(3, result.size)
        assertTrue(result.all { it == 1 || it == 2 })
    }

    @Test
    fun throwDices() {
        for ((i, dices) in dicesList.withIndex()) {
            val result = dices.throwDices()
            println("dice = ${i + 1}, sum = ${result.sum()}, result = $result")

            // 出目数とpiecesが一致しているか
            assertEquals(i + 1, result.size)
            // 値がすべて正しい範囲にあるか
            assertTrue(result.all { it in 1..6 })
        }
    }
}
