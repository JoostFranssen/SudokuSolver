package nl.sogyo.sudoku;

import java.time.Duration;
import java.time.Instant;

/**
 * A simple command line interface to solve a sudoku. The input is via a sudoku string as the first command-line argument
 * @author jfranssen
 * @see Sudoku#isValidSudokuString(String)
 */
public class SudokuSolver {
	public static void main(String[] args) {
		Sudoku sudoku = new Sudoku();
		
		if(args.length > 0) {
			if(!Sudoku.isValidSudokuString(args[0])) {
				System.out.println("Provide a string of " + (Sudoku.DIMENSION*Sudoku.DIMENSION) + " digits to represent the sudoku as a command-line argument.\nStart from the top-left row-wise to the bottom-right; use 0 for an empty cell.");
				System.exit(1);
			} else {
				sudoku = new Sudoku(args[0]);
			}
		}
		
		sudoku.printCompactly();
		System.out.println();
		
		Instant startTime = Instant.now();
		Sudoku.solve(sudoku).printCompactly();
		Instant endTime = Instant.now();
		Duration elapsedTime = Duration.between(startTime, endTime);
		System.out.println(String.format("Sudoku solved in %s ms:", elapsedTime.toMillis()));
	}
}
