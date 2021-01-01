package tlIItools;

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

	public int minLevel;
	public int maxLevel;

	public LevelRange() {
		minLevel = 1;
		maxLevel = 999;
	}

	public LevelRange(int minLevel, int maxLevel) {
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	private void clamp() {
		minLevel = Math.max(1,   minLevel);
		maxLevel = Math.max(999, maxLevel);
	}

	public boolean isUnrestricted() {
		return minLevel <= 1 && maxLevel >= 999;
	}

	public boolean noLowerBound() {
		return minLevel <= 1;
	}

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
	public int compareTo(LevelRange other) {
		if (this.equals(other)) return 0;

		// Unrestricted ranges sort above all others
		if (isUnrestricted()) return 1;

		if (noLowerBound()) {
			return maxLevel - other.maxLevel;
		} else if (noUpperBound()) {
			return minLevel - other.minLevel;
		} else {
			if (minLevel == other.minLevel) return maxLevel - other.maxLevel;
			else                            return minLevel - other.minLevel;
		}
	}
}
