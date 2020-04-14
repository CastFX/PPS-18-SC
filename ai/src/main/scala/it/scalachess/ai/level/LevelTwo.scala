package it.scalachess.ai.level

import it.scalachess.core.Color
import it.scalachess.core.board.Board
import it.scalachess.core.logic.moves.FullMove
import it.scalachess.core.logic.moves.generators.MoveGenerator

/**
 * The level two AI plays the best move following two principles:
 * [1] it cares about value based on piece importance (it uses the same evaluation of level one);
 * [2] it analyse all the possible next moves, relying on the minimax algorithm.
 */
final case class LevelTwo() extends Level {

  private val levelOne       = LevelOne()
  private val minimaxDepth   = 3
  private val checkmateValue = 9999.0

  override def apply(board: Board, aiPlayer: Color, history: Seq[FullMove]): FullMove =
    moveWithMaxEvaluation(generateMovesWithMinimaxEval(board, aiPlayer, history, minimaxDepth))

  /**
   * Generates the moves and their evaluation using the minimax algorithm.
   * @param board the board on which computes the move generation and evaluation
   * @param aiPlayer the color of the AI player
   * @param history the moves history played on the board
   * @param depth the depth of the minimax algorithm
   * @return the map containing the moves and the relative evaluations
   */
  private[level] def generateMovesWithMinimaxEval(board: Board,
                                                  aiPlayer: Color,
                                                  history: Seq[FullMove],
                                                  depth: Int): Map[FullMove, Double] = {
    require(depth > 0, "The minimax algorithm should computes at least the consequences of his first future move")
    new MoveGenerator(board, aiPlayer, history)
      .allMoves()
      .map(fullMove => {
        fullMove -> minimax(board(fullMove.validMove.boardChanges),
                            history :+ fullMove,
                            depth - 1,
                            aiPlayer.other,
                            aiPlayer)
      })
      .toMap
  }

  /**
   * Evaluates a board relying on the minimax algorithm.
   * @param board the board to evaluate
   * @param history the moves history played on the board
   * @param depth the depth of the minimax algorithm
   * @param activePlayer the active player during the current minimax turn simulation
   * @param aiPlayerWhichMaximize the color of the player that want to maximize the minimax evaluation (AI player)
   * @return the evaluation of the board
   */
  def minimax(board: Board,
              history: Seq[FullMove],
              depth: Int,
              activePlayer: Color,
              aiPlayerWhichMaximize: Color): Double = {
    def recallMinimax(possibleMoves: List[FullMove],
                      board: Board,
                      history: Seq[FullMove],
                      depth: Int,
                      activePlayer: Color,
                      aiPlayerWhichMaximize: Color): List[Double] =
      possibleMoves
        .map(move => {
          minimax(board(move.validMove.boardChanges),
                  history :+ move,
                  depth - 1,
                  activePlayer.other,
                  aiPlayerWhichMaximize)
        })
    depth match {
      case 0 => levelOne.evaluateBoardByPieceValue(board, aiPlayerWhichMaximize)
      case _ =>
        val possibleMoves = new MoveGenerator(board, activePlayer, history).allMoves()
        if (possibleMoves.isEmpty) {
          activePlayer match {
            case `aiPlayerWhichMaximize` => -checkmateValue // the move brings the ai player into checkmate
            case _                       => checkmateValue
          }
        } else {
          activePlayer match {
            case `aiPlayerWhichMaximize` =>
              recallMinimax(possibleMoves, board, history, depth, activePlayer, aiPlayerWhichMaximize).max
            case _ =>
              recallMinimax(possibleMoves, board, history, depth, activePlayer, aiPlayerWhichMaximize).min
          }
        }
    }
  }

}
