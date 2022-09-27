package tlIItools;

import java.util.*;

/**
 * Represents a range of levels.
 * 
 * @author bjcul
 *
 */
public class LevelRange implements Comparable<LevelRange> {
	/*
	 * if (minLevel <= 1 && maxLevel == 999) {
	 * sb.append("No Level Range");
	 * } else if (minLevel != 1 && maxLevel != 999) { 
	 * sb.append("Level Range: ");
	 * sb.append(minLevel);
	 * sb.append("-");
	 * sb.append(maxLevel);
	 * } else if (minLevel <= 1) { 
	 * sb.append("Max Level: ");
	 * sb.append(maxLevel);
	 * } else if (maxLevel == 999) {
	 * sb.append("Minimum Level: ");
	 * sb.append(minLevel);
	 * } */

	/**
	 * The maximum level for this range.
	 */
	public int minLevel;
	/**
	 * The minimum level for this range.
	 */
	public int maxLevel;

	/**
	 * Create a new blank level range
	 */
	public LevelRange() {
		minLevel = 1;
		maxLevel = 999;
	}

	/**
	 * Create a new set level range.
	 * 
	 * @param minLevel The minimum level
	 * @param maxLevel The maximum level
	 */
	public LevelRange(int minLevel, int maxLevel) {
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	private void clamp() {
		minLevel = Math.max(1,   minLevel);
		maxLevel = Math.min(999, maxLevel);
	}

	/**
	 * Check if this level range is 'unrestricted'
	 * 
	 * @return Whether the level range is unrestricted
	 */
	public boolean isUnrestricted() {
		return minLevel <= 1 && maxLevel >= 999;
	}

	/**
	 * Check if this range has no lower bound
	 * 
	 * @return Whether the range has no lower bound
	 */
	public boolean noLowerBound() {
		return minLevel <= 1;
	}

	/**
	 * Check if this range has no upper bound
	 * 
	 * @return Whether the range has no upper bound
	 */
	public boolean noUpperBound() {
		return maxLevel >= 999;
	}

	@Override
	public String toString() {
		clamp();

		StringBuilder sb = new StringBuilder();

		if (minLevel <= 1 && maxLevel >= 999) {
			sb.append("No Level Range");
		} else if (minLevel > 1 && maxLevel < 999) { 
			sb.append("Level Range: ");
			sb.append(minLevel);
			sb.append("-");
			sb.append(maxLevel);
		} else if (minLevel <= 1) { 
			sb.append("Max Level: ");
			sb.append(maxLevel);
		} else if (maxLevel >= 999) {
			sb.append("Minimum Level: ");
			sb.append(minLevel);
		}

		return sb.toString();
	}

	@Override
    public int hashCode() {
	    clamp();
	    
        return Objects.hash(maxLevel, minLevel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;

        LevelRange other = (LevelRange) obj;
        
        clamp();
        other.clamp();
        
		if (isUnrestricted() && other.isUnrestricted()) {
			return true;
		} else if (noLowerBound()) {
			if (other.noLowerBound()) return maxLevel == other.maxLevel;
			else                      return false;
		} else if (noUpperBound()) {
			if (other.noUpperBound()) return minLevel == other.minLevel;
			else                      return false;
		} else {
			return maxLevel == other.maxLevel && minLevel == other.minLevel;
		}
    }

    @Override
	public int compareTo(LevelRange other) {
		clamp();
		other.clamp();

		if (this.equals(other)) return 0;

		// Unrestricted ranges sort above all others
		if (isUnrestricted())       return 1;
		if (other.isUnrestricted()) return -1;

		if (noLowerBound()) {
		        if (other.noLowerBound()) {
		            return maxLevel - other.maxLevel;
		        }
				return -1;
		} else if (noUpperBound()) {
			if (other.noUpperBound()) {
				return minLevel - other.minLevel;
			}
			return 1;
		} else {
			if (minLevel == other.minLevel) return maxLevel - other.maxLevel;
			return minLevel - other.minLevel;
		}
	}
}
