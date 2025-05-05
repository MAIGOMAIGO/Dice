/** DiceFragment.kt **/

package com.maigo.dice.dices.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.addCallback
import com.maigo.dice.R
import com.maigo.dice.dices.viewmodels.DiceViewModel
import com.maigo.dice.dices.data.Dices

/**
 * DiceGLView を表示するフラグメント。
 * ボタンを押すとサイコロが回転します。
 */
class DiceFragment : Fragment() {

    companion object {
        fun newInstance() = DiceFragment()
    }

    private val viewModel: DiceViewModel by viewModels()
    private val dices = Dices() // Dicesインスタンスを保持
    private lateinit var diceGLView: DiceGLView

    // diceGLViewが未初期化の間にrollDiceToが呼ばれたときのために保持
    private var pendingRoll: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OS戻るボタンでFragmentを削除
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.beginTransaction().remove(this@DiceFragment).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_dice, container, false)
        diceGLView = root.findViewById(R.id.dice_gl_view)

        return root
    }

    override fun onResume() {
        super.onResume()
        diceGLView.onResume()
    }

    override fun onPause() {
        super.onPause()
        diceGLView.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<FrameLayout>(R.id.dice_overlay)
        container.setOnClickListener {
            // isRolledがfalseのままなので後で修正
            parentFragmentManager.beginTransaction().remove(this).commit()
            // 間違えて押下するの防止用
//            if (diceGLView.isRolled()) {
//                parentFragmentManager.beginTransaction().remove(this).commit()
//            }
        }

        // pendingRoll がある場合はここで実行
        pendingRoll?.let { (face, sides) ->
            rollDiceTo(face, sides)
            pendingRoll = null
        }
    }

    /**
     * ダイスを指定の出目で振る。
     * Viewが未初期化ならpendingRollに保留しておき、onViewCreatedで実行される。
     * 呼び出し元（MainActivityなど）から使われる想定。
     */
    fun rollDiceTo(face: Int, sides: Int) {
        if (::diceGLView.isInitialized) {
            dices.setProperty(viewModel.diceProperty)
            diceGLView.rollTo(face, sides)
            viewModel.lastResults = listOf(face)
        } else {
            pendingRoll = face to sides
        }
    }
}
