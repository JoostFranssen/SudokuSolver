package nl.sogyo.sudoku;

import java.util.HashSet;
import java.util.Set;

/**
 * A single cell of a sudoku
 * @author jfranssen
 *
 */
public class Cell {
	public static final int MIN_VALUE = 1;
	public static final int MAX_VALUE = Sudoku.DIMENSION;
	public static final int NO_VALUE = 0;
	
	private int value;
	private Set<Integer> possibleValues;
	
	/**
	 * Creates an empty {@code Cell} with possible values 1 through 9.
	 */
	public Cell() {
		this(NO_VALUE);
	}
	/**
	 * Creates a {@code Cell} with the set value {@value}.
	 * @param value a value between 1 and 9 (inclusive) or {@code NO_VALUE} (which is 0).
	 */
	public Cell(int value) {
		possibleValues = new HashSet<Integer>();
		
		setValue(value);
		if(this.value == NO_VALUE) {
			for(int i = MIN_VALUE; i <= MAX_VALUE; i++) {
				possibleValues.add(i);
			};
		}
	}
	/**
	 * Makes a deep copy of the provided {@code Cell}, so that the possible values of the new {@code Cell} are independent of the given one.
	 * @param cell a {@code Cell} to be copied.
	 */
	public Cell(Cell cell) {
		this(cell.getValue());
		possibleValues = new HashSet<Integer>(cell.possibleValues);
	}
	
	/**
	 * @return the {@code Cell}'s {@code value}
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Set a value. This updates the possible values if a non-{@code NO_VALUE}) value is provided.
	 * @param newValue A new value (1 through 9 or {@code NO_VALUE}) to be set.
	 */
	public void setValue(int newValue) {
		value = newValue;
		if(value != NO_VALUE) {
			possibleValues.clear();
			possibleValues.add(value);
		}
	}
	
	/**
	 * Eliminate a possible value.
	 * @param value the value to remove
	 * @return whether the value was removed
	 */
	public boolean removePossibleValue(int value) {
		boolean wasRemoved = possibleValues.remove(Integer.valueOf(value));
		
		//if there is only one possible value left, set the value to this one
		if(possibleValuesSize() == 1) {
			setValue(possibleValues.iterator().next());
		}
		return wasRemoved;
	}
	
	/**
	 * Checks whether a value can be placed in this {@code Cell}
	 * @param value a value to checked
	 * @return whether the given {@code value} could be set
	 */
	public boolean isPossibleValue(int value) {
		return possibleValues.contains(value);
	}
	
	/**
	 * @return the total number of possible values
	 */
	public int possibleValuesSize() {
		return possibleValues.size();
	}
	
	public String toString() {
		String string = "";
		for(Integer value : possibleValues) {
			string += String.valueOf(value);
		}
		return string;
	}
}
