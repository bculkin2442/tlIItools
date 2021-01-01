package tlIItools;

import java.util.*;

/** Container of a set of affixes.
 *
 * @author Ben Culkin */
public class AffixSet {
	private static class AffixComparator implements Comparator<Affix> {
		@Override
		public int compare(Affix a1, Affix a2) {
			return a1.spawnRange.compareTo(a2.spawnRange);
		}
	}

	/** All of the affix groups contained in this set.
	 *
	 * An affix group is a set of affixes that generally have the same or
	 * similar effects, but have different intensities or spawn levels. */
	public Map<AffixGroup, Set<Affix>> affixGroups;

	/** All of the ungrouped affixes contained in this set. */
	public Set<Affix> ungroupedAffixes;

	/** Create a new blank affix set. */
	public AffixSet() {
		affixGroups = new TreeMap<>();

		ungroupedAffixes = new TreeSet<>(new AffixComparator());
	}

	/** Add an affix to this set.
	 * 
	 * @param afx The affix to add. */
	public void addAffixByContents(Affix afx) {
		AffixGroup group = afx.toAffixGroup();
		String afxGroup = group.toString();

		if (afxGroup.equals("")) {
			ungroupedAffixes.add(afx);
		} else {
			affixGroups.compute(group, (key, val) -> {
				if (val == null) {
					Set<Affix> afxSet = new HashSet<>();
					afxSet.add(afx);
					return afxSet;
				} else {
					val.add(afx);
					return val;
				}
			});
		}
	}
}
