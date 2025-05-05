/** MainActivity.kt **/

package com.maigo.dice

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.maigo.dice.dices.ui.DiceFragment
import com.maigo.dice.dices.data.DiceProperty
import com.maigo.dice.dices.data.Dices

class MainActivity : AppCompatActivity() {

    private val dices = Dices()
    private var diceFragment: DiceFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ステータスバー・ナビゲーションバーの余白を考慮
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.start_button).setOnClickListener {
            showDice()
        }
    }

    /**
     * サイコロフラグメントを表示してダイスを振る
     */
    private fun showDice() {
        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
        if (frameLayout.visibility == View.GONE) {
            frameLayout.visibility = View.VISIBLE
        }

        // ViewModelに設定反映
        val diceProperty = DiceProperty(pieces = 1, values = listOf(1, 2, 3, 4, 5, 6))
        dices.setProperty(diceProperty)
        val results = dices.throwDices()

        val fragment = DiceFragment.newInstance()
        diceFragment = fragment

        replaceFragment(fragment)

        // サイコロ表示指示
        diceFragment?.rollDiceTo(results.first(), diceProperty.values.size)
    }

    /**
     * フレーム内にフラグメントを表示
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            commit()
        }
    }
}
