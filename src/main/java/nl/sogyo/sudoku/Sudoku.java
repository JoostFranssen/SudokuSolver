package nl.sogyo.sudoku;

import java.util.ArrayList;
import java.util.Stack;

/**
 * An object representing a normal sudoku
 * @author jfranssen
 *
 */
public class Sudoku {
	public static final int DIMENSION = 9;
	public static final int BLOCK_DIMENSION = 3;
	
	private Cell[][] cells;
	private ArrayList<CellUnit> cellUnits;
	
	/**
	 * Creates a blank {@code Sudoku}.
	 */
	public Sudoku() {
		this("0".repeat(DIMENSION * DIMENSION));
	}
	/**
	 * Creates a {@code Sudoku} from a {@code String} of digits.
	 * @param sudokuString a {@code String} of 81 digits representing the sudoku. It starts from the top-left row-wise to the bottom-right. Zero represents an empty cell
	 * @throws IllegalArgumentException when {@link #isValidSudokuString(String)} returns false for the given {@code String}
	 */
	public Sudoku(String sudokuString) throws IllegalArgumentException {
		if(!Sudoku.isValidSudokuString(sudokuString)) {
			throw new IllegalArgumentException("Wrong number of entries.");
		}
		
		//initialize all cells from the string
		cells = new Cell[DIMENSION][DIMENSION];
		for(int i = 0; i < DIMENSION * DIMENSION; i++) {
			char entryChar = sudokuString.charAt(i);
			int entryValue = Character.getNumericValue(entryChar);
			cells[i / DIMENSION][i % DIMENSION] = new Cell(entryValue);
		}
		
		initializeCellUnits();
	}
	/**
	 * Creates a new {@code Sudoku} with the same entries and possible entries as the given one. This is a deep copy so that all {@code Cell} objects function independently of the passed {@code Sudoku}.
	 * @param sudoku a {@code Sudoku} object
	 */
	public Sudoku(Sudoku sudoku) {
		cells = new Cell[DIMENSION][DIMENSION];
		for(int r = 0; r < DIMENSION; r++) {
			for(int c = 0; c < DIMENSION; c++) {
				cells[r][c] = new Cell(sudoku.cells[r][c]);
			}
		}
		
		initializeCellUnits();
	}
	
	/**
	 * Checks whether the provided 
	 * @param sudokuString a {@code String} of 81 digits representing the sudoku.
	 * @return whether the passed {@code String} repesents a valid sudoku, which is the case if and only if the string contains 81 digits
	 */
	public static boolean isValidSudokuString(String sudokuString) {
		if(sudokuString.length() != DIMENSION * DIMENSION) {
			return false;
		}
		
		for(int i = 0; i < DIMENSION * DIMENSION; i++) {
			if(!Character.isDigit(sudokuString.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Initializes each {@code CellUnit}. Each {@code CellUnit} contains nine {@code Cell} objects representing a single block that must contains exactly once each digit from 1 through 9. In a standard sudoku these are all rows, columns, and the 3×3 blocks.
	 * <br/>
	 * By overriding this method other types of sudokus can be implemented.
	 */
	protected void initializeCellUnits() {
		cellUnits = new ArrayList<CellUnit>();
		for(int i = 0; i < DIMENSION; i++) {
			//rows
			cellUnits.add(new CellUnit(cells[i]));
			
			//columns
			Cell[] column = new Cell[DIMENSION];
			for(int j = 0; j < DIMENSION; j++) {
				column[j] = cells[j][i];
			}
			cellUnits.add(new CellUnit(column));
		}
		
		//blocks cell units
		//loops over the 3-by-3 blocks from top to bottom and left to right; within each block it loops the same way over the cells
		for(int i = 0; i < DIMENSION; i += BLOCK_DIMENSION) {
			for(int j = 0; j < DIMENSION; j += BLOCK_DIMENSION) {
				Cell[] block = new Cell[DIMENSION];
				for(int r = 0; r < BLOCK_DIMENSION; r++) {
					for(int c = 0; c < BLOCK_DIMENSION; c++) {
						block[BLOCK_DIMENSION * r + c] = cells[i + r][j + c];
					}
				}
				cellUnits.add(new CellUnit(block));
			}
		}
	}
	
	/**
	 * Finds a solution to the provided {@code Sudoku}, if it is consistent. This method does not affect the passed {@code Sudoku}.
	 * @param sudoku the sudoku to be solved
	 * @return a new {@code Sudoku} containing a solved state
	 * @throws IllegalArgumentException if the provided {@Sudoku} is inconsistent
	 */
	public static Sudoku solve(Sudoku sudoku) throws IllegalArgumentException {
		Stack<Sudoku> sudokus = new Stack<Sudoku>();
		Sudoku currentSudoku = new Sudoku(sudoku);
		
		Stack<CellCoordinate> guessedCellCoordinates = new Stack<CellCoordinate>();
		Stack<Integer> guessedValues = new Stack<Integer>();
		
		while(!currentSudoku.isFilled()) {
			boolean successfulIteration = currentSudoku.solveIteration();
			
			if(!successfulIteration) {
				Sudoku sudokuCopy = new Sudoku(currentSudoku);
				CellCoordinate guessedCellCoordinate = sudokuCopy.getCellToGuess();
				
				if(guessedCellCoordinate != null) {
					sudokus.push(currentSudoku);
					currentSudoku = sudokuCopy;
					guessedCellCoordinates.push(guessedCellCoordinate);
					int lastGuessedValue = currentSudoku.guess(currentSudoku.cells[guessedCellCoordinate.row][guessedCellCoordinate.column]);
					guessedValues.push(lastGuessedValue);
				}
			}
			
			if(!currentSudoku.isConsistent()) {
				if(!sudokus.isEmpty()) {
					currentSudoku = sudokus.pop();
					CellCoordinate lastGuessedCellCoordinate = guessedCellCoordinates.pop();
					int lastGuessedValue = guessedValues.pop();
					currentSudoku.cells[lastGuessedCellCoordinate.row][lastGuessedCellCoordinate.column].removePossibleValue(lastGuessedValue);
				} else {
					throw new IllegalArgumentException("Inconsistent sudoku.");
				}
			}
		}
		return currentSudoku;
	}
	
	/**
	 * Runs through ones solve iteration. This first eliminates from each {@code Cell} the possible values that already appear in the same {@code CellUnit}. Then it checks for each {@code CellUnit} whether there is a unique value, and if so, sets it.
	 * @return {@code true} when any changes were made, either in actual values of a {@code Cell} or the possible values. Otherwise returns {@code false}.
	 */
	private boolean solveIteration() {
		boolean changeMade = false;
		
		for(CellUnit unit : cellUnits) {
			boolean changed1 = unit.eliminatePossibleValues();
			boolean changed2 = unit.setUniqueCellValues();
			
			if(changed1 || changed2) {
				changeMade = true;
			}
		}
		
		return changeMade;
	}
	
	/**
	 * Checks whether the {@code Sudoku} is completely filled in.
	 * @return {@code true} when each {@code Cell} has a set value. Otherwise returns {@code false}.
	 */
	public boolean isFilled() {
		for(int r = 0; r < DIMENSION; r++) {
			for(int c = 0; c < DIMENSION; c++) {
				if(cells[r][c].getValue() == Cell.NO_VALUE) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks whether the {@code Sudoku} is consistent or not.
	 * @return {@code false} when the {@code Sudoku} contains conflicting values. If it currently does not contain any conflicting values, {@code true} is returned; it is not guaranteed that the {@code Sudoku} actually has a solution
	 */
	public boolean isConsistent() {
		for(CellUnit cellUnit : cellUnits) {
			if(!cellUnit.isConsistent()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the lowest possible value for the {@code Cell}.
	 * @param guessCell the {@code Cell} for which a value should be guessed
	 * @return the guessed value. If no guess was made 0 is returned
	 */
	private int guess(Cell guessCell) {
		for(int value = Cell.MIN_VALUE; value <= Cell.MAX_VALUE; value++) {
			if(guessCell.isPossibleValue(value)) {
				guessCell.setValue(value);
				return value;
			}
		}
		return 0;
	}
	
	//returns the coordinates of a cell with the smallest number of possible values
	//if no such cell exists, then null is returned
	/**
	 * Finds a {@code Cell} to guess. This returns one of the {@code Cell}s that has the fewest possible values, thus minimizing the chance that the guess is incorrect.
	 * @return a {@code CellCoordinate} object representing the position in the {@code Sudoku}
	 */
	protected CellCoordinate getCellToGuess() {
		CellCoordinate guessCellCoordinate = null;
		int lowestPossibleValues = Cell.MAX_VALUE+1;
		
		for(int r = 0; r < DIMENSION; r++) {
			for(int c = 0; c < DIMENSION; c++) {
				if(cells[r][c].getValue() == Cell.NO_VALUE) {
					if(cells[r][c].possibleValuesSize() < lowestPossibleValues) {
						guessCellCoordinate = new CellCoordinate(r, c);
						lowestPossibleValues = cells[r][c].possibleValuesSize();
					}
				}
			}
		}
		
		return guessCellCoordinate;
	}
	
	/**
	 * A position of a {@code Cell} in a {@code Sudoku} represented by its row and column (starting at 0).
	 * @author jfranssen
	 *
	 */
	private static class CellCoordinate {
		public int row;
		public int column;
		
		public CellCoordinate(int row, int column) {
			this.row = row;
			this.column = column;
		}
		
		public String toString() {
			return String.format("(%d, %d)", row, column);
		}
	}
	
	public String toString() {
		String sudokuString = "";
		for(int r = 0; r < DIMENSION; r++) {
			for(int c = 0; c < DIMENSION; c++) {
				sudokuString += cells[r][c].getValue();
			}
		}
		return sudokuString;
	}
	
	/**
	 * Prints a representation of the {@code Sudoku}.
	 */
	public void print() {
		String sudokuString = "";
		String rowSeparator = "\u2013".repeat(DIMENSION*2 + 1); //n-dash
		String thickRowSeparator = "=".repeat(DIMENSION*2 + 1);
		String columnSeparator = "|";
		String thickColumnSeparator = "\u2016"; //double vertical line
		
		for(int r = 0; r < DIMENSION; r++) {
			sudokuString += (r != 0 && r % BLOCK_DIMENSION == 0 ? thickRowSeparator : rowSeparator) + "\n" + columnSeparator;
			for(int c = 0; c < DIMENSION; c++) {
				int value = cells[r][c].getValue();
				sudokuString += (value == Cell.NO_VALUE ? " " : String.valueOf(value)) + (c != DIMENSION-1 && c % BLOCK_DIMENSION == BLOCK_DIMENSION-1 ? thickColumnSeparator : columnSeparator);
				if(c == DIMENSION-1) {
					sudokuString += "\n";
				}
			}
		}
		sudokuString += rowSeparator;
		System.out.println(sudokuString);
	}
	
	/**
	 * Prints a compact representation of the {@code Sudoku}.
	 */
	public void printCompactly() {
		String sudokuString = "";
		for(int r = 0; r < DIMENSION; r++) {
			for(int c = 0; c < DIMENSION; c++) {
				int value = cells[r][c].getValue();
				sudokuString += (value == Cell.NO_VALUE ? " " : String.valueOf(value));
				if(c != DIMENSION-1 && c % BLOCK_DIMENSION == BLOCK_DIMENSION-1) {
					sudokuString += "|";
				}
			}
			if(r != DIMENSION-1) {
				sudokuString += "\n";
				
				if(r % BLOCK_DIMENSION == BLOCK_DIMENSION-1) {
					sudokuString += "\u2013".repeat(DIMENSION + (DIMENSION / BLOCK_DIMENSION - 1)) + "\n"; //n-dash
				}
			}
		}
		System.out.println(sudokuString);
	}
}
