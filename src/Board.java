/*
 * Miroslav Svitok
 *         
 */	

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board {
	private byte[] board = new byte[121];
	private int movesCount = 0; 
	private boolean swapApplied = false; //whether swap rule was applied or not
	private boolean playerAMove = true; //if true, player 1 is on the move, otherwise player 2
	private byte gameStatus = GAME_STARTED;	
	private byte lastMove = -1; //-2 is swap move, -1 no move, other values are indexes 
	

	private int evaluation = 0; // 
	
	//in case we need compute few moves ahead we save its children
	private ArrayList<Board> children = new ArrayList<Board>();
	
	
	/**
	 * creating a board from existing
	 * @param board
	 */
	public Board(Board orig) {
		board = Arrays.copyOf(orig.board,121);
		movesCount = orig.movesCount;
		swapApplied = orig.swapApplied;
		playerAMove = orig.playerAMove;
		gameStatus = orig.gameStatus;		
		//checkForEnd();
	}
	
	//basic creates the start board
	public Board(){
		initBoard();		
		//checkForEnd();
	}
		
	public byte[] getBoard() {
		return board;
	}

	public Board initBoard(){
		board = Arrays.copyOf(initPosition,121);		
		return this;		
	}	
	
	public Board swapBoard(int index){
		if (board[index] == 3)
			board[index] = 2;
		else if (board[index] == 2)
			board[index] = 3;
		
		movesCount++;
		swapApplied=true;
		playerAMove = !playerAMove;		
		lastMove = -2;
		return this;
	}
	

	public Board initChildren(byte killerMove){
		children.clear();
		//children = new ArrayList<Board>(); 
		if(gameStatus!=GAME_STARTED) return this; //end, no more successors
		
		
		
		for (int i = 16; i < 105; i++) { //where game-board starts and stops
			if(board[i]==1){ //is empty
				
				Board newBoard = new Board(this);
				newBoard.move(i).checkForEnd();
				
				if (killerMove == i){ //add killer move first	
					children.add(0, newBoard);					
				} else {
					children.add(newBoard);
				}	
			}			
		}
		
		
		return this;
	}
	
	public String gameStatusToString(){
		String status = null;
		switch(gameStatus){
			case GAME_STARTED: status="Game is in progress"; break;
			case GAME_PA_WON: status="Player A has won"; break;				
			case GAME_PB_WON: status="Player B has won"; break;				
			case GAME_DRAW: status="No one has won"; break;				
		}
		return status;
	}	
	
	
	/**
	 * move with index parameter, just for private, unsafe for public use
	 * @param index  - number of cell in board array	 
	 * @return
	 */
	private Board move(int index){
		board[index] =  isPlayerAMove() ? (byte) 2 : (byte) 3;
		playerAMove = !playerAMove; //switch players
		movesCount++;
		lastMove= (byte) index;
		return this;
	}
	
	/**
	 * move
	 * @param moveCoords coordinates as String
	 * @return
	 * @throws BadMoveSyntaxException
	 * @throws NoPlayableMoveException
	 * @throws OccupiedCellException
	 */
	public Board move(String moveCoords) throws BadMoveSyntaxException, NoPlayableMoveException, OccupiedCellException{
		Matcher matcher = allowedInput.matcher(moveCoords);
		//check syntax
		if (!matcher.find()){
			throw new BadMoveSyntaxException("'" + moveCoords + "' move has a bad syntax.");
		}		
		moveCoords = moveCoords.toUpperCase();		
		
		//special swap move
		if (moveCoords=="X"){
			return null;
		}
		
		int row = (int) moveCoords.charAt(0) - 64; //ASCII A is 65, if we subtract 64 we get 1		
		int col = Character.getNumericValue(moveCoords.charAt(1));		
		if (row<5)	col += 5 - row; //get coordinates for column in case of A-D row
		
		//check validity of a move
		if(board[11*row+col] == 0){
			throw new NoPlayableMoveException("'" + moveCoords + "' is not a playable move.");
		}
		
		//check cell occupation
		if(board[11*row+col] != 1){
			throw new OccupiedCellException("'" + moveCoords + "' is a occupied cell.");
		}
				
		//now move should be valid, make it
		//PlayerA has number 2, PlayerB has number 3
		return this.move(11*row+col);
	}
	
	/**
	 * adjust the entry number to automaton according to a which player is on the move
	 * @param a
	 * @return
	 */
	private int automatonInputPB(int a){
		//if(!isPlayerAMove()){
		if (a==2) return 3;
		if (a==3) return 2;
		//}
		return a;
	}	
	
	/**
	 * evaluation function - from the POV of a player who is about to making a move 
	 * @return
	 */
	public int evaluate(){	
		//checkForTheEnd should be run first in order to find out whether hasnt ended before 
		
		
		if(gameStatus == GAME_PA_WON){
			evaluation = isPlayerAMove() ? 1000 : -1000 ;
			return evaluation;
		} else if(gameStatus == GAME_PB_WON){
			evaluation= isPlayerAMove() ? -1000 : 1000 ;
			return evaluation;
		} else if (gameStatus==GAME_DRAW){
			evaluation=0;
			return evaluation;
		}
		
		//if game is in progress, we evaluate
		int evalA=0,evalB=0;
		
		int multi=1; //offset to move in array
		int state; //automaton state, 0 is initial state
		int n; //input n
		
		//automaton1, automaton2, automaton3
		for (int d=0; d<3; d++) { //3 directions	
			if (d==1) multi=10; //left down orientation
			if (d==2) multi=11; //right down orientation
			
			for(int j=0; j<9;j++){ //9 starting cells
				//PLAYER A
				//automaton1
				state=0;
				for(int i = 0; i<9; i++ ){
					n = (board[directionStart[d][j] + i*multi]);
					
					evalA += automaton1[state][n][1];
					state = automaton1[state][n][0];
					if (state==99) break; //terminal status
				}
				
				//automaton2
				state=0;
				for(int i = 0; i<9; i++ ){
					n = (board[directionStart[d][j] + i*multi]);
					
					evalA += automaton2[state][n][1];
					state = automaton2[state][n][0];
					if (state==99) break; //terminal status
				}
				
				//automaton3
				state=0;				
				for(int i = 0; i<9; i++ ){
					n = (board[directionStart[d][j] + i*multi]);
					
					evalA += automaton3[state][n][1];
					state = automaton3[state][n][0];
					if (state==99) break; //terminal status
				}
				
				//PLAYER B
				//automaton1
				state=0;
				for(int i = 0; i<9; i++ ){
					n = automatonInputPB((board[directionStart[d][j] + i*multi]));
					
					evalB += automaton1[state][n][1];
					state = automaton1[state][n][0];
					if (state==99) break; //terminal status
				}
				
				//automaton2
				state=0;
				for(int i = 0; i<9; i++ ){
					n = automatonInputPB((board[directionStart[d][j] + i*multi]));
					
					evalB += automaton2[state][n][1];
					state = automaton2[state][n][0];
					if (state==99) break; //terminal status
				}
				
				//automaton3
				state=0;				
				for(int i = 0; i<9; i++ ){
					n = automatonInputPB((board[directionStart[d][j] + i*multi]));
					
					evalB += automaton3[state][n][1];
					state = automaton3[state][n][0];
					if (state==99) break; //terminal status
				}
	
				
			}
		}
		evaluation = isPlayerAMove() ? evalA-evalB : evalB-evalA;
		return evaluation;
	}
	
	//check all 6 directions at once
	public boolean checkForEnd(){
		
		int twos=0, threes = 0;		
		int multi=1;
		boolean PA3 = false,PB3 = false; //if player A|B has 3 stones in row, we will chheck the other one
		
		for (int d=0; d<3; d++) { //3 directions	
			if (d==1) multi=10; //left down orientation
			if (d==2) multi=11; //right down orientation
			
			for(int j=0; j<9;j++){ //9 starting cells
				twos = threes = 0;				
				PA3 = PB3 = false;
								
				for(int i = 0; i<9; i++ ){
					if (board[directionStart[d][j] + i*multi]==0){
						
						if(PA3){
							setGameStatus(GAME_PB_WON);
							return true;
						}
						if(PB3){
							setGameStatus(GAME_PA_WON);
							return true;
						}
						break;
						
					} else if (board[directionStart[d][j] + i*multi]==1){
						twos=threes=0;
						
						if(PA3){
							setGameStatus(GAME_PB_WON);
							return true;
						}
						if(PB3){
							setGameStatus(GAME_PA_WON);
							return true;
						}
						
					} else if (board[directionStart[d][j] + i*multi]==2){ //a
						
						if(PB3){
							setGameStatus(GAME_PA_WON);
							return true;
						}
						
						threes=0;
						twos++;
						if (twos==3) PA3=true;
					} else if (board[directionStart[d][j] + i*multi]==3){ //b
						
						if(PA3){
							setGameStatus(GAME_PB_WON);
							return true;
						}
						
						twos=0;
						threes++;
						if (threes==3) PB3=true;						
					}
					if (threes==4) {
						setGameStatus(GAME_PB_WON);
						return true;
					} else if(twos==4){
						setGameStatus(GAME_PA_WON);
						return true;					
					}  
					 
				}
			}
		} 		
		
		//check for the draw
		if (movesCount==61){
			setGameStatus(GAME_DRAW);
			return true;
		}
		
		return false;
	}
	
	public void simplePrintBoard(){
		//System.out.println();
		System.out.print("[");
		for (int i = 16; i <105; i++) {
			System.out.print(board[i]+",");
		}
		System.out.println("]");
	}
	
	public void printBoard(){
		printBoard(true);
	}
	
	/**
	 * print the board on the command line
	 * @param printStats
	 */
	public void printBoard(boolean printStats){
		String indent = "";
		int index;
		
		for (int row = 0; row < 11; row++) {
			System.out.print(indent);
			System.out.print(" ");
			for (int col=0; col< 11; col++){
				index = row*11 + col;
				
				if(board[index] == 2)
					System.out.print("X");
				else if(board[index] == 3)
					System.out.print("O");
				else 
					System.out.print(" ");
				System.out.print(" ");
				
				if(printingBoard[index] >= 4 &&  printingBoard[index] <= 7)
					System.out.print("|");
				else 
					System.out.print(" ");
				
				System.out.print(" ");				
			}			
			System.out.println();
			System.out.print(indent);
			
			for (int col=0; col< 11; col++){
				index = row*11 + col;
				
				if(printingBoard[index] == 2 
						|| printingBoard[index] == 3 
						|| printingBoard[index] == 6 
						|| printingBoard[index] == 7)
					System.out.print("\\");
				else 
					System.out.print(" ");
				System.out.print(" ");
				
				
				
				if(printingBoard[index] == 1 
						|| printingBoard[index] == 3 
						|| printingBoard[index] == 5 
						|| printingBoard[index] == 7)
					System.out.print("/");
				else 
					System.out.print(" ");
				
				System.out.print(" ");				
			}
			System.out.println();
			indent += "  ";			
		}
		if(printStats){
			System.out.println("Next move: " + ( isPlayerAMove() ? " PLAYER A" : " PLAYER B") + ", game state: " + gameStatusToString());			
		}
			
		
		
	}
	
	
	
	
	

	/**********************************
	 *      DO NOT EDIT - START (functions and variables for evaluation and initialization)
	 **********************************/
	public static final byte GAME_STARTED = 1;
	public static final byte GAME_PA_WON = 2;
	public static final byte GAME_PB_WON = 3;
	public static final byte GAME_DRAW = 4;
	
	public static byte[] printingBoard = 
		{0,0,0,0,0,1,3,3,3,3,2,
		0,0,0,0,5,7,7,7,7,7,2,
		0,0,0,5,7,7,7,7,7,7,2,
		0,0,5,7,7,7,7,7,7,7,2,
		0,5,7,7,7,7,7,7,7,7,2,
		4,7,7,7,7,7,7,7,7,7,0,
		4,7,7,7,7,7,7,7,7,0,0,
		4,7,7,7,7,7,7,7,0,0,0,
		4,7,7,7,7,7,7,0,0,0,0,
		4,7,7,7,7,7,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0}; 
	
	public static byte[] initPosition = 
		{0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,1,1,1,1,1,0,
		0,0,0,0,1,1,1,1,1,1,0,
		0,0,0,1,1,1,1,1,1,1,0,
		0,0,1,1,1,1,1,1,1,1,0,
		0,1,1,1,1,1,1,1,1,1,0,
		0,1,1,1,1,1,1,1,1,0,0,
		0,1,1,1,1,1,1,1,0,0,0,
		0,1,1,1,1,1,1,0,0,0,0,
		0,1,1,1,1,1,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0}; 
	
	
	//help variables to help checking for the end of the game, evaluation function calculation
	//cell number used to start:
	//first(horizontal) +1, second(leftdown) +10, third(rightdown) +11 
	public static byte[][] directionStart = {{16,26,36,46,56,67,78,89,100}, {16,17,18,19,20,31,42,53,64},{16,17,18,19,20,26,36,46,56}};
	
	//first(horizontal) -1, second(leftdown) -10, third(rightdown) -11
	//public static byte[][] directionReverseStart = {{20,31,42,53,64,74,84,94,104}, {56,67,78,89,100,101,102,103,104},{64,74,84,94,100,101,102,103,104}};

	
	//evaluation function's finite automatons (to make it quicker) to evaluate game state
	//first 2 dimensions are automaton (row is automaton state, col is a move to a state), third[0] is value of eval.function and 
	//third[1] is gain to eval.function when executing the move (when executing some moves we can get a gain)
	//gain is usually 0, when a pattern is found the gain is positive/negative
	//move to a 99 state is terminal move
	//e-empty, p-piece
	
	//EPEPE (-1), EPPEP (+1), EPEEP(+1) patterns, normal direction
	private static byte[][][] automaton1 = 
		{{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{1,0},{4,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{6,0},{5,0},{2,0}},
		{{99,0},{7,0},{3,0},{2,0}},
		{{99,0},{9,0},{8,0},{2,0}},
		{{99,0},{1,0},{4,1},{2,0}},  //pos
		{{99,0},{6,-1},{3,0},{2,0}}, //neg
		{{99,0},{1,0},{4,1},{2,0}}};  //pos
	
	
	//PEPPE (+1), PEEPE(+1) patterns, normal direction
	private static byte[][][] automaton2 = 
		{{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{4,0},{3,0},{2,0}},
		{{99,0},{6,0},{5,0},{2,0}},
		{{99,0},{4,0},{7,0},{2,0}},
		{{99,0},{1,0},{8,0},{2,0}},
		{{99,0},{4,1},{3,0},{2,0}},  //pos
		{{99,0},{4,1},{3,0},{2,0}}};  //pos
	
	//_EPPE_ (-1) pattern, normal direction
	private static byte[][][] automaton3 = 
		{{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{1,0},{4,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{5,0},{3,0},{2,0}},
		{{99,0},{5,0},{6,0},{2,0}},
		{{99,0},{1,0},{3,0},{2,0}},
		{{99,0},{7,0},{3,0},{2,0}},		
		{{99,-1},{1,-1},{3,0},{2,-1}}};  //neg

	private static Pattern allowedInput = Pattern.compile("^[a-i][1-9]$|^x$", Pattern.CASE_INSENSITIVE);
	
	/***************************
	 *      DO NOT EDIT - END
	 ***************************/
	
	public ArrayList<Board> getChildren() {
		return children;
	}

	public boolean noChildren(){
		return children.isEmpty();
	}
	
	public void setChildren(ArrayList<Board> children) {
		this.children = children;
	}
	
	
	public byte getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(byte gameStatus) {
		this.gameStatus = gameStatus;
	}

	public boolean isSwapApplied() {
		return swapApplied;
	}

	public void setSwapApplied(boolean swapApplied) {
		this.swapApplied = swapApplied;
	}

	public boolean isPlayerAMove() {
		return playerAMove;
	}

	public void setPlayerAMove(boolean playerAMove) {
		this.playerAMove = playerAMove;
	}

	public int getMovesCount() {
		return movesCount;
	}
	
	public byte getLastMove() {
		return lastMove;
	}

	public void setLastMove(byte lastMove) {
		this.lastMove = lastMove;
	}

	
	
}
