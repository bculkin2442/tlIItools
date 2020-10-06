package tlIItools;

import java.util.*;

/**
 * Container of a set of affixes.
 *
 * @author Ben Culkin
 */
public class AffixSet {
	private static class AffixComparator implements Comparator<Affix> {
		@Override
		public int compare(Affix a1, Affix a2) {
			if (a1.minLevel == a2.minLevel) {
				return a1.maxLevel - a2.maxLevel;
			}

			return a1.minLevel - a2.minLevel;
		}
	}

	/**
	 * All of the affix groups contained in this set.
	 *
	 * An affix group is a set of affixes that generally have the same or
	 * similar effects, but have different intensities or spawn levels.
	 */
	public Map<String, Set<Affix>> affixGroups;

	/**
	 * All of the ungrouped affixes contained in this set.
	 */
	public Set<Affix> ungroupedAffixes;

	/**
	 * Create a new blank affix set.
	 */
	public AffixSet() {
		affixGroups = new TreeMap<>();

		ungroupedAffixes = new TreeSet<>(new AffixComparator());
	}
	
	/**
	 * Add an affix to this set.
	 * 
	 * @param afx The affix to add.
	 */
	public void addAffixByContents(Affix afx) {
		String afxGroup = afx.getAffixGroupName();
		
		if (afxGroup.equals("")) {
			ungroupedAffixes.add(afx);
		} else {
			affixGroups.compute(afxGroup, (key, val) -> {
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
