package nl.sogyo.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A unit that consists ordinarily of 9 {@code Cell}s that must have mutually exclusive values
 * @author jfranssen
 *
 */
public class CellUnit {
	private Set<Cell> cells;
	public static int DIMENSION = Sudoku.DIMENSION;
	
	/**
	 * Creates an empty {@code CellUnit} with no {@code Cell}s.
	 */
	public CellUnit() {
		this(new Cell[0]);
	}
	/**
	 * Creates a {@code CellUnit} containing the {@code Cell}s provided. {@code Cell}S that are the same are filtered out.
	 * @param cells an array of {@code Cell}s.
	 */
	public CellUnit(Cell[] cells) {
		this.cells = new HashSet<Cell>(Arrays.asList(cells));
	}
	
	/**
	 * Add a {@code Cell} to the unit.
	 * @param cell the {@code Cell} to be added
	 */
	public void addCell(Cell cell) {
		cells.add(cell);
	}
	
	/**
	 * Eliminates from each {@code Cell} the possible values based on the values that have been set in the other {@code Cell}s.
	 * @return {@code true} is anything was changed
	 */
	public boolean eliminatePossibleValues() {
		boolean changeMade = false;
		
		for(Cell cell : cells) {
			int value = cell.getValue();
			if(value != Cell.NO_VALUE) {
				for(Cell otherCell : cells) {
					if(!cell.equals(otherCell) && otherCell.getValue() == Cell.NO_VALUE) {
						if(otherCell.removePossibleValue(value)) {
							changeMade = true;
						}
					}
				}
			}
		}
		return changeMade;
	}
	
	/**
	 * Sets the value of a {@code Cell} if this is the only {@code Cell} that can take this value within the unit.
	 * @return {@code true} if any changes were made
	 */
	public boolean setUniqueCellValues() {
		boolean valueWasSet = false;
		
		for(int value = Cell.MIN_VALUE; value <= Cell.MAX_VALUE; value++) {
			Cell cell = findUniqueCellWithPossibleValue(value);
			if(cell != null) {
				if(cell.getValue() == Cell.NO_VALUE) {
					cell.setValue(value);
					valueWasSet = true;
				}
			}
		}
		return valueWasSet;
	}
	
	//if there is only one cell in which value could be set, then this cell is returned; otherwise it returns null
	/**
	 * Finds the {@code Cell} that is the only {@code Cell} that can take the provided {@code value}.
	 * @param value a value from 1 through 9
	 * @return the {@code Cell} that is unique in the unit to be able to take {@code value}. Returns {@code null} if no such {@code Cell} exists
	 */
	private Cell findUniqueCellWithPossibleValue(int value) {
		ArrayList<Cell> cellsWithPossibleValue = new ArrayList<Cell>();
		for(Cell cell : cells) {
			if(cell.isPossibleValue(value)) {
				cellsWithPossibleValue.add(cell);
			}
		}
		if(cellsWithPossibleValue.size() == 1) {
			return cellsWithPossibleValue.get(0);
		} else {
			return null;
		}
	}
	
	//a cell unit is inconsistent if more than one cell within its unit have the same value that is not Cell.NO_VALUE
	/**
	 * Checks whether a {@code CellUnit} contains conflicting values
	 * @return {@code false} when no conflicting values are found. Returns {@code true} otherwise. A {@code CellUnit} may still turn out to be inconsistent as more values are set
	 */
	public boolean isConsistent() {
		if(cells.size() > DIMENSION) {
			return false;
		}
		
		boolean[] seenValues = new boolean[Cell.MAX_VALUE+1];
		for(Cell cell : cells) {
			if(cell.getValue() != Cell.NO_VALUE) {
				if(seenValues[cell.getValue()]) {
					return false;
				} else {
					seenValues[cell.getValue()] = true;
				}
			}
		}
		return true;
	}
}
