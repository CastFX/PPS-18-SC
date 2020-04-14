package it.scalachess.ai.level

import it.scalachess.core.Color
import it.scalachess.core.board.Board
import it.scalachess.core.logic.moves.FullMove

/**
 * The interface which manages level implementation.
 */
trait Level extends ((Board, Color, Seq[FullMove]) => FullMove) {

  override def apply(board: Board, aiPlayer: Color, history: Seq[FullMove]): FullMove

  protected def moveWithMaxEvaluation(movesEvaluated: Map[FullMove, Double]): FullMove = {
    assert(movesEvaluated.nonEmpty, aiPlayerInCheckmateFailMsg)
    movesEvaluated.find(_._2 == movesEvaluated.values.max).get._1
  }

  protected val othePlayerInCheckmate      = "The other player is in checkmate: wrong usage of AI"
  protected val aiPlayerInCheckmateFailMsg = "The AI player is in checkmate: the game should be already ended"

}
