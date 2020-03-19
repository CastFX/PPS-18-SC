package it.scalachess.core.board

import it.scalachess.core.pieces.{ Bishop, King, Knight, Pawn, Piece, PieceType, Queen, Rook }
import it.scalachess.core.colors.{ Black, Color, White }

/**
 * Functional chess board representation
 * @param pieces a map Position -> Piece.
 */
final case class Board(
    pieces: Map[Position, Piece]
) {

  /**
   * Returns an Option type of the Piece at a certain Position, if present
   * @param pos A position on the Board
   * @return an Option[Piece] with the respective Piece on that Position, None if it's empty
   */
  def pieceAtPosition(pos: Position): Option[Piece] = pieces get pos

  /**
   * Curried function of pieceAt, to get the Piece at the passed coordinates
   * @param col numerical value of column
   * @param row number of the row
   * @return an Option[Piece] with the respective Piece at that coordinates, None if it's empty
   */
  def pieceAtCoordinates(col: Int)(row: Int): Option[Piece] = Position.of(row)(col) flatMap { pieces get }

  /**
   * Retrieves a Piece under a certain position, if it's present
   * @param notation String notation of the Position, e.g "a1", "h8"
   * @return an Option[Piece] with the respective Piece at those coordinates, None if there isn't any
   */
  def pieceAt(notation: String): Option[Piece] = Position.ofNotation(notation) flatMap { pieces get }

}

object Board {
  val width: Int  = 8
  val height: Int = 8

  /**
   * Function to check if a certain position expressed with row and column is inside this board
   * @param row the position's row to be checked
   * @param col the position's column to be checked
   * @return True if this position is within the Board
   */
  def isInside(col: Int, row: Int): Boolean =
    col >= 1 && col <= width && row >= 1 && row <= height

  /**
   * @return the standard 8x8 chess Board with pieces placed in the starting position
   */
  def defaultBoard(): Board = {
    val pieceMap = {
      for (row <- Seq(1, 2, height - 1, height); //for rows 1,2 7,8
           col <- 1 to 8) yield { //for each column [a-h]
        Position
          .of(col)(row) //from the position
          .map({ pos =>
            val color: Color = if (row <= 2) White else Black
            val piece        = Piece(color, initialPieceTypeAtPosition(pos)) //get the starting piece
            (pos, piece) //in a tuple
          })
      }
    }.flatten.toMap

    Board(pieceMap)
  }

  private def initialPieceTypeAtPosition(pos: Position): PieceType =
    pos.row match {
      case 1 | 8 =>
        pos.col match {
          case 1 | 8 => Rook //(a1,a8,h1,h8)
          case 2 | 7 => Knight //(b1,b8,g1,g8)
          case 3 | 6 => Bishop //(c1,c8,f1,f8)
          case 4     => King //(d1,d8)
          case 5     => Queen //(e1,e8)
        }
      case 2 | 7 => Pawn // (a2-h2, a7-h7)
    }
}
