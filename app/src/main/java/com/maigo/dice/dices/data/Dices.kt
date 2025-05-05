/** Dices.kt **/

package com.maigo.dice.dices.data

import java.security.SecureRandom

/**
 * 複数のサイコロを扱うロジッククラス。
 * 指定されたプロパティ（個数・面の構成）に基づいて、乱数で振る。
 *
 * スレッドセーフな実装（@Synchronized）
 */
class Dices(property: DiceProperty? = null) {

    private var _property = property ?: DiceProperty()
    private val secureRandom = SecureRandom()

    /**
     * サイコロの設定を変更する。
     */
    @Synchronized
    fun setProperty(property: DiceProperty) {
        this._property = property
    }

    /**
     * サイコロを振ってランダムな出目を返す。
     * @return 出た目のリスト（サイズはpiecesと等しい）
     */
    @Synchronized
    fun throwDices(): List<Int> {
        return List(_property.pieces) {
            val index = secureRandom.nextInt(_property.values.size)
            _property.values[index]
        }
    }
}
