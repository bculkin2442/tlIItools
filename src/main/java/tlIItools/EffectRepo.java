package tlIItools;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

/**
 * Repository class for storing information needed for parsing/outputing
 * effects.
 * 
 * @author Ben Culkin
 *
 */
public class EffectRepo {
	// NOTE: consider making these use function accessors in the future?
	// --bculkin, 6/24/20
	/**
	 * The list of detail strings for skills.
	 */
	public static Map<String, String> detals;
	/**
	 * The list of detail strings for timed skills.
	 */
	public static Map<String, String> timeDetals;

	/**
	 * The list of replacements for detail strings.
	 */
	public static List<ReplPair> replList;

	/*
	 * Init. lists from files.
	 */
	static {
		try (FileReader detalReader = new FileReader("data/affix-detals.txt")) {
			detals = readDetails(new Scanner(detalReader));
		} catch (IOException ioex) {
			AffixLister.errOut.println("Error loading affix detail text");
		}

		try (FileReader timedDetalReader
				= new FileReader("data/timed-affix-detals.txt")) {
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
	 *            The source to read from.
	 * @return The map of effect details to use.
	 */
	public static Map<String, String> readDetails(Scanner scn) {
		Map<String, String> detalMap = new HashMap<>();

		return readDetails(detalMap, scn);
	}

	/**
	 * Read effect detail strings from an input source, adding to an existing set.
	 *
	 * @param detalMap
	 *                 The details to add to.
	 * @param scn
	 *                 The source to read from.
	 * @return The map of effect details to use.
	 */
	public static Map<String, String> readDetails(Map<String, String> detalMap,
			Scanner scn) {
		while (scn.hasNextLine()) {
			String name = scn.nextLine().trim();
			if (name.equals(""))
				continue;
			if (name.startsWith("#"))
				continue;

			String body;
			do {
				body = scn.nextLine().trim();
			} while (body.startsWith("#"));

			detalMap.put(name, body);
		}

		return detalMap;
	}

	/**
	 * Sanity check the loaded format strings.
	 */
	public static void sanityCheckFormats() {
		for (Entry<String, String> detal : detals.entrySet()) {
			String fmt = detal.getValue();

			AffixLister.errOut.printf("\tTRACE: Applying replacements for %s\n",
					detal.getKey());
			for (ReplPair repl : replList) {
				String tmp = fmt;
				fmt = fmt.replaceAll(repl.find, repl.replace);
				if (!fmt.equals(tmp)) {
					String outFmt = "\t\tTRACE: Replaced %s with %s: \n\t\t%s\n\t\t%s\n";

					AffixLister.errOut.printf(outFmt, repl.find, repl.replace, tmp, fmt);
				}
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				String warnFmt
						= "WARN: Details for effect %s are malformated (contains < or >):\n\t%s\n";

				AffixLister.errOut.printf(warnFmt, detal.getKey(), fmt);
			}
		}

		for (Entry<String, String> detal : timeDetals.entrySet()) {
			String fmt = detal.getValue();

			for (ReplPair repl : replList) {
				fmt = fmt.replaceAll(repl.find, repl.replace);
			}

			if (fmt.contains("<") || fmt.contains(">")) {
				String warnFmt
						= "WARN: Details for timed effect %s are malformatted (contains < or >):\n\t%s\n";
				AffixLister.errOut.printf(warnFmt, detal.getKey(), fmt);
			}
		}
	}
}
