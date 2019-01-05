package tlIItools;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Torchlight II affix.
 */
public class Affix {
	/**
	 * Internal name of the affix.
	 */
	public String intName;

	/**
	 * The prefix/suffix attached to the affix.
	 *
	 * In general, only one of these is set for a given affix.
	 *
	 * NOTE: Some affixes have a mis-set suffix/prefix ordering, and
	 * may need to be changed.
	 */
	public String affixSuffix;
	public String affixPrefix;

	/**
	 * The min/max levels the affix can spawn at.
	 */
	public int minLevel;
	public int maxLevel;

	/**
	 * The spawn weight for the affix.
	 */
	public int weight;

	/**
	 * The number of affix slots taken up.
	 */
	public int slots;

	/**
	 * Whether this is a socketable affix.
	 */
	public boolean isSocketable;
	/**
	 * Whether this is an enchantment, instead of a standard affix.
	 */
	public boolean isEnchantment;
	/**
	 * Whether this is a mob/player affix.
	 */
	public boolean isPerson;

	/**
	 * The types of equipment this can spawn on.
	 */
	public List<String> equipTypes;
	/**
	 * The types of equipment this can't spawn on.
	 */
	public List<String> nonequipTypes;

	/**
	 * Places to get this enchantment from;
	 */
	public List<String> enchantSources;

	/**
	 * The socketable types this spawns on.
	 */
	public List<String> socketableTypes;

	/**
	 * The effects attached to this affix.
	 */
	public List<Effect> effects;

	/*
	 * Are invalid equip types being added?
	 */
	private boolean inNonEquip;

	/**
	 * Create a new blank affix.
	 */
	public Affix() {
		equipTypes      = new ArrayList<>();
		nonequipTypes   = new ArrayList<>();
		enchantSources  = new ArrayList<>();
		socketableTypes = new ArrayList<>();
		effects         = new ArrayList<>();
	}

	/**
	 * Sets the set of equip types that is added to.
	 *
	 * @param nonequip 
	 * 	True if the equip types being added are prohibited, or
	 * 	false if they are allowed.
	 */
	public void setEquipType(boolean nonequip) {
		inNonEquip = nonequip;
	}

	/**
	 * Add an equip type to the right set of equip types.
	 *
	 * @param type
	 * 	The equip type to add.
	 */
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
			} else if (type.equals("MONSTER") || type.equals("PLAYER"))  {
				isPerson = true;
			} else {
				equipTypes.add(type);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isSocketable)  sb.append("Socketable ");
		else if (isPerson || (intName != null && intName.startsWith("HERO_"))) sb.append("Personal ");
		else if (intName != null & intName.startsWith("MAP_")) sb.append("Area ");

		if (isEnchantment) sb.append("Enchantment: ");
		else               sb.append("Affix: ");

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
			if (isSocketable) sb.append("\tSocketable Into: ");
			else if (isEnchantment) sb.append("\tEnchants Onto: ");
			else sb.append("\tSpawns On: ");
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

		return sb.toString();
	}
}
