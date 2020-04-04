package it.scalachess.core

import it.scalachess.core.board.Board
import it.scalachess.core.logic.moves.FullMove
import it.scalachess.core.logic.MoveValidator
import it.scalachess.core.parser.Parser.AlgebraicParser
import scalaz.{ Failure, Success, Validation }

/**
 * Functional Representation of a game of Chess
 * @param board The board which contains all the chess pieces
 * @param player The Player (White/Black) who is moving
 * @param turn The turn number of this game
 * @param gameStatus the status of the game
 * @param moveHistory the history of the moves of this chess game
 */
final case class ChessGame(
    board: Board,
    player: Color,
    turn: Int,
    gameStatus: GameStatus,
    moveHistory: Seq[FullMove]
) {
  lazy val isKingInCheck: Boolean = moveHistory.last.resultsInCheck

  def apply(move: String): Validation[String, ChessGame] = gameStatus match {
    case Ongoing =>
      AlgebraicParser.parse(move) match {
        case None => Failure("Not algebraic format, insert another move")
        case Some(parsedMove) =>
          MoveValidator(board, player)(parsedMove) match {
            case Success(fullMove) =>
              val nextBoard = board(fullMove.validMove.boardChanges)
              val result    = if (fullMove.resultsInCheckmate) Win(player) else Ongoing
              Success(ChessGame(nextBoard, player.other, turn + 1, result, moveHistory :+ fullMove))
            case error: Failure[String] => error
          }
      }
    case _ => Failure("The game is not ongoing")
  }
}

object ChessGame {

  /**
   * Initialize a standard game of chess with all the piece in the starting positions
   * @return An initialized ChessGame instance
   */
  def standard(): ChessGame =
    ChessGame(Board.defaultBoard(), White, 0, Ongoing, Seq())
}
