package it.scalachess.core.test

import it.scalachess.core.{ Black, ChessGame, Draw, Ongoing, White, Win }
import it.scalachess.core.board.{ Board, Position }
import it.scalachess.core.logic.moves.{ FullMove, ValidSimpleMove }
import it.scalachess.core.pieces.{ King, Knight, Pawn, Piece, Rook }
import org.scalatest.{ FlatSpec, Matchers, OptionValues }
import it.scalachess.core.test.ChessGameFailureMatcher.generateFailure

class ChessGameSpec extends FlatSpec with Matchers with OptionValues {
  var simpleGame: ChessGame              = ChessGame.standard()
  var testKingConstraintsGame: ChessGame = ChessGame.standard()
  var foolMateGame: ChessGame            = ChessGame.standard()
  var scholarMateGame: ChessGame         = ChessGame.standard()
  var simpleGameTurnCounter              = 0

  "A standard ChessGame" should "have a non-empty board" in {
    assert(simpleGame.board.pieces.nonEmpty)
  }

  it should "have a starting white player at turn 0" in {
    simpleGame.player should equal(White)
    simpleGame.turn should be(simpleGameTurnCounter)
  }

  it should "not accept a move with an illegal format" in {
    val illegalFormatMove = "aa2 a3"
    simpleGame(illegalFormatMove) should generateFailure
  }

  it should "not allows player to move an opponent piece" in {
    val blackPawnMove = "a7 a6"
    simpleGame(blackPawnMove) should generateFailure
  }

  it should "not allows a player captures his own pieces" in {
    val queenCapturesPawn = "Qxd2"
    simpleGame(queenCapturesPawn) should generateFailure
  }

  it should "not allows specific pieces pass through other pieces" in {
    val queenPassThroughPawn = "Qd3"
    simpleGame(queenPassThroughPawn) should generateFailure
  }

  it should "apply a move correctly" in {
    val whitePawnMove          = "a4"
    val whitePawnFinalPosition = Position(1, 4)
    simpleGame(whitePawnMove).toOption.value.board.pieceAtPosition(whitePawnFinalPosition).value should equal(
      Piece(White, Pawn))
  }

  it should "must alternate turns between players" in {
    val whitePawnMove = "a3"
    val blackPawnMove = "a6"
    simpleGame = simpleGame(whitePawnMove).toOption.value
    simpleGameTurnCounter += 1
    simpleGame.turn should be(simpleGameTurnCounter)
    simpleGame.player should equal(Black)
    simpleGame = simpleGame(blackPawnMove).toOption.value
    simpleGameTurnCounter += 1
    simpleGame.turn should be(simpleGameTurnCounter)
    simpleGame.player should equal(White)
  }

  "Build a game where the king " should "not being able do some move because it's under check" in {
    val whitePawnMove               = "e4"
    val firstBlackPawnMove          = "e5"
    val whiteBishopMove             = "Bd3"
    val secondBlackPawnMove         = "d5"
    val secondWhitePawnMove         = "f4"
    val blackQueenMove              = "Qh4+"
    val whiteKingMoveNotAllowed     = "Kf2"
    val whiteKingMoveAllowed        = "Ke2"
    val anotherWhiteKingMoveAllowed = "Kf1"
    testKingConstraintsGame = testKingConstraintsGame(whitePawnMove).toOption.value
    testKingConstraintsGame = testKingConstraintsGame(firstBlackPawnMove).toOption.value
    testKingConstraintsGame = testKingConstraintsGame(whiteBishopMove).toOption.value
    testKingConstraintsGame = testKingConstraintsGame(secondBlackPawnMove).toOption.value
    testKingConstraintsGame = testKingConstraintsGame(secondWhitePawnMove).toOption.value
    testKingConstraintsGame = testKingConstraintsGame(blackQueenMove).toOption.value
    testKingConstraintsGame.isKingInCheck shouldBe true
    testKingConstraintsGame.gameStatus should be(Ongoing)
    testKingConstraintsGame(whiteKingMoveNotAllowed) should generateFailure
    testKingConstraintsGame(whiteKingMoveAllowed).isSuccess should be(true)
    testKingConstraintsGame = testKingConstraintsGame(anotherWhiteKingMoveAllowed).toOption.value
    testKingConstraintsGame.isKingInCheck shouldBe false
  }

  /*
   * FOOL'S MATE
   * */
  "Build a Fool's Check Mate in which the game " should " end in 4 turn," in {
    val firstWhitePawnMove  = "f3"
    val blackPawnMove       = "e6"
    val secondWhitePawnMove = "g4"
    val blackQueenMove      = "Qh4#"
    foolMateGame = foolMateGame(firstWhitePawnMove).toOption.value
    foolMateGame = foolMateGame(blackPawnMove).toOption.value
    foolMateGame = foolMateGame(secondWhitePawnMove).toOption.value
    foolMateGame.isKingInCheck shouldBe false
    foolMateGame = foolMateGame(blackQueenMove).toOption.value
    foolMateGame.isKingInCheck shouldBe true
    foolMateGame.gameStatus should equal(Win(Black))
  }

  /*
   * SCHOLAR'S MATE
   * */
  "Build a Scholar's Check Mate in which the game " should " end in 7 turn," in {
    scholarMateGame = GameCreator.scholarGame
    scholarMateGame.gameStatus should equal(Win(White))
    scholarMateGame.isKingInCheck shouldBe true
  }

  "Build a game where the white player" should "be able to use a QueenSide Castling" in {
    var castlingGame: ChessGame = ChessGame.standard()
    val firstMove               = "Na3"
    val secondMove              = "a6"
    val thirdMove               = "b3"
    val fourthMove              = "b6"
    val fifthMove               = "c3"
    val sixthMove               = "c6"
    val seventhMove             = "Qc2"
    val eighthMove              = "d6"
    val ninthMove               = "Bb2"
    val tenthMove               = "e6"
    val castling                = "0-0-0"
    Seq(firstMove,
        secondMove,
        thirdMove,
        fourthMove,
        fifthMove,
        sixthMove,
        seventhMove,
        eighthMove,
        ninthMove,
        tenthMove,
        castling).foreach(move => castlingGame = castlingGame(move).toOption.value)
    castlingGame.board.pieceAtPosition(Position(3, 1)).value shouldBe Piece(White, King)
    castlingGame.board.pieceAtPosition(Position(4, 1)).value shouldBe Piece(White, Rook)
  }

  "Build a game where the black player" should "be able to use a KingSide Castling" in {
    var castlingGame: ChessGame = ChessGame.standard()
    Seq("a3", "Nh6", "b3", "g6", "c3", "f6", "d3", "Bg7", "e3", "0-0").foreach(move =>
      castlingGame = castlingGame(move).toOption.value)
    castlingGame.board.pieceAtPosition(Position(7, 8)).value shouldBe Piece(Black, King)
    castlingGame.board.pieceAtPosition(Position(6, 8)).value shouldBe Piece(Black, Rook)
  }

  "At the start of a game, a castling" should "not be allowed" in {
    val game: ChessGame = ChessGame.standard()
    game("0-0-0").toOption.isDefined shouldBe false
    game("0-0").toOption.isDefined shouldBe false
  }

  "A correct move history" should "be created during a game" in {
    var game: ChessGame = ChessGame.standard()

    val validMoves = Seq(
      ValidSimpleMove(Position(2, 1), Position(1, 3), Knight, White, None),
      ValidSimpleMove(Position(2, 7), Position(2, 6), Pawn, Black, None),
      ValidSimpleMove(Position(4, 2), Position(4, 3), Pawn, White, None),
      ValidSimpleMove(Position(4, 7), Position(4, 6), Pawn, Black, None),
      ValidSimpleMove(Position(7, 2), Position(7, 3), Pawn, White, None)
    )

    val history = validMoves.map { v =>
      val movesBefore = validMoves.span(_ != v)._1
      val boardAfter =
        movesBefore.foldLeft(Board.defaultBoard())((board, move) => board(move.boardChanges))(v.boardChanges)
      FullMove(v, resultsInCheck = false, resultsInCheckmate = false, boardAfter)
    }

    Seq("Na3", "b6", "d3", "d6", "g3").foreach(move => game = game(move).toOption.value)
    game.moveHistory shouldEqual history
  }

  "A Pawn on Position h4" should "be able to capture on g5" in {
    val game = Seq("h4", "g5", "hxg5").foldLeft(ChessGame.standard())((game, move) => game(move).toOption.value)
    game.board.pieces(Position(7, 5)).color shouldBe White
  }

  "A stalemate" should "result in a draw" in {
    val drawGame = GameCreator.drawGame
    drawGame.gameStatus shouldBe Draw
  }
}
