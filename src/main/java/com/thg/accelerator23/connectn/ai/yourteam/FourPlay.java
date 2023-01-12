package com.thg.accelerator23.connectn.ai.yourteam;

import com.thehutgroup.accelerator.connectn.GameRunner;
import com.thehutgroup.accelerator.connectn.analysis.BoardAnalyser;
import com.thehutgroup.accelerator.connectn.analysis.GameState;
import com.thehutgroup.accelerator.connectn.player.*;

import java.util.ArrayList;
import java.util.Random;


public class FourPlay extends Player {

  static final int wrap = 10;
  static final int width = 10;
  static final int height = 8;
  static final long oddMask;
  static final long evenMask;

  static byte O = Byte.parseByte(Counter.O.getStringRepresentation());
  static byte X = Byte.parseByte(Counter.X.getStringRepresentation());

  GameConfig config = new GameConfig(width, height, 4);
  static BoardAnalyser analyser = new BoardAnalyser(config);

  private static long[] board = new long[3];

  {
    long oddMaskTemp = 0;
    long evenMaskTemp = 0;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < wrap; j++) {
        if (j%2==0) {
          if (j < wrap-1)
            oddMaskTemp |= 1L<<(wrap*i+j);
        } else {
          evenMaskTemp |= 1L<<(wrap*i+j);
        }

        if (j < wrap -1) {

        }
      }
    }
    oddMask = oddMaskTemp;
    evenMask = evenMaskTemp;
  }

  public FourPlay(Counter counter) {
    //TODO: fill in your name here
    super(counter, FourPlay.class.getName());
  }

  public static boolean checkWinO(Board board) {
    GameState state = analyser.calculateGameState(board);
    return state.isWin() && state.getWinner() == Counter.O;
  }

  public static boolean checkWinX(Board board) {
    GameState state = analyser.calculateGameState(board);
    return state.isWin() && state.getWinner() == Counter.X;
  }

  public boolean checkDraw(Board board) {
    GameState state = analyser.calculateGameState(board);
    return state.isDraw();
  }

  public static long addDangerPositions(long board, long emptySpacesMap, int wrap) {
    long dangerSign = board & board>>wrap; //detect two in a row with the wrap
    long dangerMap;
    long threeInARowDanger = dangerSign & dangerSign>>wrap;
    dangerMap = threeInARowDanger<<wrap*3 | threeInARowDanger >> wrap; //add blanks next to three in a row
    dangerMap |= (threeInARowDanger>>2*wrap & board)<<wrap; //detect three pieces with blank in between any pair
    dangerMap |= (threeInARowDanger<<3*wrap & board)>>wrap; //detect three pieces with blank in between any pair
    return dangerMap & emptySpacesMap;
  }

  public static byte[][] getBoard(Board currentBoard) {
    byte[][] output = new byte[width][height];
    for (byte counter = O;
         counter <= X; counter++) {
      for (int i = 0; i < width*(height+1); i++) {
        if (i%(height+1) != height && (board[counter]>>i&1)==1)
          output[i/(height+1)][i%(height+1)] = counter;
      }
    }
    return output;
  }

  private static int evaluate(Board currentBoard) {
    long board[] = new long[3];
    // converting to bit board to simplify minimax
    byte[][] byteBoard = getBoard(currentBoard);
    int i = 0;
    for (int y = 0; y < byteBoard[0].length; y++) {
      for (int x = 0; x < byteBoard.length; x++) {
        if (byteBoard[x][y]!=0) {
          board[byteBoard[x][y]] = 1 << i;
        }
        i++;
      }
    }
    board[0] = ~(board[1] | board[2]);
    long dangerMap[] = new long[3];
    for (int j = O; j <= X; j++) {
      dangerMap[j] = 0;
      dangerMap[j] |= addDangerPositions(board[j], board[0], 1);
      dangerMap[j] |= addDangerPositions(board[j], board[0], wrap);
      dangerMap[j] |= addDangerPositions(board[j], board[0], wrap+1);
      dangerMap[j] |= addDangerPositions(board[j], board[0], wrap-1);
    }
    return (2*Long.bitCount(oddMask & dangerMap[1]) +
            Long.bitCount(evenMask & dangerMap[1])) - (2*Long.bitCount(evenMask & dangerMap[2]) +
            Long.bitCount(oddMask & dangerMap[2]));
  }

  int depth = 12;
  public int miniMax(Board board) {
    byte[][] currentBoard = getBoard(board);
    long startTime = System.currentTimeMillis();
    int[] score = new int[width];
    int topScore = 0;

    if (getCounter() == Counter.O) {
      topScore = -1000000;
    }
    if (getCounter() == Counter.X) {
      topScore = 1000000;
    }
    boolean[] possibleMoves = new boolean[width];
    for (int i = 0; i < width; i++) {
      if (board.hasCounterAtPosition(new Position(i, 7))) {
        if (getCounter()==Counter.O)
          score[i] = min(board, depth-1, topScore-1);
        if (getCounter()==Counter.X)
          score[i] = max(board, depth-1, topScore+1);
        topScore = score[i];
        possibleMoves[i] = true;
      } else {
        possibleMoves[i] = false;
      }
    }
    int bestPossibleScore = 0;
    ArrayList tiedMoves = new ArrayList();
    if (getCounter()==Counter.O) {
      int bestScore = -1000000;
      for (int i = 0; i < width; i++) {
        if (score[i] > bestScore & possibleMoves[i]) {
          tiedMoves.clear();
          tiedMoves.add(i);
          bestScore = score[i];
          bestPossibleScore = score[i];
        }
        if (score[i] == bestScore & possibleMoves[i]) {
          tiedMoves.add(i);
        }
      }
    }
    if (getCounter() == Counter.X) {
      int bestScore = -1000000;
      for (int i = 0; i < width; i++) {
        if (score[i] < bestScore & possibleMoves[i]) {
          tiedMoves.clear();
          tiedMoves.add(i);
          bestScore = score[i];
          bestPossibleScore = score[i];
        }
        if (score[i] == bestScore & possibleMoves[i]) {
          tiedMoves.add(i);
        }
      }
    }

    Random rand = new Random();
    int bestPosition = (int) tiedMoves.get(tiedMoves.size()*rand.nextInt());
    int deathScore = 0;

    if (getCounter() == Counter.O && bestPossibleScore >=100 || getCounter() == Counter.X && bestPossibleScore <= -100) {
      if (getCounter() == Counter.O) {
        deathScore = depth - (bestPossibleScore - 200)/10-2;
      }
      if (getCounter() == Counter.X) {
        deathScore = depth + (bestPossibleScore + 200)/10-2;
      }
    }
    return bestPosition;
  }

  private static int min(Board board, int placeDepth, int minSearchBoundary) {
    byte[][] currentboard = getBoard(board);
    if (checkWinX(board))
  }

  @Override
  public int makeMove(Board board) {
    //TODO: some crazy analysis
    //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it

    return 4;
  }
}
