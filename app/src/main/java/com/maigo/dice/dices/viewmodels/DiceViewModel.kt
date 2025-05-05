/** DiceViewModel.kt **/

package com.maigo.dice.dices.viewmodels

import androidx.lifecycle.ViewModel
import com.maigo.dice.dices.data.DiceProperty

/**
 * DiceView 用の設定情報を保持する ViewModel。
 * 今後は外部の設定画面からこのデータを更新し、
 * DiceFragment に反映させる設計とする。
 */
class DiceViewModel : ViewModel() {
    var diceProperty: DiceProperty = DiceProperty()
    var lastResults: List<Int> = listOf()

    // 拡張候補（未実装）:
    // var diceSides = 6
    // var animationDuration = 1000L
    // var colorScheme = DiceColor(...)
}
