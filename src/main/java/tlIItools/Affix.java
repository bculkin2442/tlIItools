package tlIItools;

import java.util.*;

/** Represents a Torchlight II affix.
 *
 * @author Ben Culkin
 */
public class Affix {
	/** Whether or not to record timing info. */
	public static boolean doTiming;
	/** Internal name of the affix. */
	public String intName;

	/* The prefix/suffix attached to the affix.
	 *
	 * In general, only one of these is set for a given affix.
	 *
	 * NOTE: Some affixes have a mis-set suffix/prefix ordering, and may need to be
	 * changed in their files. */

	/** The 'suffix name' attached to this affix.
	 * 
	 * Include the word [ITEM] to indicate where the item name should go.
	 * 
	 * Note that it is not guaranteed that this is actually a suffix; it could be a
	 * prefix. */
	public String affixSuffix;

	/** The 'prefix name' attached to this affix.
	 * 
	 * Include the word [ITEM] to indicate where the item name should go.
	 * 
	 * Note that it is not guaranteed that this is actually a prefix; it could be a
	 * suffix. */
	public String affixPrefix;

	/* The min/max levels the affix can spawn at. */

	/** The minimum level of an item this affix can spawn on. */
	public int minLevel;
	/** The maximum level of an item this affix can spawn on. */
	public int maxLevel;

	/** The spawn weight for the affix. */
	public int weight;

	/** The number of affix slots taken up. */
	public int slots;

	// @NOTE isEnchantment, isSocketable and isPerson seem to be  mutally
	// exclusive. Should they be a enum? - bculkin, 12/29/2020
	/** Whether this is a socketable affix. */
	public boolean isSocketable;

	/** Whether this is an enchantment, instead of a standard affix. */
	public boolean isEnchantment;
	/** Whether this is a mob/player affix. */
	public boolean isPerson;

	/** The types of equipment this can spawn on. */
	public List<String> equipTypes;
	/** The types of equipment this can't spawn on. */
	public List<String> nonequipTypes;

	/** Places to get this enchantment from; */
	public List<String> enchantSources;

	/** The socketable types this spawns on. */
	public List<String> socketableTypes;

	/** The effects attached to this affix. */
	public List<Effect> effects;

	/** Determine whether this affix is in an 'affix group' with another affix.
	 * 
	 * By 'affix group', what we mean is that it is essentially the same affix, with
	 * generally just differing levels/values.
	 * 
	 * For instance, an affix that granted +2 strength, and one that granted +4
	 * strength would be considered to be in the same affix group (assuming that
	 * both of those strength bonuses had the same effect name).
	 * 
	 * @param afx The affix to check if we are in an affix group with.
	 * 
	 * @return Whether or now we are in an affix group with the specified affix. */
	public boolean isInAffixGroup(Affix afx) {
		if (effects == null) {
			if (afx.effects != null) return false;
		} else if (!effects.equals(afx.effects)) {
			Iterator<Effect> leftIter = effects.iterator();
			Iterator<Effect> rightIter = afx.effects.iterator();
			
			while (leftIter.hasNext() && rightIter.hasNext()) {
			    Effect left = leftIter.next();
			    Effect right = rightIter.next();
			    
			    if (!left.group.equals(right.group)) return false;
			}
		}
		
		if (enchantSources == null) {
			if (afx.enchantSources != null) return false;
		} else if (!enchantSources.equals(afx.enchantSources)) {
			return false;
		} else if (equipTypes == null) {
			if (afx.equipTypes != null) return false;
		} else if (!equipTypes.equals(afx.equipTypes)) {
			return false;
		} else if (isEnchantment != afx.isEnchantment) {
			return false;
		} else if (isPerson != afx.isPerson) {
			return false;
		} else if (isSocketable != afx.isSocketable) {
			return false;
		} else if (nonequipTypes == null) {
			if (afx.nonequipTypes != null) return false;
		} else if (!nonequipTypes.equals(afx.nonequipTypes)) {
			return false;
		} else if (socketableTypes == null) {
			if (afx.socketableTypes != null) return false;
		} else if (!socketableTypes.equals(afx.socketableTypes)) {
			return false;
		} else {
			return true;
		}
		return true;
	}

	/** Gets the name of the 'affix group' that this affix is in.
	 * 
	 * By 'affix group', what we mean is that it is essentially the same affix, with
	 * generally just differing levels/values.
	 * 
	 * For instance, an affix that granted +2 strength, and one that granted +4
	 * strength would be considered to be in the same affix group (assuming that
	 * both of those strength bonuses had the same effect name, and applied to the
	 * same sorts of items).
	 * 
	 * @return The name of the affix group */
	public String getAffixGroupName() {
		StringBuilder sb = new StringBuilder();

		for (Effect eft : effects) sb.append(eft.group);
		for (String enchantSource : enchantSources) sb.append(enchantSource);
		for (String equipType : equipTypes) sb.append(equipType);
		for (String nonEquipType : nonequipTypes) sb.append(nonEquipType);
		for (String socketableType : socketableTypes) sb.append(socketableType);

		return sb.toString();
	}

	/* Are invalid equip types being added?
	 * 
	 * NOTE: This is kinda bad practice. It should really be handled via two
	 * separate affix methods, one to add a valid affix, one to add an invalid, w/
	 * the caller keeping tracking/calling the right one. */
	private boolean inNonEquip;

	/** Create a new blank affix. */
	public Affix() {
		equipTypes      = new ArrayList<>();
		nonequipTypes   = new ArrayList<>();
		enchantSources  = new ArrayList<>();
		socketableTypes = new ArrayList<>();
		effects         = new ArrayList<>();
	}

	/** Sets the set of equip types that is added to.
	 *
	 * @param nonequip True if the equip types being added are prohibited, or false
	 *                 if they are allowed. */
	public void setEquipType(boolean nonequip) {
		inNonEquip = nonequip;
	}

	/** Add an equip type to the right set of equip types.
	 *
	 * @param type The equip type to add. */
	public void addEquipType(String type) {
		if (inNonEquip) {
			nonequipTypes.add(type);
		} else {
			if (type.equals("SOCKETABLE") || type.contains(" EMBER")) {
				isSocketable = true;
				socketableTypes.add(type);
			} else if (type.startsWith("ENCHANTER")) {
				isEnchantment = true;
				enchantSources.add(type.substring(10));
			} else if (type.equals("MONSTER") || type.equals("PLAYER")) {
				isPerson = true;
			} else {
				equipTypes.add(type);
			}
		}
	}

	@Override
	public String toString() {
		return intName;
	}

	/** Print out the full details of this affix.
	 * 
	 * @return The full details of the affix. */
	public String toLongString() {
		StringBuilder sb = new StringBuilder();

		if (isSocketable) {
			sb.append("Socketable ");
		} else if (isPerson || (intName != null && intName.startsWith("HERO_"))) {
			sb.append("Personal ");
		} else if (intName != null & intName.startsWith("MAP_")) {
			sb.append("Area ");
		}

		if (isEnchantment) {
			sb.append("Enchantment: ");
		} else {
			sb.append("Affix: ");
		}

		sb.append(intName);
		sb.append("\n");

		if (affixSuffix != null) {
			sb.append("\tSuffix: ");
			sb.append(affixSuffix);
			sb.append("\n");
		}

		if (affixPrefix != null) {
			sb.append("\tPrefix: ");
			sb.append(affixPrefix);
			sb.append("\n");

		}

		sb.append("\t");
		if (minLevel <= 1 && maxLevel == 999) {
			sb.append("No Level Range");
		} else if (minLevel != 1 && maxLevel != 999) {
			sb.append("Level Range: ");
			sb.append(minLevel);
			sb.append("-");
			sb.append(maxLevel);
		} else if (minLevel <= 1) {
			sb.append("Max Level: ");
			sb.append(maxLevel);
		} else if (maxLevel == 999) {
			sb.append("Minimum Level: ");
			sb.append(minLevel);
		}

		sb.append("\n");

		sb.append("\tSpawn Weight: ");
		sb.append(weight);
		sb.append("\n");

		if (slots == 0) {
			sb.append("\tOccupies no slots\n");
		} else {
			sb.append("\tSlots: ");
			sb.append(slots);
			sb.append("\n");
		}

		if (equipTypes.size() != 0) {
			if (isSocketable) {
				sb.append("\tSocketable Into: ");
			} else if (isEnchantment) {
				sb.append("\tEnchants Onto: ");
			} else {
				sb.append("\tSpawns On: ");
			}

			sb.append(equipTypes);
			sb.append("\n");
		}

		// Socketables & enchantments use this as a duplicate.
		if (!isSocketable && !isEnchantment && nonequipTypes.size() != 0) {
			sb.append("\tCan't Spawn On: ");
			sb.append(nonequipTypes);
			sb.append("\n");
		}

		if (enchantSources.size() != 0) {
			sb.append("\tEnchantment Sources: ");
			sb.append(enchantSources);
			sb.append("\n");
		}

		if (socketableTypes.size() != 0) {
			sb.append("\tSocketable Types: ");
			sb.append(socketableTypes);
			sb.append("\n");
		}

		if (effects.size() != 0) {
			sb.append("\tEffects: ");
			for (Effect eft : effects) {
				sb.append("\n\t\t");
				sb.append(eft.toString());
			}
			sb.append("\n");
		}
		
		/*
		sb.append("Affix Group: ");
		String afxGroupName = getAffixGroupName();
		sb.append(afxGroupName);
		sb.append(" (ID: ");
		sb.append(afxGroupName.hashCode());
		sb.append(")");
		sb.append("\n");
		*/
		
		return sb.toString();
	}

	/** Load an affix from an input source.
	 *
	 * @param scn The input source to load from.
	 *
	 * @param fName The name of the input source in question.
	 *
	 * @return The loaded affix. */
	public static Affix loadAffix(Scanner scn, String fName) {
		return loadAffix(scn, fName, new ArrayList<>());
	}

	/** Load an affix from an input source.
	 * 
	 * @param scn The input source to read from.
	 * @param scnName The name of the input source.
	 * @param errors A list to stick errors encountered during loading the affix.
	 *
	 * @return The affix, loaded from the file. */
	public static Affix loadAffix(Scanner scn, String scnName, List<String> errors) {
		Affix afx = new Affix();

		long startTime = System.nanoTime();

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();
			ln = ln.replaceAll("\\p{Cntrl}", "");

			if (ln.contains("[NOT_UNITTYPES]")) {
				afx.setEquipType(true);
				continue;
			} else if (ln.contains("[UNITTYPES]")) {
				afx.setEquipType(false);
				continue;
			}

			String[] splits = ln.split(":");
			if (ln.contains("<TRANSLATE>")) {
				splits[0] = splits[0].substring(11);

				switch (splits[0]) {
				case "SUFFIX":
					afx.affixSuffix = splits[1];
					break;
				case "PREFIX":
					afx.affixPrefix = splits[1];
					break;
				default:
					String msg = String.format(
							"Misformed affix translation: (%s) (%s) (%s)\n",
							splits[0], splits[1], scnName);
					errors.add(msg);
				}
			} else if (ln.contains("MIN_SPAWN_RANGE")) {
				afx.minLevel = Integer.parseInt(splits[1]);
			} else if (ln.contains("MAX_SPAWN_RANGE")) {
				afx.maxLevel = Integer.parseInt(splits[1]);
			} else if (ln.contains("WEIGHT:")) {
				afx.weight = Integer.parseInt(splits[1]);
			} else if (ln.contains("SLOTS_OCCUPY")) {
				afx.weight = Integer.parseInt(splits[1]);
			} else if (ln.contains("UNITTYPE") && !ln.contains("/")) {
				if (splits.length == 1)
					errors.add(String.format(
							"Malformed equip type: (%s) (%s)\n",
							splits[0], scnName));

				afx.addEquipType(splits[1]);
			} else if (splits[0].equals("<STRING>NAME")) {
				if (splits.length == 1)
					errors.add(String.format(
							"Malformed name: (%s) (%s)\n",
							splits[0], scnName));

				afx.intName = splits[1];
			} else if (ln.contains("[EFFECT]")) {
				List<String> eftErrs = new ArrayList<>();

				Effect eft = Effect.parseEffect(afx, scn, scnName, errors);
				errors.addAll(eftErrs);

				afx.effects.add(eft);
			}
		}

		// Sort effects, so that they are in a stable order, even if specified out of
		// order
		afx.effects.sort(Comparator.comparingInt((val) -> val.hashCode()));

		long endTime = System.nanoTime();
		if (doTiming) {
			String fmt = "\tProcessed affix %s from %s in %d nanoseconds (%.2f seconds)\n\n";
			double seconds = ((double) (endTime - startTime) / 1000000000);

			errors.add(String.format(
					fmt,
					afx.intName, scnName, endTime - startTime, seconds));
		}

		return afx;
	}
}
