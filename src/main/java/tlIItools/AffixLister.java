package tlIItools;

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

	/**
	 * Should the file name be attached to things?
	 */
	public static boolean addFileName = false;

	public static PrintStream normOut = System.out;
	public static PrintStream errOut = System.err;

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
