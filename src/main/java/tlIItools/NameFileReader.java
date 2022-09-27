package tlIItools;

import java.io.*;

import java.util.*;

/** Reads in a list of file names to process.
 *
 * @author Ben Culkin */
public class NameFileReader {
	/** Are we attempting to guess group names? */
	public boolean guessGroups;
	/** Regex to use for guessing group names. */
	public String groupRx;
	/** The default group to put files into. */
	public String defGroup;
	/** The map of file groups. */
	public Map<String, List<String>> fNames;

	/* The current group. */
	private String curGroup;
	/* The list of files for the current group. */
	private	List<String> curList;

	/** Counts the files read in. */
	public int fCount;

	/** Stream to write normal output to. */
	public PrintStream normOut = System.out;
	/** Stream to write error output to. */
	public PrintStream errOut  = System.err;

	/** Create a new name reader using the default settings.
	 *
	 * Guessing groups is disabled by default. */
	public NameFileReader() {
		this(false);
	}

	/** Create a new name reader using the default settings.
	 *
	 * @param guessGroups Controls whether or not to try to guess file groups from file names. */
	public NameFileReader(boolean guessGroups) {
		this(new HashMap<>(), "default", guessGroups);
	}

	/** Create a new name reader using the specified settings.
	 *
	 * @param fNames The set of groups to add files to.
	 * @param defGroup The name of the default file group.
	 * @param guessGroups Whether or not to attempt to guess group names. */
	public NameFileReader(Map<String, List<String>> fNames, String defGroup, boolean guessGroups) {
		if (!fNames.containsKey(defGroup)) fNames.put(defGroup, new ArrayList<>());

		this.fNames = fNames;

		this.defGroup    = defGroup;
		this.guessGroups = guessGroups;
		this.curGroup    = defGroup;

		this.curList     = fNames.get(curGroup);

		this.fCount = 0;
	}

	/** Read in file names from a file.
	 *
	 * @param from The name of the file to read from. */
	public void readFrom(String from) {
		try (FileReader fr = new FileReader(from)) {
			readFrom(fr);
		} catch (IOException ioex) {
			errOut.printf("Error reading names from file %s\n", from);
			ioex.printStackTrace(errOut);
			errOut.println();
		}
	}

	/** Read in file names from an input source.
	 *
	 * @param r The input source to read from. */
	public void readFrom(Reader r) {
		// the caller will take care of it
		@SuppressWarnings("resource")
		Scanner scn = new Scanner(r);

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();

			boolean skipAdd = false;

			if (ln.startsWith("#")) {
				swapGroup(ln.substring(1));

				skipAdd = true;
			} else if (guessGroups && ln.contains("/mods/")) {
				swapGroup(ln.replaceAll(groupRx, "$1"));
			}

			if (!skipAdd) {
				fCount += 1;

				curList.add(ln);
			}

			skipAdd = false;
		}
		
		scn.close();
	}

	/** Swap to a new file group.
	 *
	 * @param groupName The name of the group to swap to. */
	public void swapGroup(String groupName) {
		curGroup = groupName;

		if (!fNames.containsKey(curGroup)) {
			curList = new ArrayList<>();

			fNames.put(curGroup, curList);
		} else {
			curList  = fNames.get(curGroup);
		}
	}
	
	/** Add a file to this file reader.
	 * 
	 * @param fName The file to add. */
	public void addFile(String fName) {
		curList.add(fName);

		fCount += 1;
	}
	
	/** Add a file to this file reader.
	 * 
	 * @param groupName The group to add the file to.
	 * @param fName The file to add. */
	public void addFile(String groupName, String fName) {
		fNames.computeIfAbsent(groupName, (key) -> new ArrayList<>()).add(fName);

		fCount += 1;
	}
}
