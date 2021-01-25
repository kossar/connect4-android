package ee.taltech.connect4

import android.R.array
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
class GameEngine: Parcelable  {
    companion object {
        const val EMPTY = "-"
        const val PLAYER_1 = "x"
        const val PLAYER_2 = "o"
        const val WIN = "win"
        const val CONTINUE = "continue"
    }
    private var board = Array(6) {Array(7) { EMPTY } }
    var firstPlayerMove = true
    private var winCombination = ArrayList<String>()

    fun resetBoard() {
        board = Array(6) {Array(7) { EMPTY } }
    }
    fun move(col: Int): String? {

        if (board[board.size - 1][col] != EMPTY) {
            for (i in 0..board.size){
                    if (board[i][col] != EMPTY) {
                        if (i == 0) {
                            return null
                        }
                        board[i - 1][col] = if (firstPlayerMove) PLAYER_1 else PLAYER_2

                        firstPlayerMove = !firstPlayerMove
                        return (i - 1).toString() + col.toString()
                    }
                }
        }else board[board.size - 1][col] = if (firstPlayerMove) PLAYER_1 else PLAYER_2

        firstPlayerMove = !firstPlayerMove
        return (board.size - 1).toString() + col.toString()

    }

    fun monitorScore(): String {

        if (findFromRows() == WIN ||
            findDiagonals(true) == WIN ||
                findDiagonals(false) == WIN ||
                    findFromCols()== WIN ) {
            return WIN
        }
        return CONTINUE


    }

    private fun count(row: Array<String>, rowIndex: Int?, positions: Array<String>?): String {
        val groups = mutableListOf<Group>()
        var colIndex = 0

        row.forEach {
            if (it != EMPTY) {
                val last = groups.lastOrNull()
                if (last?.value == it) {
                    last.count++
                    if (rowIndex != null) {
                        //Log.d("TAG", "row not null")
                        last.indexes.add(rowIndex.toString() + colIndex.toString())
                    } else if (positions != null){
                       // Log.d("TAG", "positions not null")
                        last.indexes.add(positions[colIndex])
                        //Log.d("TAG", "last: " + last.toString())
                    }

                } else {
                    if (rowIndex != null) {
                        groups.add(Group(it, 1, arrayListOf(rowIndex.toString() + colIndex.toString())))
                    }else if (positions != null) {
                        groups.add(Group(it, 1, arrayListOf(positions[colIndex])))
                    }
                }
                colIndex++
            }
        }
        //Log.d("TAG", groups.toString())
        groups.forEach{
            if (it.count == 4) {
                winCombination = it.indexes
                return WIN
            }
        }
        return CONTINUE
    }


    data class Group(val value: String, var count: Int, var indexes: ArrayList<String>)

    private fun findDiagonals(bottomLeftToUpperRight: Boolean): String {
        var diagonalBoard =  Array(6) {Array(7) { EMPTY } }

        val positionMap = IntArray(7){it + 1}

        if (!bottomLeftToUpperRight) {
            for (row in board.indices) {
                diagonalBoard[row] = board[row].reversedArray()
            }
        } else {
            diagonalBoard = board
        }

        val width = board[0].size
        val height = board.size

        var counter = 0
        var items = Array(board.size){ EMPTY }
        var positions = Array(board.size){ EMPTY }

        for (k in 0..width + height - 2) {
            for (j in 0..k) {
                val i = k - j
                if (i < height && j < width) {
                    items[counter] = diagonalBoard[i][j]
                    if (!bottomLeftToUpperRight) {
                        positions[counter] = i.toString() + (positionMap.reversedArray()[j] - 1).toString()
                    }else {
                        positions[counter] = i.toString() + j.toString()
                    }
                    counter++
                }
            }
            if (count(items, null,  positions) == WIN) {
                return WIN
            }
            //Log.d("TAG", items.contentToString())

            items = Array(board.size){ EMPTY }
            positions = Array(board.size){ EMPTY }
            counter = 0
        }

        return CONTINUE
    }
    private fun findFromCols(): String {
        val flippedBoard = Array(7) {Array(6) { EMPTY } }

        val positionMap = IntArray(6){it + 1}.reversedArray()
        var positions = Array(7) {Array(6) { EMPTY } }
        for (i in board[0].indices){
            for (j in 0 until board.size){
                flippedBoard[i][j] = board[j][i]
                positions[i][j] = (positionMap[j] - 1).toString() + i.toString()
            }
        }

        for (i in flippedBoard.indices) {
            Log.d("TAG", "pos: " + Arrays.toString(positions))
            if (count(flippedBoard[i], null,  positions[i]) == WIN) {
                return WIN
            }



            //Log.d("TAG", "Flipped: " + count(flippedBoard[i], i, null).toString())
        }
        //Log.d("TAG", flippedBoard.contentDeepToString())

        return CONTINUE
    }
    private fun findFromRows(): String {
        for (i in board.indices) {
            if (count(board[i], i,  null) == WIN) {
                return WIN
            }
            //Log.d("TAG", "ROWs: " + count(board[i], i, null).toString())

        }
        return CONTINUE
    }

    fun getBoard(): Array<Array<String>> = board

    fun getWinCombination(): List<String> = winCombination
}