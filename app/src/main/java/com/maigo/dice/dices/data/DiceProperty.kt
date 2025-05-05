/** DiceProperty.kt **/

package com.maigo.dice.dices.data

/**
 * サイコロのプロパティ定義
 * @property pieces 同時に振るサイコロの個数
 * @property values サイコロの各面の値（順不同でも可、重複不可）
 *
 * 今後の拡張予定：
 * - 面のラベルを数値以外にする（文字や記号など）
 * - UI設定用の色やサイズ情報を含める
 */
data class DiceProperty(
    val pieces: Int = 1,
    val values: List<Int> = listOf(1, 2, 3, 4, 5, 6)
)
