/*
 * Miroslav Svitok
 *         
 */	

public class Game {

	private int evaluationCount = 0;	
	private boolean debug = false;			
	private boolean kHeuristic = false;
	
	//these cell should be swapped if available
	private static byte[] criticalCells ={49,50,59,60,61,70,71};
	
	
	public boolean iskHeuristic() {
		return kHeuristic;
	}

	public void setkHeuristic(boolean kHeuristic) {
		this.kHeuristic = kHeuristic;
	}

	
	
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getEvaluationCount() {
		return evaluationCount;
	}

	public void setEvaluationCount(int evaluationCount) {
		this.evaluationCount = evaluationCount;
	}

	public static int safeMinus(int i){
		if (i==Integer.MIN_VALUE) return Integer.MAX_VALUE;
		if (i==Integer.MAX_VALUE) return Integer.MIN_VALUE;
		return -i;
	}
	
	public Board root;
	
	
	
	/**
	 * finds the next best move using negamax search
	 * @param current
	 * @param depth
	 * @return
	 */
	public Board nextBestMove(Board current, int depth){
		evaluationCount=0;
		
		//swap move?
		if(current.getMovesCount()==1){
			 for(byte cIndex : criticalCells){
				 if(current.getBoard()[cIndex]==2){
					 Board newBoard = new Board(current);
					 newBoard.swapBoard(cIndex);					 
					 return newBoard;
				 }
			 }
			
		}
		
		
		current.initChildren(kHeuristic ? killerMoves[current.getMovesCount()+1] : 0);
		Board best = null;
		int bestScore = Integer.MIN_VALUE;
		int temp;		
		
		for(Board b: current.getChildren()){
			temp = -negamax(b, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
			if (temp > bestScore){
				bestScore=temp;
				best= b;				
			}
		}
		
		if(debug){
			System.out.println();
			System.out.println();System.out.println();System.out.println();
			System.out.println("-------------------------------------------------------");
			System.out.println("BEST SCORE: " + bestScore);
			System.out.println("BEST BOARD: ");
			best.printBoard(false);
		}		
		return best;
	}
	
	
	
	//maximum height of a tree is 61, keep statistics for each ply
	private byte[] killerMoves = new byte[61]; 
	private int[][] killerStatistic= new int[61][121];
	

	/**
	 * negamax
	 * @param board
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @return
	 */
	private int negamax(Board board, int depth, int alpha, int beta){
		if(debug){
			board.printBoard();
			System.out.print("DEPTH: " + depth + ", EVAL: " + board.evaluate());
			System.out.println();
			System.out.println("--------------------------------------------------------");			
		}
		
		
		//getMovesCount()  keeps the record about deepness of the tree (which ply)
		if (depth==0 || board.initChildren(kHeuristic ? killerMoves[board.getMovesCount()+1] : 0).noChildren()) {
			evaluationCount++;			
			return board.evaluate();
		} else {
			int temp;
			
			for(Board child : board.getChildren()){							    
				temp = - negamax(child, depth-1, safeMinus(beta), safeMinus(alpha));				
				//A-B cutoff
				if(temp>= beta) {
					
					//killer heuristic[ply][move] - if turned on
					if(kHeuristic){
						killerStatistic[child.getMovesCount()][child.getLastMove()]++;
						//change the killer move
						if (killerMoves[child.getMovesCount()]==0 || killerStatistic[child.getMovesCount()][killerMoves[child.getMovesCount()]] < killerStatistic[child.getMovesCount()][child.getLastMove()]){
							killerMoves[child.getMovesCount()]=child.getLastMove();
						}
					}
					
					
					return temp;					
				}
								
				//maximalize				
				if(temp > alpha) {					
					alpha = temp;
				}				
			}			
			return alpha;
		}
		
	}
}
