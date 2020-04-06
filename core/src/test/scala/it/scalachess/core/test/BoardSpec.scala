package it.scalachess.core.test

import it.scalachess.core.board.Board.BoardChanges
import it.scalachess.core.{ Black, White }
import it.scalachess.core.board.{ Board, Position }
import it.scalachess.core.pieces.{ Bishop, King, Knight, Pawn, Piece, PieceType, Queen, Rook }
import org.scalatest.{ FlatSpec, Inspectors, Matchers, OptionValues }

class BoardSpec extends FlatSpec with Matchers with Inspectors with OptionValues {

  val initialBoard: Board = Board.defaultBoard()
  val piecesPerType: Map[PieceType, Int] = Map(
    Pawn   -> 16,
    Rook   -> 4,
    Knight -> 4,
    Bishop -> 4,
    King   -> 2,
    Queen  -> 2
  )
  val pawnRowsNumbers: Seq[Int]  = Seq(2, 7)
  val majorRowsNumbers: Seq[Int] = Seq(1, 8)
  val whiteRowsNumbers: Seq[Int] = Seq(1, 2)
  val blackRowsNumbers: Seq[Int] = Seq(7, 8)
  val pieceRowsNumbers: Seq[Int] = whiteRowsNumbers union blackRowsNumbers

  "A standard board" should
  s"have ${piecesPerType(Pawn)} pawns, " +
  s"${piecesPerType(Rook)} rooks, " +
  s"${piecesPerType(Knight)} knights, " +
  s"${piecesPerType(Bishop)} bishops, " +
  s"${piecesPerType(King)} kings, " +
  s"${piecesPerType(Queen)} queens" in {

    val piecesCounts = initialBoard.pieces.values
      .map(_.pieceType)
      .groupBy(identity)
      .mapValues(_.size)
      .toSet

    piecesCounts should equal(piecesPerType.toSet)
  }

  it should "have half pieces black and half pieces white" in {
    val halfPiecesPerType = piecesPerType.map { case (p, n) => (p, n / 2) }.toSet

    val whitePiecesCount = initialBoard.pieces.values
      .filter(_.color === White)
      .map(_.pieceType)
      .groupBy(identity)
      .mapValues(_.size)
      .toSet

    whitePiecesCount should equal(halfPiecesPerType)
  }

  it should "have only pawns in row 2 and 7" in {
    val pieceTypesInRow2_7 = initialBoard.pieces
      .filter { case (pos, _) => pawnRowsNumbers.contains(pos.row) }
      .values
      .map { _.pieceType }

    all(pieceTypesInRow2_7) should be(Pawn)

  }

  it should "have row 1 and 8 as Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook" in {
    val correctRow = Seq(Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook)

    val rows_1_8 = initialBoard.pieces.toSeq
      .filter { case (pos, _) => majorRowsNumbers.contains(pos.row) }
      .sortBy(_._1.col)
      .partition { case (pos, _) => pos.row == 1 }

    val pieceTypesInRow1 = rows_1_8._1.map(_._2.pieceType)
    val pieceTypesInRow8 = rows_1_8._2.map(_._2.pieceType)
    pieceTypesInRow1 should equal(correctRow)
    pieceTypesInRow8 should equal(correctRow)

  }

  it should "have all white pieces in row 1-2 and black pieces in row 7-8" in {
    val whiteAndBlackPieces = initialBoard.pieces.partition { case (_, piece) => piece.color === White }
    val whitePieces         = whiteAndBlackPieces._1.toSeq
    val blackPieces         = whiteAndBlackPieces._2.toSeq

    forAll(whitePieces) {
      case (pos, piece) =>
        whiteRowsNumbers should contain(pos.row)
        piece.color should be(White)
    }

    forAll(blackPieces) {
      case (pos, piece) =>
        blackRowsNumbers should contain(pos.row)
        piece.color should be(Black)
    }
  }

  it should "have all initial positions in range [1,8]" in {
    forAll(initialBoard.pieces.keys) { pos =>
      pos.row should (be >= 1 and be <= 8)
      pos.row should (be >= 1 and be <= 8)
    }
  }

  it should "have pieces in rows 1,2,7,8" in {
    forAll(initialBoard.pieces.keys) { pos =>
      pieceRowsNumbers should contain(pos.row)
    }
  }

  "Apply the promotion move" should "just moves a piece and promote it, on a standard board" in {
    val pawnToPromotedPosFrom = Position(1, 2)
    val pawnToPromotedPosTo   = Position(1, 3)
    val queenPiece            = Piece(White, Queen)
    val board                 = Board.defaultBoard()
    val boardAfterPromotion   = board(BoardChanges(Map(pawnToPromotedPosTo -> queenPiece), Set(pawnToPromotedPosFrom)))
    boardAfterPromotion.pieceAtPosition(pawnToPromotedPosTo).value should equal(queenPiece)
    boardAfterPromotion.pieceAtPosition(pawnToPromotedPosFrom) should equal(None)
  }

  "Apply the castling move" should "just shifts the two piece involved, on a standard board" in {
    val king          = Piece(White, King)
    val kingPosition  = Position(5, 1)
    val kingFinalPos  = Position(7, 1)
    val rook          = Piece(White, Rook)
    val rookPosition  = Position(8, 1)
    val rookFinalPos  = Position(6, 1)
    val standardBoard = Board.defaultBoard()
    val boardAfterCastling =
      standardBoard(BoardChanges(Map(kingFinalPos -> king, rookFinalPos -> rook), Set(kingPosition, rookPosition)))
    boardAfterCastling.pieceAtPosition(rookFinalPos).value should equal(rook)
    boardAfterCastling.pieceAtPosition(kingFinalPos).value should equal(king)
  }

  "Apply the enPassant move" should "moves the piece moved and captured the other" in {
    val pawnMoved       = Piece(White, Pawn)
    val pawnPosFrom     = Position(1, 2)
    val pawnPosTo       = Position(2, 3)
    val pawnCapturedPos = Position(1, 7)
    val standardBoard   = Board.defaultBoard()
    val boardAfterEnPassant =
      standardBoard.apply(BoardChanges(Map(pawnPosTo -> pawnMoved), Set(pawnPosFrom, pawnCapturedPos)))
    boardAfterEnPassant.pieceAtPosition(pawnPosTo).value should equal(pawnMoved)
    boardAfterEnPassant.pieceAtPosition(pawnCapturedPos) should equal(None)
  }
}