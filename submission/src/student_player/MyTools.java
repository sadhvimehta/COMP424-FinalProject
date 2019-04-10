package student_player;

import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class MyTools {
	
	static int maxDepth = 2;
	static int myPlayer;
    
    // below is a nested class that helped keeps track of moves and their utility value
    static class ValueMove {
    	private double value;
    	private Move move;
    	
    	public Move getMove() {
			return move;
		}
    	
    	public double getValue() {
			return value;
		}
    	
    	public void setMove(Move move) {
			this.move = move;
		}
    	
    	public void setValue(double value) {
			this.value = value;
		}
    }
    
    // below function is helper to get a row from board
    public static Piece[] getRow(PentagoBoardState boardState, int row, boolean horizontal) {
    	Piece[] result = new Piece[6];
    	if(horizontal) {
    		for(int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
        		result[i] = boardState.getPieceAt(row, i);
        	}
    	}
    	else {
    		for(int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
        		result[i] = boardState.getPieceAt(i, row);
        	}
    	}
    	
    	return result;
    }
    
    // below function is helper to get color of pieces of the bot
    public static Piece getBotColor(PentagoBoardState boardState) {
    	if(myPlayer == PentagoBoardState.WHITE)
    		return PentagoBoardState.Piece.WHITE;
    	else
    		return PentagoBoardState.Piece.BLACK;
    }
    
    // below function picks the next best move
    public static Move pickMove(PentagoBoardState boardState) {
    	myPlayer = boardState.getTurnPlayer();
    	
    	Move middleMove = placeInMiddle(boardState);
    	if(middleMove != null)
    		return middleMove;
    	
    	ValueMove vm = 
    			maxValue(boardState, boardState, maxDepth, Double.MIN_VALUE, Double.MAX_VALUE);
    	return vm.getMove();	
    }
    
    // below funtion places piece in middle of quadrants if legal
    private static Move placeInMiddle(PentagoBoardState boardState) {
    	PentagoCoord mid_quad1 = new PentagoCoord(1,4);
    	PentagoCoord mid_quad2 = new PentagoCoord(4,4);
    	PentagoCoord mid_quad3 = new PentagoCoord(1,1);
    	PentagoCoord mid_quad4 = new PentagoCoord(4,1);
    	
    	if(boardState.isPlaceLegal(mid_quad1))
    		return new PentagoMove(mid_quad1, Quadrant.BL, Quadrant.BR, myPlayer);
    	if(boardState.isPlaceLegal(mid_quad2))
    		return new PentagoMove(mid_quad2, Quadrant.BL, Quadrant.TL, myPlayer);
    	if(boardState.isPlaceLegal(mid_quad3))
    		return new PentagoMove(mid_quad3, Quadrant.BR, Quadrant.TR, myPlayer);
    	if(boardState.isPlaceLegal(mid_quad4))
    		return new PentagoMove(mid_quad4, Quadrant.TL, Quadrant.TR, myPlayer);
    	else return null;
    }
    
    /** below function calculates the value of each possible move for the max-player
    /	and returns best one **/
    private static ValueMove maxValue(PentagoBoardState boardState, PentagoBoardState originalState, int depth, double alpha, double beta) {
    	double bestMoveValue = Double.MIN_VALUE;
    	List<PentagoMove> moves = boardState.getAllLegalMoves();
    	Collections.shuffle(moves);
    	ValueMove vm = new ValueMove();
    	
    	if(depth == 0 || moves.isEmpty() || boardState.gameOver()) {// greater than max depth, evaluate the heuristic
    		vm.setValue(evaluate(boardState, originalState));
    		return vm;
    	}
    	
    	for(PentagoMove move : moves) {
    		// below, get the clone state to evaluate a potential move
    		PentagoBoardState cloneState = (PentagoBoardState) boardState.clone();
    		cloneState.processMove(move);
    		
    		bestMoveValue = minValue(cloneState, originalState, depth-1, alpha, beta).value;
    		if(bestMoveValue > alpha) {
    			vm.setValue(bestMoveValue);
    			vm.setMove(move);
    			alpha = bestMoveValue;
    		}
    		if (alpha >= beta)
    			break;
    	}
    	
    	return vm;
    }
    
    /** below function calculates the value of each possible move for the min-player
    /	and returns best one **/
    private static ValueMove minValue(PentagoBoardState boardState, PentagoBoardState originalState, int depth, double alpha, double beta) {
    	double bestMoveValue = Double.MAX_VALUE;
    	List<PentagoMove> moves = boardState.getAllLegalMoves();
    	Collections.shuffle(moves);
    	ValueMove vm = new ValueMove();
    	
    	if(depth == 0 || moves.isEmpty() || boardState.gameOver()){ // greater than max depth, evaluate the heuristic
    		vm.setValue(evaluate(boardState, originalState));
    		return vm;
    	}
    	
    	for(PentagoMove move : moves) {
    		// below, get the clone state to evaluate a potential move
    		PentagoBoardState cloneState = (PentagoBoardState) boardState.clone();
    		cloneState.processMove(move);
    		
    		bestMoveValue = maxValue(cloneState, originalState, depth-1, alpha, beta).value;
    		if(bestMoveValue < beta) {
    			vm.setValue(bestMoveValue);
    			vm.setMove(move);
    			beta = bestMoveValue;
    		}
    		if (alpha >= beta)
    			break;
    	}
    	
    	return vm;
    }
    
    // below function is the heuristic used to evaluate the moves
    // it uses the difference between player cost and opponent cost as the comparison
    private static double evaluate(PentagoBoardState boardState, PentagoBoardState originalState) {
    	if(boardState.gameOver()) {
    		int winner = boardState.getWinner();
    		if(winner == Board.DRAW)
        		return 0.0;
        	if(winner != myPlayer)
        		return Double.MIN_VALUE;
        	else
        		return Double.MAX_VALUE;
    	}
    	
    	
    	//double cost = calcMarbleInCenter(boardState, originalState);
    	Piece playerColor = getBotColor(boardState);
		Piece opponentColor;
		if(playerColor == PentagoBoardState.Piece.BLACK)
			opponentColor = PentagoBoardState.Piece.WHITE;
		else
			opponentColor = PentagoBoardState.Piece.BLACK;
		
    	double totalCost = 0;
    	for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
    		double playerCost = 0;
    		double opponentCost = 0;
			
    		playerCost = playerCost + calcMarblesInARow(getRow(boardState, i, true), playerColor) + calcMarblesInARow(getRow(boardState, i, false), playerColor);
    		opponentCost = opponentCost + calcMarblesInARow(getRow(boardState, i, true), opponentColor) + calcMarblesInARow(getRow(boardState, i, false), opponentColor);
    		
    		totalCost = totalCost + playerCost - opponentCost;
    	}
    	
    	// below calculate cost at diagonals
    	Piece[] diag1 = {boardState.getPieceAt(4, 0), boardState.getPieceAt(3, 1),
    			boardState.getPieceAt(2, 2), boardState.getPieceAt(1, 3), 
    			boardState.getPieceAt(0, 4)};
    	Piece[] diag2 = {boardState.getPieceAt(5, 1), boardState.getPieceAt(4, 2),
    			boardState.getPieceAt(3, 3), boardState.getPieceAt(2, 4), 
    			boardState.getPieceAt(1, 5)};
    	Piece[] diag3 = {boardState.getPieceAt(5, 0), boardState.getPieceAt(4, 1),
    			boardState.getPieceAt(3, 2), boardState.getPieceAt(2, 3), 
    			boardState.getPieceAt(1, 4)};
    	Piece[] diag4 = {boardState.getPieceAt(4, 1), boardState.getPieceAt(3, 2),
    			boardState.getPieceAt(2, 3), boardState.getPieceAt(1, 4), 
    			boardState.getPieceAt(0, 5)};
    	Piece[] diag5 = {boardState.getPieceAt(4, 5), boardState.getPieceAt(3, 4),
    			boardState.getPieceAt(2, 3), boardState.getPieceAt(1, 2), 
    			boardState.getPieceAt(0, 1)};
    	Piece[] diag6 = {boardState.getPieceAt(5, 4), boardState.getPieceAt(4, 3),
    			boardState.getPieceAt(3, 2), boardState.getPieceAt(2, 1), 
    			boardState.getPieceAt(1, 0)};
    	Piece[] diag7 = {boardState.getPieceAt(5, 5), boardState.getPieceAt(4, 4),
    			boardState.getPieceAt(3, 3), boardState.getPieceAt(2, 2), 
    			boardState.getPieceAt(1, 1)};
    	Piece[] diag8 = {boardState.getPieceAt(4, 4), boardState.getPieceAt(3, 3),
    			boardState.getPieceAt(2, 2), boardState.getPieceAt(1, 1), 
    			boardState.getPieceAt(0, 0)};
    	
    	int playerDiagCost = calcMarblesInARow(diag1, playerColor) + 
    			calcMarblesInARow(diag2, playerColor) +
    			calcMarblesInARow(diag3, playerColor) +
    			calcMarblesInARow(diag4, playerColor) +
    			calcMarblesInARow(diag5, playerColor) +
    			calcMarblesInARow(diag6, playerColor) +
    			calcMarblesInARow(diag7, playerColor) + 
    			calcMarblesInARow(diag8, playerColor);
    	
    	int opponentDiagCost = calcMarblesInARow(diag1, opponentColor) + 
    			calcMarblesInARow(diag2, opponentColor) +
    			calcMarblesInARow(diag3, opponentColor) +
    			calcMarblesInARow(diag4, opponentColor) +
    			calcMarblesInARow(diag5, opponentColor) +
    			calcMarblesInARow(diag6, opponentColor) +
    			calcMarblesInARow(diag7, opponentColor) + 
    			calcMarblesInARow(diag8, opponentColor);
    	
    	totalCost = totalCost + playerDiagCost - opponentDiagCost;
    	
    	return totalCost;
    }
    
    // below function helps calculate part of heuristic
    // it gives +5 if any white marble has been added to center of board
    // it gives -5 if any black marble has been added to center of board
    private static double calcMarbleInCenter(PentagoBoardState boardState, PentagoBoardState originalState) {
    	double cost = 0;
    	int xPos = 2;
    	int yPos = 2;
    	if(boardState.getPieceAt(xPos, yPos) == PentagoBoardState.Piece.WHITE
    			&& originalState.getPieceAt(xPos, yPos) != PentagoBoardState.Piece.WHITE)
    		cost = cost + 5.0;
    	if(boardState.getPieceAt(xPos, yPos) == PentagoBoardState.Piece.BLACK
    			&& originalState.getPieceAt(xPos, yPos) != PentagoBoardState.Piece.BLACK)
    		cost = cost - 5.0;
    	if(boardState.getPieceAt(xPos, yPos+1) == PentagoBoardState.Piece.WHITE
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.WHITE)
    		cost = cost + 5.0;
    	if(boardState.getPieceAt(xPos, yPos+1) == PentagoBoardState.Piece.BLACK
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.BLACK)
    		cost = cost - 5.0;
    	if(boardState.getPieceAt(xPos+1, yPos+1) == PentagoBoardState.Piece.WHITE
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.WHITE)
    		cost = cost + 5.0;
    	if(boardState.getPieceAt(xPos+1, yPos+1) == PentagoBoardState.Piece.BLACK
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.BLACK)
    		cost = cost - 5.0;
    	if(boardState.getPieceAt(xPos+1, yPos) == PentagoBoardState.Piece.WHITE
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.WHITE)
    		cost = cost + 5.0;
    	if(boardState.getPieceAt(xPos+1, yPos) == PentagoBoardState.Piece.BLACK
    			&& originalState.getPieceAt(xPos, yPos+1) != PentagoBoardState.Piece.BLACK)
    		cost = cost - 5.0;
    	return cost;
    }
    
    // below function also calculates part of heuristic
    // it checks to see if any 4/3-in a row could POTENTIALLY GROW into a 5 in a row
    // ex: given you are white, W W W B E E will give you no points but W W E W E B will
    private static int calcMarblesInARow(Piece[] a, Piece p) {
    	// 0 means empty
    	// 1 means passed in color of piece-p
    	// 3 means anything

		// 4 in a row
		int[] p41 = { 1, 1, 1, 1, 0, 3 };
		int[] p42 = { 1, 1, 1, 0, 1, 3 };	
		int[] p43 = { 1, 1, 0, 1, 1, 3 };
		int[] p44 = { 1, 0, 1, 1, 1, 3 };	
		int[] p45 = { 0, 1, 1, 1, 1, 3 };
		int[] p46 = { 3, 1, 1, 1, 1, 0 };
		int[] p47 = { 3, 1, 1, 1, 0, 1 };
		int[] p48 = { 3, 1, 1, 0, 1, 1 };
		int[] p49 = { 3, 1, 0, 1, 1, 1 };
		int[] p410 = { 3, 0, 1, 1, 1, 1 };

		// 3 in a row
		int[] p31 = { 1, 1, 1, 0, 0, 3 };
		int[] p32 = { 1, 1, 0, 1, 0, 3 };
		int[] p33 = { 1, 1, 0, 0, 1, 3 };
		int[] p34 = { 1, 0, 1, 0, 1, 3 };
		int[] p35 = { 1, 0, 0, 1, 1, 3 };
		int[] p36 = { 0, 1, 0, 1, 1, 3 };
		int[] p37 = { 0, 0, 1, 1, 1, 3 };
		int[] p38 = { 3, 1, 1, 1, 0, 0 };
		int[] p39 = { 3, 1, 1, 0, 1, 0 };
		int[] p310 = { 3, 1, 1, 0, 0, 1 };
		int[] p311 = { 3, 1, 0, 1, 0, 1 };
		int[] p312 = { 3, 1, 0, 0, 1, 1 };
		int[] p313 = { 3, 0, 1, 0, 1, 1 };
		int[] p314 = { 3, 0, 0, 1, 1, 1 };
		int[] p315 = { 3, 0, 1, 1, 0, 1 };
		int[] p316 = { 0, 1, 1, 0, 1, 3 };
		int[] p317 = { 3, 1, 0, 1, 1, 0 };
		int[] p318 = { 1, 0, 1, 1, 0, 3 };
		int[] p319 = { 3, 0, 1, 1, 1, 0 };
		int[] p320 = { 0, 1, 1, 1, 0, 3 };
		
		/**int[] p21 = { 1, 1, 0, 0, 0, 3 };
		int[] p22 = { 1, 0, 1, 0, 0, 3 };
		int[] p23 = { 1, 0, 0, 1, 0, 3 };
		int[] p24 = { 1, 0, 0, 0, 1, 3 };
		int[] p25 = { 0, 1, 0, 0, 1, 3 };
		int[] p26 = { 0, 0, 1, 0, 1, 3 };
		int[] p27 = { 0, 0, 0, 1, 1, 3 };
		int[] p28 = { 0, 0, 1, 1, 0, 3 };
		int[] p29 = { 0, 1, 1, 0, 0, 3 };*/
		

		if (specialEquals(a, p41, p) || specialEquals(a, p42, p) || specialEquals(a, p43, p) || specialEquals(a, p44, p)
				|| specialEquals(a, p45, p) || specialEquals(a, p46, p) || specialEquals(a, p47, p) || specialEquals(a, p48, p)
				|| specialEquals(a, p49, p) || specialEquals(a, p410, p))
			return 8;
		else if (specialEquals(a, p31, p) || specialEquals(a, p32, p) || specialEquals(a, p33, p) || specialEquals(a, p34, p)
				|| specialEquals(a, p35, p) || specialEquals(a, p36, p) || 
				specialEquals(a, p37, p) || specialEquals(a, p38, p) ||  specialEquals(a, p39, p) || 
				specialEquals(a, p310, p) ||  specialEquals(a, p311, p) || specialEquals(a, p312, p) ||
				 specialEquals(a, p313, p) || specialEquals(a, p314, p) || specialEquals(a, p315, p) ||
				 specialEquals(a, p316, p) || specialEquals(a, p317, p) ||  specialEquals(a, p318, p) 
				 || specialEquals(a, p319, p) || specialEquals(a, p320, p))
			return 5;
		/**else if (specialEquals(a, p21, p) || specialEquals(a, p22, p) || specialEquals(a, p23, p) || specialEquals(a, p24, p)
				|| specialEquals(a, p25, p) || specialEquals(a, p26, p) || specialEquals(a, p27, p) || specialEquals(a, p28, p)
				|| specialEquals(a, p29, p))
			return 2;*/
		else
			return 0;
	}
    
    // below function is helper to check if submitted row equals required configurations
    public static boolean specialEquals(Piece[] a, int[] b, Piece p) {
		for (int i = 0; i < a.length; i++) {
			if (b[i] == 3)
				continue;
			
			int pieceValue;
			if(a[i] == p)
				pieceValue = 1;
			else if(a[i] == PentagoBoardState.Piece.EMPTY)
				pieceValue = 0;
			else
				pieceValue = 2;
					
			if (pieceValue != b[i])
				return false;
		}
		return true;
	}
}