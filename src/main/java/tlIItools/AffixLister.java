package tlIItools;

import java.io.*;

import java.util.*;
import java.util.Map.Entry;

/** Lists randomly generated affixes for Torchlight II gear.
 *
 * @author Ben Culkin */
public class AffixLister {
	/* Count of effects this class has loaded. */
	private static int effectCount = 0;

	/** Should the class record timing data? */
	public static boolean doTiming = false;

	/** Should the file name be attached to things? */
	public static boolean addFileName = false;

	/* Output streams to use. */

	/** The normal output to use. */
	public static PrintStream normOut = System.out;
	/** The error output to use. */
	public static PrintStream errOut = System.err;
	/** The output to write affix groups to. */
	public static PrintStream affixGroupDest = null;

	/** Indicates how to treat affixes with regards to their names. */
	public static enum NameMode {
		/** Show all affixes. */
		ALL,
		/** Show only unnamed affixes. */
		UNNAMED,
		/** Show only named affixes. */
		NAMED;
	}

	/** Main method.
	 *
	 * @param args The names of the files to read affix data from. */
	public static void main(String[] args) {
		listAffixes(args);
	}

	/** Main method that actually does stuff.
	 *
	 * @param args The names of the files to read affix data from.
	 * 
	 * @return The listed affix set. */
	@SuppressWarnings("unused")
	public static AffixSet listAffixes(String[] args) {
	    boolean doingArgs = true;

		boolean omitZeros = false;
		boolean listZeros = false;

		NameMode nameMode = NameMode.ALL;

		int namedCount   = 0;
		int unnamedCount = 0;
		int zeroCount    = 0;
		int groupCount   = 0;

		boolean outputAffixGroups   = false;

		Map<String, Set<Affix>> groupContents    = new HashMap<>();
		Set<Affix>              nonGroupContents = new HashSet<>();

		NameFileReader nfr = new NameFileReader(false);
		nfr.groupRx = ".*/mods/([^/]+)/*";

		long startTime = System.nanoTime();

		for (int i = 0; i < args.length; i++) {
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
					Effect.doTiming = true;
					break;
				case "--no-timing":
				case "-T":
					doTiming = false;
					Effect.doTiming = false;
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

					nfr.swapGroup(args[++i]);
					break;
				case "--guess-groups":
					nfr.guessGroups = true;
					break;
				case "--no-guess-groups":
					nfr.guessGroups = false;
					break;
				case "--read-names-from-file":
				case "-r":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: read name file argument requires the file to use be specified\n");
						break;
					}

					nfr.readFrom(args[++i]);
					break;
				case "--output":
				case "-o":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: output file argument requires the file to use be specified\n");
						break;
					}

					try {
						normOut = new PrintStream(args[++i]);
						nfr.normOut = normOut;
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
						nfr.errOut = errOut;
					} catch (IOException ioex) {
						errOut.printf("Could not open error output file %s\n", args[i]);

						ioex.printStackTrace(errOut);
					}
					break;
				case "--guess-regex":
					if (i + 1 >= args.length) {
						errOut.printf("ERROR: group regex argument requires the regex to use be specified\n");
						break;
					}

					nfr.groupRx = args[++i];
					break;
				case "--output-affix-groups":
					if (i + 1 >= args.length) {
						errOut.printf(
								"ERROR: to output affix-groups, must specify a file to output them to");
						break;
					}

					try {
						affixGroupDest = new PrintStream(args[++i]);
						outputAffixGroups = true;
					} catch (IOException ioex) {
						errOut.printf(
								"ERROR: Couldn't open file %s to write affix groups to\n",
								affixGroupDest);
					}
					break;
				default:
					isArg = false;
				}

				if (isArg) continue;
				else       nfr.addFile(fName);
			} else {
				nfr.addFile(fName);
			}
		}

		AffixSet affixSetByContents = new AffixSet();

		for (Entry<String, List<String>> fGroup : nfr.fNames.entrySet()) {
			if (fGroup.getValue().size() == 0) continue;

			normOut.printf("\nFile Group '%s' starting\n", fGroup.getKey());
			for (String fName : fGroup.getValue()) {
				try (FileReader fr = new FileReader(fName)) {
					Scanner sc = new Scanner(fr);

					Affix afx = Affix.loadAffix(sc, fName);
					affixSetByContents.addAffixByContents(afx);

					effectCount += afx.effects.size();

					if (afx.intName != null && afx.weight != 0) {
						String groupRx = "(.*_?)\\d+\\Z";
						boolean hasGroup = afx.intName.matches(groupRx);
						String groupName = afx.intName.replaceAll(groupRx, "$1");

						if (!groupContents.containsKey(groupName)) {
							groupCount += 1;
							// errOut.printf("\tTRACE: Counted distinct group %s from
							// %s\n", groupName, afx.intName);

							if (hasGroup) {
								// errOut.printf("\tTRACE: Counted actual group %s from
								// %s\n", groupName, afx.intName);

								groupContents.put(groupName, new HashSet<>());
							} else {
								nonGroupContents.add(afx);
							}
						} else if (hasGroup) {
							groupContents.get(groupName).add(afx);
						} else {
							nonGroupContents.add(afx);
						}
					}

					if (afx.weight == 0) zeroCount += 1;

					if (afx.weight == 0 && !listZeros) {
						if (!omitZeros) normOut.printf(
								"\nAffix %s has zero spawn weight\n",
								afx.intName);
					} else {
						boolean isNamed = 
							(afx.affixSuffix != null) || (afx.affixPrefix != null);

						if (isNamed) namedCount += 1;
						else         unnamedCount += 1;

						if (nameMode == NameMode.UNNAMED && isNamed)  continue;
						if (nameMode == NameMode.NAMED   && !isNamed) continue;

						normOut.printf("\n%s\n", afx.toLongString());
					}
				} catch (Exception ex) {
					errOut.printf(
						"Something bad happened for file %s:%s\n",
						fName, ex.getMessage());

					ex.printStackTrace(errOut);
					errOut.println();
				}
			}

			normOut.printf("\nFile Group '%s' ending\n", fGroup.getKey());
		}

		errOut.println("\nGroup Contents: ");

		for (Entry<String, Set<Affix>> ent : groupContents.entrySet()) {
			errOut.printf("\t%s: %s\n", ent.getKey(), ent.getValue());
		}

		errOut.println();
		errOut.println();

		if (outputAffixGroups) {
		    for (Entry<AffixGroup, Set<Affix>> entry 
		            : affixSetByContents.affixGroups.entrySet())
		    {
		        AffixGroup group   = entry.getKey();
			Set<Affix> affixes = entry.getValue();

			// Skip one-affix groups
			if (affixes.size() == 1) continue;

			List<Affix> affixList = new ArrayList<>(affixes);
			Collections.sort(affixList, (lhs, rhs) -> lhs.spawnRange.compareTo(rhs.spawnRange));

			boolean isFirstAfx = true;
			for (Affix afx : affixList) {
				// @TODO actually implement this -bculkin, 12/29/2020

				// Print the header for this group
				if (isFirstAfx) {
					isFirstAfx = false;
					affixGroupDest.printf("Group ID %s (%d affixes)\t",
						group.hashCode(), affixes.size());
					affixGroupDest.print(group.groupSummary());
				}

				affixGroupDest.print("\t");
				affixGroupDest.print(afx.toShortString().replaceAll("\n(?=.)", "\n\t"));
				// print this affix in the group format
			}
			affixGroupDest.println();
		    }
		}

		long endTime = System.nanoTime();
		errOut.printf(
			"\nProcessed %,d affixes (%,d named, %,d unnamed, %,d zero-weight) (%,d effects) (%,d distinct groups, %,d actual groups, %,d nongrouped affixes) out of %,d files (%,d groups) in %,d nanoseconds (%.2f seconds)\n",
			nfr.fCount, namedCount, unnamedCount, zeroCount, effectCount, groupCount,
			groupContents.size(), nonGroupContents.size(), nfr.fCount,
			nfr.fNames.size(), endTime - startTime,
			((double) (endTime - startTime) / 1000000000));

		errOut.printf(
			"\tOptions: Name Mode: %s, Special-case zero weight: %s, Noting zero-weight in special case: %s\n",
			nameMode, !listZeros, !omitZeros);

		return affixSetByContents;
	}
}
