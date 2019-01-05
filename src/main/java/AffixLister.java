import java.io.IOException;
import java.io.FileReader;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 * Lists randomly generated affixes for Torchlight II gear.
 *
 * @author Ben Culkin
 */
public class AffixLister {
	private static int effectCount = 0;

	private static boolean doTiming = false;
	private static boolean addFileName = false;

	public static PrintStream normOut = System.out;
	public static PrintStream errOut = System.err;

	private static Map<String, String> readEffectDetails(Scanner scn) {
		Map<String, String> detals = new HashMap<>();

		while (scn.hasNextLine()) {
			String name = scn.nextLine().trim();
			if (name.equals("")) continue;
			if (name.startsWith("#")) continue;
			if (name.startsWith("alias ")) {
				String[] pts = name.split("\\t");

				detals.put(pts[1], detals.get(pts[2]));
			}

			String body;
			do {
				body = scn.nextLine().trim();
			} while (body.startsWith("#"));

			detals.put(name, body);
		}

		return detals;
	}

	/**
	 * Represents an effect attached to an affix.
	 */
	public static class Effect {
		private static Map<String, String> detals;
		private static Map<String, String> timeDetals;

		private static List<ReplPair> replList;

		static {
			try (FileReader detalReader = new FileReader("data/affix-detals.txt")) {
				detals = readEffectDetails(new Scanner(detalReader));
			} catch (IOException ioex) {
				errOut.println("Error loading affix detail text");
			}

			try (FileReader timedDetalReader = new FileReader("data/timed-affix-detals.txt")) {
				timeDetals = readEffectDetails(new Scanner(timedDetalReader));
			} catch (IOException ioex) {
				errOut.println("Error loading timed affix detail text");
			}

			try (FileReader replListReader = new FileReader("data/replace-list.txt")) {
				replList = ReplPair.readList(new Scanner(replListReader));
			} catch (IOException ioex) {
				errOut.println("Error loading replacement lists");
			}
		}

		private static void sanityCheckFormats() {
			for (Entry<String, String> detal : detals.entrySet()) {
				String fmt = detal.getValue();

				errOut.printf("\tTRACE: Applying replacements for %s\n", detal.getKey());
				for (ReplPair repl : replList) {
					String tmp = fmt;
					fmt = fmt.replaceAll(repl.find, repl.replace);
					if (!fmt.equals(tmp)) {
						errOut.printf("\t\tTRACE: Replaced %s with %s: \n\t\t%s\n\t\t%s\n", repl.find, repl.replace, tmp, fmt);
					}
				}

				if (fmt.contains("<") || fmt.contains(">")) {
					errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", detal.getKey(), fmt);
				}
			}

			for (Entry<String, String> detal : timeDetals.entrySet()) {
				String fmt = detal.getValue();

				for (ReplPair repl : replList) {
					fmt = fmt.replaceAll(repl.find, repl.replace);
				}

				if (fmt.contains("<") || fmt.contains(">")) {
					errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", detal.getKey(), fmt);
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
					errOut.printf("Improvised details for timed %s\n", type);
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
					errOut.printf("WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n", type, fmt);
				}

				sb.append(String.format(fmt, Math.abs(minValue), Math.abs(maxValue), duration, damageType.toLowerCase(), level, resist, name, Math.abs(minPer), Math.abs(maxPer), range, maxCount, pulse));
			} else {
				sb.append("No effect details for effect ");
				sb.append(type);
				sb.append(String.format(" with parameters (min %.2f, max %.2f, dur %.2f, type %s, level %d)", minValue, maxValue, duration, damageType.toLowerCase(), level, name));

				if (addFileName) {
					sb.append(" from file ");
					sb.append(fName);
				}

				if (hasDuration) errOut.print("TIMED: ");
				errOut.println(sb.toString());
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

	/**
	 * Represents a Torchlight II affix.
	 */
	public static class Affix {
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

	/**
	 * Indicates how to treat affixes with regards to their names.
	 */
	public static enum NameMode {
		/**
		 * Show all affixes.
		 */
		ALL,
		/**
		 * Show only unnamed affixes.
		 */
		UNNAMED,
		/**
		 * Show only named affixes.
		 */
		NAMED;
	}

	private static int readNamesFromFile(Map<String, List<String>> fNames, String from, String defGroup, boolean guessGroups) {
		int numFiles = 0;
		try (FileReader fr = new FileReader(from)) {
			Scanner scn = new Scanner(fr);

			String curGroup      = defGroup;
			List<String> curList = fNames.get(curGroup);

			while (scn.hasNextLine()) {
				String ln = scn.nextLine();

				if (ln.startsWith("#")) {
					curGroup = ln.substring(1);

					if (!fNames.containsKey(curGroup)) {
						curList = new ArrayList<>();
						fNames.put(curGroup, curList);
					} else {
						curList  = fNames.get(curGroup);
					}
				} else if (guessGroups && ln.contains("/mods/")) {
					curGroup = ln.replaceAll(".*/mods/([^/]+)/*", "$1");

					if (!fNames.containsKey(curGroup)) {
						curList = new ArrayList<>();
						fNames.put(curGroup, curList);
					} else {
						curList  = fNames.get(curGroup);
					}
				} else {
					numFiles += 1;
					curList.add(ln);
				}
			}
		} catch (IOException ioex) {
			errOut.printf("Error reading names from file %s\n", from);
			ioex.printStackTrace(errOut);
			errOut.println();
		}

		return numFiles;
	}
	/**
	 * Main method.
	 *
	 * @param args The names of the files to read affix data from.
	 */
	public static void main(String[] args) {
		boolean doingArgs  = true;

		boolean omitZeros   = false;
		boolean listZeros   = false;
		boolean guessGroups = false;

		NameMode nameMode = NameMode.ALL;

		long startTime = System.nanoTime();

		int fCount = 0;

		int namedCount   = 0;
		int unnamedCount = 0;
		int zeroCount    = 0;
		int groupCount   = 0;

		Map<String, List<String>> groupContents = new HashMap<>();

		List<String> nonGroupContents = new ArrayList<>();

		int argCount = 0;

		Map<String, List<String>> fGroups = new HashMap<>();
		List<String> fNames = new ArrayList<>();

		String curGroup = "default";
		fGroups.put(curGroup, fNames);
		for(int i = 0; i < args.length; i++) {
			String fName = args[i];

			if (doingArgs && fName.startsWith("-")) {
				boolean isArg = true;
				switch (fName) {
				case "--":
					doingArgs = false;
					break;
				case "--omit-zero":
				case "-z":
					omitZeros = true;
					break;
				case "--no-omit-zero":
				case "-Z":
					omitZeros = false;
					break;
				case "--list-zero":
				case "-l":
					listZeros = true;
					break;
				case "--no-list-zero":
				case "-L":
					listZeros = false;
					break;
				case "--timing":
				case "-t":
					doTiming = true;
					break;
				case "--no-timing":
				case "-T":
					doTiming = false;
					break;
				case "--file-names":
				case "-f":
					addFileName = true;
					break;
				case "--no-file-names":
				case "-F":
					addFileName = true;
					break;
				case "--name-mode":
				case "-n":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: name mode argument requires the mode to use be specified (all, unnamed or named)\n");
						break;
					}

					nameMode = NameMode.valueOf(args[++i].toUpperCase());
					break;
				case "--file-group":
				case "-g":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: file group argument requires the group name to use be specified\n");
						break;
					}

					curGroup = args[++i];
					if (fGroups.containsKey(curGroup)) {
						fNames = fGroups.get(curGroup);
					} else {
						fNames = new ArrayList<>();
						fGroups.put(curGroup, fNames);
					}
					break;
				case "--guess-groups":
					guessGroups = true;
					break;
				case "--no-guess-groups":
					guessGroups = false;
					break;
				case "--read-names-from-file":
				case "-r":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: read name file argument requires the file to use be specified\n");
						break;
					}

					fCount += readNamesFromFile(fGroups, args[++i], curGroup, guessGroups);
					break;
				case "--output":
				case "-o":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: output file argument requires the file to use be specified\n");
						break;
					}
					try {
						normOut = new PrintStream(args[++i]);
					} catch (IOException ioex) {
						errOut.printf("Could not open output file %s\n", args[i]);

						ioex.printStackTrace(errOut);
					}
					break;
				case "--output-errors":
				case "-e":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: error output file argument requires the file to use be specified\n");
						break;
					}
					try {
						errOut = new PrintStream(args[++i]);
					} catch (IOException ioex) {
						errOut.printf("Could not open error output file %s\n", args[i]);

						ioex.printStackTrace(errOut);
					}
					break;
				default:
					isArg = false;
				}

				if (isArg) {
					argCount += 1;
					continue;
				}

				fCount += 1;
				fNames.add(fName);
			} else {
				fCount += 1;
				fNames.add(fName);
			}
		}

		for (Entry<String, List<String>> fGroup : fGroups.entrySet()) {
			if (fGroup.getValue().size() == 0) continue;
			normOut.printf("\nFile Group '%s' starting\n", fGroup.getKey());
		for (String fName : fGroup.getValue()) {
			try (FileReader fr = new FileReader(fName)) {
				Scanner sc = new Scanner(fr);

				Affix afx = processFile(sc, fName);

				if (afx.intName != null && afx.weight != 0) {
					String groupRx = "(.*_?)\\d+\\Z";
					boolean hasGroup = afx.intName.matches(groupRx);
					String groupName = afx.intName.replaceAll(groupRx, "$1");
					
					if (!groupContents.containsKey(groupName)) {
						groupCount += 1;
						// errOut.printf("\tTRACE: Counted distinct group %s from %s\n", groupName, afx.intName);

						if (hasGroup) {
							// errOut.printf("\tTRACE: Counted actual group %s from %s\n", groupName, afx.intName);

							groupContents.put(groupName, new ArrayList<>());
						} else {
							nonGroupContents.add(afx.intName);
						}
					} else if (hasGroup) {
						groupContents.get(groupName).add(afx.intName);
					} else {
						nonGroupContents.add(afx.intName);
					}
				}

				if (afx.weight == 0) zeroCount += 1;

				if (afx.weight == 0 && !listZeros) {
					if (!omitZeros)
						normOut.printf("\nAffix %s has zero spawn weight\n", afx.intName);
				} else {
					boolean isNamed = (afx.affixSuffix != null) || (afx.affixPrefix != null);

					if (isNamed) namedCount   += 1;
					else         unnamedCount += 1;

					if (nameMode == NameMode.UNNAMED && isNamed) continue;
					if (nameMode == NameMode.NAMED   && !isNamed) continue;

					normOut.printf("\n%s\n", afx.toString());
				}
			} catch (Exception ex) {
				errOut.printf("Something bad happened for file %s:%s\n", fName, ex.getMessage());

				ex.printStackTrace(errOut);

				errOut.println();
			}
		}
			normOut.printf("\nFile Group '%s' ending\n", fGroup.getKey());
		}

		errOut.println("\nGroup Contents: ");
		for (Entry<String, List<String>> ent: groupContents.entrySet()) {
			errOut.printf("\t%s: %s\n", ent.getKey(), ent.getValue());
		}
		errOut.println();
		errOut.println();

		long endTime = System.nanoTime();
		errOut.printf("\nProcessed %,d affixes (%,d named, %,d unnamed, %,d zero-weight) (%,d effects) (%,d distinct groups, %,d actual groups, %,d nongrouped affixes) out of %,d files (%,d groups) in %,d nanoseconds (%.2f seconds)\n", fCount, namedCount, unnamedCount, zeroCount, effectCount, groupCount, groupContents.size(), nonGroupContents.size(), fCount, fGroups.size(), endTime - startTime, ((double)(endTime - startTime) / 1000000000));
		errOut.printf("\tOptions: Name Mode: %s, Special-case zero weight: %s, Noting zero-weight in special case: %s\n", nameMode, !listZeros, !omitZeros);
	}

	// Process an affix file
	private static Affix processFile(Scanner scn, String fName) {
		Affix afx = new Affix();

		long startTime = System.nanoTime();

		while (scn.hasNextLine())  {
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
						errOut.printf("Misformed affix translation: (%s) (%s) (%s)\n", splits[0], splits[1], fName);
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
					errOut.printf("Malformed equip type: (%s) (%s)\n", splits[0], fName);
				afx.addEquipType(splits[1]);
			} else if (splits[0].equals("<STRING>NAME")) {
				if (splits.length == 1)
					errOut.printf("Malformed name: (%s) (%s)\n", splits[0], fName);
				afx.intName = splits[1];
			} else if (ln.contains("[EFFECT]")) {
				afx.effects.add(parseEffect(afx, scn, fName));
			}
		}

		long endTime = System.nanoTime();
		if (doTiming) errOut.printf("\tProcessed affix %s from %s in %d nanoseconds (%.2f seconds)\n\n", afx.intName, fName, endTime - startTime, ((double)(endTime - startTime) / 1000000000));

		return afx;
	}

	private static Effect parseEffect(Affix afx, Scanner scn, String fName) {
		Effect efct = new Effect();

		long startTime = System.nanoTime();

		efct.fName = fName;

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
						errOut.printf("Malformed activation type: (%s) (%s) (%s)\n", splits[1], efct.name, afx.intName);
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

					errOut.printf("WARN: Punting on DURATION:PERCENT for %s\n", fName);
				} else if (splits[1].equals("0")) {
					efct.hasDuration = false;
					efct.duration = 0.0;
				} else {
					efct.hasDuration = true;

					efct.duration = Double.parseDouble(splits[1]);
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
		if (doTiming) errOut.printf("\t\tProcessed effect %s from %s in %d nanoseconds (%.2f seconds)\n", efct.name, fName, endTime - startTime, ((double)((endTime - startTime) / 1000000000)));

		effectCount += 1;

		return efct;
	}
}
