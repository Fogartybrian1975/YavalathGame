import java.util.Scanner;

public class Main {
	/*
	 * Miroslav Svitok
	 *         
	 */	
	
	public static void main(String args[]){		
		Scanner in = new Scanner(System.in);
		  		
		System.out.println("======================================================================");
		System.out.println("===============================YAVALATH===============================");
		System.out.println("======================================================================");
		
		String coords=null;
		boolean correctMove,end=false;
		
		Board b = new Board();	
		Board b1,b2;
		Game game = new Game();		
		b.printBoard();		
		int searchDisagreeCount=0, evalsKillerOn = 0, evalsKillerOff =0; 
		
		//game
		while(!end){
			System.out.println();
			System.out.print("Place new move (X as a swap move or a normal move (A1,A2,...,E1,...,E9,...,I5) ): ");
			coords = in.next();			
			
			correctMove = false;
						
			try {
				b.move(coords);
				correctMove=true;
			} catch (BadMoveSyntaxException e) {				
				System.out.print(e.getMessage());
			} catch (NoPlayableMoveException e) {				
				System.out.print(e.getMessage());
			} catch (OccupiedCellException e) {				
				System.out.print(e.getMessage());
			}   
			
			if(correctMove){
				end = b.checkForEnd();
				b.printBoard();				
				
				//computer's move				
				if(b.getGameStatus()==Board.GAME_STARTED){
					System.out.println("===========Computer's=========== (thinking...)");
					
					game.setkHeuristic(true);
					b1 = game.nextBestMove(b, 3); //killer heuristic on
					
					//System.out.print("STATIC EVAL: Killer(on): "+ game.getEvaluationCount() +", Killer(off): ");
					evalsKillerOn+=game.getEvaluationCount();
					
					//game.setkHeuristic(false);
					//b2 = game.nextBestMove(b, 3); //killer heuristic off
					//System.out.println(game.getEvaluationCount());
					//evalsKillerOff+=game.getEvaluationCount();
					
					//different moves were chosen
					//if (b1.getLastMove() != b2.getLastMove())
						//searchDisagreeCount++;
					
					b = b1;//always use the no/killer heuristic move
					
					if (b.isSwapApplied() && b.getMovesCount()==2){ //swap was applied
						System.out.println("Swap move!!");
					}					
					b.printBoard();	
					b.checkForEnd();					
				}
			}				
		}
		
		
		System.out.println("========The END===========");
		System.out.println();
		System.out.println("Statistics");
		System.out.println("----------");
		System.out.println("Search disagree: "+searchDisagreeCount);
		System.out.println("Static evaluations (Killer ON): "+evalsKillerOn);
		System.out.println("Static evaluations (Killer OFF): "+evalsKillerOff);
	}
}
