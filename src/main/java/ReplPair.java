import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * String pairs for replacements.
 *
 * @author Ben Culkin
 */
public class ReplPair {
	/**
	 * The string to look for.
	 */
	public String find;
	/**
	 * The string to replace it with.
	 */
	public String replace;

	/**
	 * Create a new blank replacement pair.
	 */
	public ReplPair() {

	}

	/**
	 * Create a new replacement pair.
	 *
	 * @param f
	 * 	The string to find.
	 * @param r
	 * 	The string to replace.
	 */
	public ReplPair(String f, String r) {
		find = f;
		replace = r;
	}

	/**
	 * Read a list of replacement pairs from an input source.
	 *
	 * @param scn
	 * 	The source to read the replacements from.
	 */
	public static List<ReplPair> readList(Scanner scn) {
		return ReplPair.readList(new ArrayList<>(), scn);
	}

	/**
	 * Read a list of replacement pairs from an input source, adding them to
	 * an existing list.
	 *
	 * @param detals
	 * 	The list to add the replacements to.
	 * @param scn
	 * 	The source to read the replacements from.
	 */
	public static List<ReplPair> readList(List<ReplPair> detals, Scanner scn) {
		while (scn.hasNextLine()) {
			String name = scn.nextLine().trim();
			if (name.equals("")) continue;
			if (name.startsWith("#")) continue;

			String body;
			do {
				body = scn.nextLine().trim();
			} while (body.startsWith("#"));

			detals.add(new ReplPair(name, body));
		}

		return detals;
	}
}
