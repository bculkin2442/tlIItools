package tlIItools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Container of a set of affixes.
 *
 * @author Ben Culkin
 */
public class AffixSet {
	/**
	 * All of the affix groups contained in this set.
	 *
	 * An affix group is a set of affixs that generally have the same or
	 * similiar effects, but have different intensities or spawn levels.
	 */
	public Map<String, List<Affix>> affixGroups;

	/**
	 * All of the ungrouped affixes contained in this set.
	 */
	public List<Affix> ungroupedAffixes;

	/**
	 * Create a new blank affix set.
	 */
	public AffixSet() {
		affixGroups = new HashMap<>();

		ungroupedAffixes = new ArrayList<>();
	}
}
