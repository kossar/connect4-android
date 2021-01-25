package ee.taltech.connect4


import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.stats.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    var redFirst = true
    var firstPlayerMove = true
    var gameStarted = false
    private var buttons = ArrayList<Button>()
    private lateinit var board: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        board = GameEngine()


        loopThrough(findViewById(R.id.boardButtons))
        addTags(buttons.sortedWith(compareBy { it.id }))

    }


    fun buttonPlayerOnClick(view: View) {

        if (!gameStarted) {
            setUi()
            redFirst = !redFirst
        }
    }

    private fun setUi() {
        Log.d(TAG, "setUi")
        Log.d(TAG, "Redfirst: $redFirst")
        val firstButton = findViewById<Button>(R.id.buttonColorP1)
        val secondButton = findViewById<Button>(R.id.buttonColorP2)
        if (!redFirst) {
            firstButton.setBackgroundResource(R.drawable.game_button_rounded_yellow)
            secondButton.setBackgroundResource(R.drawable.game_button_rounded_red)

            textViewPlayer1.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkYellow
                )
            )
            textViewPlayer1.text = getString(R.string.yellow)

            textViewPlayer2.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkRed
                )
            )
            textViewPlayer2.text = getString(R.string.red)
        } else {
            firstButton.setBackgroundResource(R.drawable.game_button_rounded_red)
            secondButton.setBackgroundResource(R.drawable.game_button_rounded_yellow)

            textViewPlayer1.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkRed
                )
            )
            textViewPlayer1.text = getString(R.string.red)

            textViewPlayer2.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkYellow
                )
            )
            textViewPlayer2.text = getString(R.string.yellow)
        }
    }

    private fun loopThrough(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val button = parent.getChildAt(i)

            if (button is Button) {
                buttons.add(button)
            } else if (button is ViewGroup) loopThrough(button)
        }
    }

    fun newGameOnClick(view: View) {
        gameStarted = !gameStarted
        resetBoard()
        nextTurnText()
    }

    fun gameButtonOnClick(view: View) {
        if (gameStarted) {
            with(view as Button) {
                val tag: String = buttons.single { it.id == view.id }.tag as String

                Log.d(TAG, tag[0].toString() + " " + tag[1].toString())
                val instruction = board.move(tag[1].toString().toInt())

                if (instruction == null) {
                    disableColumns(tag)
                } else {
                    styleMovedButton(instruction)
                    //redFirst = !redFirst
                    //firstPlayerMove = !firstPlayerMove
                }

            }
            if (board.monitorScore() == "win") {

                styleWinCombination()
                winnerIs()
                //disableBoard()
                gameStarted = !gameStarted
                firstPlayerMove = !firstPlayerMove

            } else {
                firstPlayerMove = !firstPlayerMove
                nextTurnText()
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, textViewScoreP1.text.toString())
        Log.d(TAG, "onSaveInstanceState")
        Log.d(TAG, "Redfirst: $redFirst")
        outState.putParcelable("board", board)
        outState.putSerializable("buttons", buttons)
        outState.putBoolean("redFirst", redFirst)
        outState.putBoolean("firstPlayerMove", firstPlayerMove)
        outState.putBoolean("gameStarted", gameStarted)
        outState.putString("player1score", textViewScoreP1.text.toString())
        outState.putString("player2score", textViewScoreP2.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        redFirst = savedInstanceState.getBoolean("redFirst")
        firstPlayerMove = savedInstanceState.getBoolean("firstPlayerMove")
        gameStarted = savedInstanceState.getBoolean("gameStarted")
        textViewScoreP1.text = savedInstanceState.getString("player1score")
        textViewScoreP1.text = savedInstanceState.getString("player2score")
        board = savedInstanceState.getParcelable<GameEngine>("board")!!
        // buttons = savedInstanceState.getSerializable("buttons") as ArrayList<Button>
        setUi()

        if (gameStarted) {
            setBoardFromInstanceState()
            nextTurnText()
        }
    }

    private fun setBoardFromInstanceState() {
        for (i in board.getBoard().indices) {
            for (j in board.getBoard()[i].indices) {
                buttons.forEach {
                    if (it.tag == i.toString() + j.toString()) {
                        if (board.getBoard()[i][j] == "x" && redFirst || board.getBoard()[i][j] == "o" && !redFirst) {
                            Log.d(TAG, "is x")
                            it.setBackgroundResource(R.drawable.game_button_rounded_red)
                        } else if (board.getBoard()[i][j] == "o" && redFirst || board.getBoard()[i][j] == "x" && !redFirst) {
                            it.setBackgroundResource(R.drawable.game_button_rounded_yellow)
                        }
                    }
                }
            }
        }
    }

    fun buttonResetOnClick(view: View) {
        if (gameStarted) {
            resetBoard()
            gameStarted = !gameStarted
        } else {
            resetBoard()
            textViewScoreP1.text = getString(R.string.score_hint)
            textViewScoreP2.text = getString(R.string.score_hint)
        }
    }

    private fun resetBoard() {
        for (button in buttons) {
            button.setBackgroundResource(R.drawable.game_button_rounded_black)
        }
        board.resetBoard()
    }

    private fun winnerIs() {
        val winner: String
        var player1Score = Integer.valueOf(textViewScoreP1.text.toString())
        var player2Score = Integer.valueOf(textViewScoreP2.text.toString())

        if (firstPlayerMove) {
            winner = getString(R.string.red) + " " + getString(R.string.winner)
            player1Score += 1
            textViewScoreP1.text = player1Score.toString()

        } else {
            winner = getString(R.string.yellow) + " " + getString(R.string.winner)
            player2Score += 1
            textViewScoreP2.text = player2Score.toString()
        }

        textViewInfo.text = winner

    }

    private fun styleWinCombination() {
        for (combination in board.getWinCombination()) {

            Log.d("TAG", "comb: " + combination)
            for (button in buttons) {
                if (button.tag == combination) {
                    button.setBackgroundResource(R.drawable.game_button_rounded_win)
                }
            }
        }
    }

    private fun nextTurnText() {
        val text = getString(R.string.next_turn) + " "
        var turn = ""
        if ((redFirst && firstPlayerMove) || (!redFirst && !firstPlayerMove)) {
            turn = getString(R.string.red)
            textViewInfo.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkRed
                )
            )
        } else {
            turn = getString(R.string.yellow)
            textViewInfo.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorDarkYellow
                )
            )
        }

        textViewInfo.text = text + turn
    }

    private fun disableBoard() {
        for (button in buttons) {
            button.isEnabled = false
            button.isClickable = false
        }
    }

    private fun styleMovedButton(instruction: String) {
        Log.d(TAG, "styleMovedButton $instruction")
        textViewInfo.text
        for (button in buttons) {
            if (button.tag == instruction) {
                if ((redFirst && firstPlayerMove) || (!redFirst && !firstPlayerMove)) {
                    button.setBackgroundResource(R.drawable.game_button_rounded_red)
                } else
                    button.setBackgroundResource(R.drawable.game_button_rounded_yellow)
            }
        }
    }

    private fun disableColumns(tag: String) {
        for (button in buttons) {
            if (button.tag.toString()[1] == tag[1]) {
                button.isEnabled = false
                button.isClickable = false
            }
        }
    }

    private fun addTags(buttons: List<Button>) {
        var index = 0
        for (row in 0..5) {
            for (col in 0..6) {
                buttons.elementAtOrNull(index)?.tag = row.toString() + col.toString()
                index++
            }
        }
    }


}