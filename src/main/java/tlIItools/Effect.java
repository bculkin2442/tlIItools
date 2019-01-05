package tlIItools;

import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Represents an effect attached to an affix.
 */
public class Effect {
	private static Map<String, String> detals;
	private static Map<String, String> timeDetals;

	private static List<ReplPair> replList;

	static {
		try (FileReader detalReader = new FileReader("data/affix-detals.txt")) {
			detals = readDetails(new Scanner(detalReader));
		} catch (IOException ioex) {
			AffixLister.errOut.println("Error loading affix detail text");
		}

		try (FileReader timedDetalReader = new FileReader("data/timed-affix-detals.txt")) {
			timeDetals = readDetails(new Scanner(timedDetalReader));
		} catch (IOException ioex) {
			AffixLister.errOut.println("Error loading timed affix detail text");
		}

		try (FileReader replListReader = new FileReader("data/replace-list.txt")) {
			replList = ReplPair.readList(new Scanner(replListReader));
		} catch (IOException ioex) {
			AffixLister.errOut.println("Error loading replacement lists");
		}
	}

	/**
	 * Read effect detail strings from an input source.
	 *
	 * @param scn
	 * 	The source to read from.
	 * @return
	 * 	The map of effect details to use.
	 */
	public static Map<String, String> readDetails(Scanner scn) {
		Map<String, String> detals = new HashMap<>();

		return readDetails(detals, scn);
	}

	/**
	 * Read effect detail strings from an input source, adding to an
	 * existing set.
	 *
	 * @param detals
	 * 	The details to add to.
	 * @param scn
	 * 	The source to read from.
	 * @return
	 * 	The map of effect details to use.
	 */
	public static Map<String, String> readDetails(Map<String, String> detals, Scanner scn) {
		while (scn.hasNextLine()) {
			String name = scn.nextLine().trim();
			if (name.equals("")) continue;
			if (name.startsWith("#")) continue;

			String body;
			do {
				body = scn.nextLine().trim();
			} while (body.startsWith("#"));

			detals.put(name, body);
		}

		return detals;
	}

	private static void sanityCheckFormats() {
		for (Entry<String, String> detal : detals.entrySet()) {
			String fmt = detal.getValue();

			AffixLister.errOut.printf("\tTRACE: Applying replacements for %s\n", detal.getKey());
			for (ReplPair repl : replList) {
				String tmp = fmt;
				fmt = fmt.replaceAll(repl.find, repl.replace);
				if (!fmt.equals(tmp)) {
					AffixLister.errOut.printf("\t\tTRACE: Replaced %s with %s: \n\t\t%s\n\t\t%s\n", repl.find, repl.replace, tmp, fmt);
				}
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				AffixLister.errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", detal.getKey(), fmt);
			}
		}

		for (Entry<String, String> detal : timeDetals.entrySet()) {
			String fmt = detal.getValue();

			for (ReplPair repl : replList) {
				fmt = fmt.replaceAll(repl.find, repl.replace);
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				AffixLister.errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", detal.getKey(), fmt);
			}
		}
	}
	/**
	 * The file name this effect came from.
	 */
	public String fName;

	/**
	 * The name of the effect.
	 */
	public String name;

	/**
	 * The specific effect that happens.
	 */
	public String type;

	/**
	 * Damage type for the effect, if applicable.
	 */
	public String damageType = "physical";

	/**
	 * Duration of the effect.
	 */
	public double duration;

	/**
	 * Whether or not we have a duration or not.
	 */
	public boolean hasDuration;

	/**
	 * Minimum value for the effect.
	 */
	public double minValue;
	/**
	 * Maximum value for the effect.
	 */
	public double maxValue;

	/**
	 * The name of the stat that applies to this affect.
	 */
	public String statName;
	/**
	 * The percent of the stat value to apply.
	 */
	public double statPercent;
	/**
	 * Whether or not this stat is a bonus value.
	 */
	public boolean isStatBonus;

	/**
	 * Whether or not this uses the owners level to modify
	 * any applicable graph.
	 */
	public boolean ownerLevel;

	/**
	 * The graph to use instead of the default graph.
	 */
	public String graphOverride;

	/**
	 * Whether or not a graph is used for this effect.
	 */
	public boolean useGraph = true;

	/**
	 * Whether this effect can stack with itself.
	 */
	public boolean exclusive;

	/**
	 * The amount the targets armor is reduced by for this
	 * effect.
	 */
	public double soakScale = 1.0;

	/**
	 * Level of the effect.
	 */
	public int level = -1;

	/**
	 * Whether or not this effect is a 'transfer'
	 * effect (Applied to the enemy on a hit).
	 */
	public boolean isTransfer;

	/**
	 * The amount to resist/do knockback by.
	 */
	public double resist;

	/**
	 * Minimum value per monster.
	 */
	public double minPer;

	/**
	 * Maximum value per monster.
	 */
	public double maxPer;

	/**
	 * Range for effect.
	 */
	public double range;

	/**
	 * Maximum count of monsters.
	 */
	public double maxCount;

	/**
	 * The rate at which the effect fires.
	 */
	public double pulse;
	/**
	 * Create a new blank effect.
	 */
	public Effect() {

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isTransfer) {
			sb.append("Inflict on Hit: ");
		}

		Map<String, String> detMap = hasDuration ? timeDetals : detals;

		if (detMap.containsKey(type) || (hasDuration && !timeDetals.containsKey(type) && detals.containsKey(type))) {
			String fmt;
			if (hasDuration && !timeDetals.containsKey(type) && detals.containsKey(type)) {
				AffixLister.errOut.printf("Improvised details for timed %s\n", type);
				fmt = detals.get(type) + "for <DUR> seconds";
			} else {
				fmt = detMap.get(type);
			}

			// Expand aliases first.

			for (ReplPair repl : replList) {
				fmt = fmt.replaceAll(repl.find, repl.replace);
			}

			if (minValue <= 0 && maxValue <= 0) {
				fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$1");
			}

			if (minValue >= 0 && maxValue >= 0) {
				fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$2");
			}

			if (minPer <= 0 && maxPer <= 0) {
				fmt = fmt.replaceAll("<MC\\|(\\w+)\\|(\\w+)>", "$1");
			}

			if (minPer >= 0 && maxPer >= 0) {
				fmt = fmt.replaceAll("<MC\\|([^|>]+)\\|([^|>]+)>", "$2");
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				AffixLister.errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", type, fmt);
			}

			sb.append(String.format(fmt, Math.abs(minValue), Math.abs(maxValue), duration, damageType.toLowerCase(), level, resist, name, Math.abs(minPer), Math.abs(maxPer), range, maxCount, pulse));
		} else {
			sb.append("No effect details for effect ");
			sb.append(type);
			sb.append(String.format(" with parameters (min %.2f, max %.2f, dur %.2f, type %s, level %d)", minValue, maxValue, duration, damageType.toLowerCase(), level, name));

			if (AffixLister.addFileName) {
				sb.append(" from file ");
				sb.append(fName);
			}

			if (hasDuration) AffixLister.errOut.print("TIMED: ");
			AffixLister.errOut.println(sb.toString());
		}

		if (name != null) {
			sb.append(" (named ");
			sb.append(name);
			sb.append(")");
		}

		if (exclusive) sb.append(" (Exclusive)");

		if (graphOverride != null) {
			sb.append(" (Uses ");
			sb.append(graphOverride);
			sb.append(" graph)");
		}

		if (ownerLevel) {
			sb.append(" (Uses owner level for graph)");
		}

		if (soakScale != 1.0) {
			String fmt = " (%.2f%% reduced effectiveness of armor)";

			sb.append(String.format(fmt, (1 - soakScale) * 100));
		}

		if (level != -1) {
			sb.append(" (Level ");
			sb.append(level);
			sb.append(")");
		}

		if (statName != null) {
			sb.append(String.format(" (%.2f of stat %s", statPercent, statName));
			if (isStatBonus) sb.append(" as bonus)");
			else             sb.append(")");
		}

		if (!useGraph) sb.append(" (Ignoring graph)");

		return sb.toString();
	}
}
