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
 *
 * @author Ben Culkin
 */
public class Effect {
	/**
	 * Count of all loaded effects.
	 */
	public static int effectCount = 0;
	/**
	 * Do timing analysis when loading effects.
	 */
	public static boolean doTiming;

	/**
	 * The list of detail strings for skills.
	 */
	private static Map<String, String> detals;
	/**
	 * The list of detail strings for timed skills.
	 */
	private static Map<String, String> timeDetals;

	/**
	 * The list of replacements for detail strings.
	 */
	private static List<ReplPair> replList;

	/*
	 * Init. lists from files.
	 */
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

	/**
	 * Sanity check the loaded format strings.
	 */
	public static void sanityCheckFormats() {
		for (Entry<String, String> detal : detals.entrySet()) {
			String fmt = detal.getValue();

			AffixLister.errOut.printf("\tTRACE: Applying replacements for %s\n", detal.getKey());
			for (ReplPair repl : replList) {
				String tmp = fmt;
				fmt = fmt.replaceAll(repl.find, repl.replace);
				if (!fmt.equals(tmp)) {
					String outFmt = "\t\tTRACE: Replaced %s with %s: \n\t\t%s\n\t\t%s\n";

					AffixLister.errOut.printf(outFmt, repl.find, repl.replace, tmp, fmt);
				}
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				String warnFmt = "WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n";

				AffixLister.errOut.printf(warnFmt, detal.getKey(), fmt);
			}
		}

		for (Entry<String, String> detal : timeDetals.entrySet()) {
			String fmt = detal.getValue();

			for (ReplPair repl : replList) {
				fmt = fmt.replaceAll(repl.find, repl.replace);
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				String warnFmt = "WARN: Details for timed effect %s are malformatted (contains < or >):\n\t%s\n";
				AffixLister.errOut.printf(warnFmt, detal.getKey(), fmt);
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
				AffixLister.errOut.printf("WARN: Details for effect %s are malformatted (contains < or >):\n\t%s\n", type, fmt);
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

	public static Effect parseEffect(Affix afx, Scanner scn, String scnSource) {
		return parseEffect(afx, scn, scnSource, new ArrayList<>());
	}

	public static Effect parseEffect(Affix afx, Scanner scn, String scnSource, List<String> errs) {
		Effect efct = new Effect();

		long startTime = System.nanoTime();

		efct.fName = scnSource;

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();
			ln = ln.replaceAll("\\p{Cntrl}", "");

			if (ln.contains("[/EFFECT]")) break;

			String[] splits = ln.split(":");

			// Empty field
			if (splits.length == 1) continue;

			if (ln.contains("NAME")) {
				efct.name = splits[1];
			} else if (ln.contains("DAMAGE_TYPE")) {
				efct.damageType = splits[1];
			} else if (ln.contains("TYPE")) {
				efct.type = splits[1];
			} else if (ln.contains("ACTIVATION")) {
				switch (splits[1]) {
					case "DYNAMIC":
					case "PASSIVE":
						// Passive is the default, and
						// dynamic doesn't have much
						// actual difference.
						break;
					case "TRANSFER":
						efct.isTransfer = true;
						break;
					default:
						errs.add(String.format("Malformed activation type: (%s) (%s) (%s)\n", splits[1], efct.name, afx.intName));
				}
			} else if (ln.contains("DURATION")) {
				if (splits[1].equals("ALWAYS")) {
					efct.hasDuration = false;

					efct.duration = Double.POSITIVE_INFINITY;
				} else if (splits[1].equals("INSTANT")) {
					efct.hasDuration = false;

					efct.duration = Double.NaN;
				} else if (splits[1].equals("PERCENT")) {
					efct.hasDuration = false;

					efct.duration = Double.NaN;

					errs.add(String.format("WARN: Punting on DURATION:PERCENT for %s\n", scnSource));
				} else if (splits[1].equals("0")) {
					efct.hasDuration = false;
					efct.duration = 0.0;
				} else {
					efct.hasDuration = true;

					if (splits[1].equalsIgnoreCase("instant")) {
						efct.duration = -1;
					} else {
						efct.duration = Double.parseDouble(splits[1]);
					}
				}
			} else if (ln.contains("<FLOAT>MIN:")) {
				efct.minValue = Double.parseDouble(splits[1]);
			} else if (ln.contains("<FLOAT>MAX:")) {
				efct.maxValue = Double.parseDouble(splits[1]);
			} else if (ln.contains("USEOWNERLEVEL")) {
				// We don't care about this, for now
			} else if (ln.contains("LEVEL:")) {
				efct.level = Integer.parseInt(splits[1]);
			} else if (ln.contains("EXCLUSIVE")) {
				efct.exclusive = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("GRAPHOVERRIDE")) {
				efct.graphOverride = splits[1];
			} else if (ln.contains("USEOWNERLEVEL")) {
				efct.ownerLevel = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("NOGRAPH")) {
				efct.useGraph = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("STATNAME")) {
				efct.statName = splits[1];
			} else if (ln.contains("STATPERCENT")) {
				efct.statPercent = Double.parseDouble(splits[1]);
			} else if (ln.contains("STATMODIFIERISBONUS")) {
				efct.isStatBonus = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("RESISTANCE:")) {
				efct.resist = Double.parseDouble(splits[1]);
			} else if (ln.contains("FORCE:")) {
				efct.resist = Double.parseDouble(splits[1]);
			} else if (ln.contains("MIN_PER")) {
				efct.minPer = Double.parseDouble(splits[1]);
			} else if (ln.contains("MAX_PER")) {
				efct.maxPer = Double.parseDouble(splits[1]);
			} else if (ln.contains("RANGE:") || ln.contains("RADIUS")) {
				efct.range = Double.parseDouble(splits[1]);
			} else if (ln.contains("MAX_COUNT:") || ln.contains("MAX_TARGETS")) {
				efct.maxCount = Double.parseDouble(splits[1]);
			} else if (ln.contains("PULSE_RATE")) {
				efct.pulse = Double.parseDouble(splits[1]);
			} else if (ln.contains("CHANCE:")) {
				// NOTE: Should really use its own field
				efct.resist = Double.parseDouble(splits[1]);
			}
		}

		long endTime = System.nanoTime();
		if (doTiming) {
			String fmt = "\t\tProcessed effect %s from %s in %d nanoseconds (%.2f seconds)\n";

			double seconds = ((double)((endTime - startTime) / 1000000000));
			errs.add(String.format(fmt, efct.name, scnSource, endTime - startTime, seconds));
		}

		effectCount += 1;

		return efct;
	}
}
