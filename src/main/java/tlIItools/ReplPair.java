package tlIItools;

import java.util.*;

/** String pairs for replacements.
 *
 * @author Ben Culkin */
public class ReplPair implements Comparable<ReplPair> {
	/** Represents an error encountered parsing ReplPairs
	 *
	 * @author Ben Culkin */
	public static class ReplError {
		/** The line the error occured on. */
		public int line;
		/** The number of pairs we have processed so far. */
		public int numPairs;

		/** The text of the line we errored on. */
		public String txt;
		/** The message of the error. */
		public String msg;

		/** Create a new ReplPair parse error.
		 *
		 * @param lne The line the error occured on.
		 * @param nPairs The number of pairs processed up to this point.
		 * @param msg The message detailing the error.
		 * @param txt The text that caused the error. */
		public ReplError(int lne, int nPairs, String msg, String txt) {
			line = lne;
			numPairs = nPairs;

			this.txt = txt;
			this.msg = msg;
		}

		@Override
		public String toString() {
			String errString;
			if      (txt == null)    errString = "No associated line";
			else if (txt.equals("")) errString = "Text of line was empty";
			else                     errString = "Text of line was: " + txt;

			return String.format("line %d, pair %d:%s\n\t%s", line, numPairs, msg, errString);
		}
	}

	/** The priority for this replacement. */
	public int priority;
	/** The name of this replacement.
	 *
	 * Defaults to the 'find' string. */
	public String name;
	/** The string to look for. */
	public String find;
	/** The string to replace it with. */
	public String replace;

	/** Create a new blank replacement pair. */
	public ReplPair() {
		this("", "", 1, null);
	}

	/** Create a new replacement pair with a priority of 1.
	 *
	 * @param f The string to find.
	 * @param r The string to replace.
	 */
	public ReplPair(String f, String r) {
		this(f, r, 1);
	}

	/** Create a new named replacement pair with a priority of 1.
	 *
	 * @param f The string to find.
	 * @param r The string to replace.
	 * @param n The name of the replacement pair.
	 */
	public ReplPair(String f, String r, String n) {
		this(f, r, 1, n);
	}

	/** Create a new replacement pair with a set priority.
	 *
	 * @param f The string to find.
	 * @param r The string to replace.
	 * @param p The priority for the replacement. */
	public ReplPair(String f, String r, int p) {
		this(f, r, p, f);
	}

	/** Create a new replacement pair with a set priority and name.
	 *
	 * @param f The string to find.
	 * @param r The string to replace.
	 * @param n The name of the replacement pair.
	 * @param p The priority for the replacement. */
	public ReplPair(String f, String r, int p, String n) {
		find    = f;
		replace = r;

		name = n;

		priority = p;
	}

	/** Read a list of replacement pairs from an input source.
	 *
	 * @param scn The source to read the replacements from.
	 *
	 * @return The list of replacements. */
	public static List<ReplPair> readList(Scanner scn) {
		return ReplPair.readList(new ArrayList<>(), scn);
	}

	/** Read a list of replacement pairs from an input source, adding them to
	 * an existing list.
	 *
	 * @param detals The list to add the replacements to.
	 * @param scn The source to read the replacements from.
	 *
	 * @return The list of replacements. */
	public static List<ReplPair> readList(List<ReplPair> detals, Scanner scn) {
		List<ReplError> errList = new ArrayList<>();

		List<ReplPair> rplPar = readList(detals, scn, errList);

		if (errList.size() != 0) {
			String errString;
			if (errList.size() == 0) errString = "An error";
			else                       errString = "Errors";

			throw new IllegalArgumentException(String.format(
					"%s occured parsing replacement pairs:\n%s",
					errString, errList));
		}

		return rplPar;
	}

	/** Read a list of replacement pairs from an input source, adding them to
	 * an existing list.
	 *
	 * @param detals The list to add the replacements to.
	 * @param scn The source to read the replacements from.
	 * @param errs The list to stick errors in.
	 *
	 * @return The list of replacements. */
	public static List<ReplPair> readList(List<ReplPair> detals, Scanner scn, List<ReplError> errs) {
		int lno = 0;
		int pno = 0;

		int defPrior = 1;

		List<ReplPair> resList = new ArrayList<>();
		
		// For every line in the source...
		while (scn.hasNextLine()) {
			String name = scn.nextLine().trim();
			lno += 1;

			// If its commented or blank, skip it
			if (name.equals(""))      continue;
			if (name.startsWith("#")) continue;

			ReplPair rp = new ReplPair();
			rp.priority = defPrior;

			rp.find = name;
			if (rp.name == null) rp.name = name;

			// We started to process the pair, mark it as being
			// started
			pno += 1;
			String body = null;

			// Read in the next uncommented line
			do {
				if (!scn.hasNextLine()) {
					String msg = "Ran out of input looking for replacement body for raw name " + name;
						
					errs.add(new ReplError(lno, pno, msg, null));
					break;
				}

				body = scn.nextLine().trim();
				lno += 1;
			} while (body.startsWith("#"));

			rp.replace = body;

			resList.add(rp);
		}

		return resList;
	}

	@Override
	public String toString() {
		return String.format("s/%s/%s/p%d", find, replace, priority);
	}
	
	@Override
	public int compareTo(ReplPair rp) {
		return this.priority - rp.priority;
	}
}
