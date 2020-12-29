package tlIItools;

import java.util.*;

/** Represents an effect attached to an affix.
 *
 * @author Ben Culkin */
public class Effect {
	/** Count of all loaded effects. */
	public static int effectCount = 0;
	/** Do timing analysis when loading effects. */
	public static boolean doTiming;

	/** The file name this effect came from. */
	public String fName;
	/** Duration of the effect. */
	public double duration;

	/** Minimum value for the effect. */
	public double minValue;
	/** Maximum value for the effect. */
	public double maxValue;

	/** The percent of the stat value to apply. */
	public double statPercent;
	/** The amount the targets armor is reduced by for this effect. */
	public double soakScale = 1.0;

	/** Level of the effect. */
	public int level = -1;

	/** The 'effect group' this effect belongs to.
	 * 
	 * An 'effect group is essentially any other effect that is the same general sort of effect, just with different details.
     * 
     * For instance, an effect that grants +4 strength would group with one granting +8 strength,
     * assuming that most other details were equal. */
	public EffectGroup group = new EffectGroup();
	
    /** The amount to resist/do knockback by. */
	public double resist;

	/** Minimum value per monster. */
	public double minPer;

	/** Maximum value per monster. */
	public double maxPer;

	/** Range for effect. */
	public double range;

	/** Maximum count of monsters. */
	public double maxCount;

	/** The rate at which the effect fires. */
	public double pulse;

	/** Create a new blank effect. */
	public Effect() {

	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (group.isTransfer) {
			sb.append("Inflict on Hit: ");
		}

		Map<String, String> detMap = group.hasDuration ? EffectRepo.timeDetals : EffectRepo.detals;

		if (detMap.containsKey(group.type) ||
			(group.hasDuration 
			 && !EffectRepo.timeDetals.containsKey(group.type) 
			 && EffectRepo.detals.containsKey(group.type))) 
		{
			String fmt;
			if (group.hasDuration 
				&& !EffectRepo.timeDetals.containsKey(group.type) 
				&& EffectRepo.detals.containsKey(group.type)) 
			{
				AffixLister.errOut.printf("Improvised details for timed %s\n", group.type);
				fmt = EffectRepo.detals.get(group.type) + "for <DUR> seconds";
			} else {
				fmt = detMap.get(group.type);
			}

			// Expand aliases first.

			for (ReplPair repl : EffectRepo.replList) fmt = fmt.replaceAll(repl.find, repl.replace);
			
			if (minValue <= 0 && maxValue <= 0) { fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$1"); }
			if (minValue >= 0 && maxValue >= 0) { fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$2"); }
			if (minPer <= 0 && maxPer <= 0) { fmt = fmt.replaceAll("<MC\\|(\\w+)\\|(\\w+)>", "$1"); }
			if (minPer >= 0 && maxPer >= 0) { fmt = fmt.replaceAll("<MC\\|([^|>]+)\\|([^|>]+)>", "$2"); }
			
			if (fmt.contains("<") || fmt.contains(">")) {
				AffixLister.errOut.printf("WARN: Details for effect %s are malformatted (contains < or >):\n\t%s\n", group.type, fmt);
			}

			sb.append(String.format(fmt,
					Math.abs(minValue), Math.abs(maxValue),
					duration, group.damageType.toLowerCase(), level, resist, group.name,
					Math.abs(minPer), Math.abs(maxPer), range, maxCount, pulse));
		} else {
			sb.append("No effect details for effect ");
			sb.append(group.type);
			sb.append(String.format(
					" with parameters (min %.2f, max %.2f, dur %.2f, type %s, level %d)",
					minValue, maxValue, duration, group.damageType.toLowerCase(), level, group.name));

			if (AffixLister.addFileName) {
				sb.append(" from file ");
				sb.append(fName);
			}

			if (group.hasDuration) AffixLister.errOut.print("TIMED: ");
			AffixLister.errOut.println(sb.toString());
		}

		if (group.name != null) {
			sb.append(" (named ");
			sb.append(group.name);
			sb.append(")");
		}

		if (group.exclusive) sb.append(" (Exclusive)");

		if (group.graphOverride != null) {
			sb.append(" (Uses ");
			sb.append(group.graphOverride);
			sb.append(" graph)");
		}

		if (group.ownerLevel) {
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

		if (group.statName != null) {
			sb.append(String.format(" (%.2f of stat %s", statPercent, group.statName));

			if (group.isStatBonus) sb.append(" as bonus)");
			else             sb.append(")");
		}

		if (!group.useGraph) sb.append(" (Ignoring graph)");

		return sb.toString();
	}

	@Override
    public int hashCode() {
        return Objects.hash(duration, fName, group, level, maxCount, maxPer, maxValue,
                minPer, minValue, pulse, range, resist, soakScale, statPercent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Effect other = (Effect) obj;
        return Double.doubleToLongBits(duration)
                == Double.doubleToLongBits(other.duration)
                && Objects.equals(fName, other.fName)
                && Objects.equals(group, other.group) && level == other.level
                && Double.doubleToLongBits(maxCount)
                        == Double.doubleToLongBits(other.maxCount)
                && Double.doubleToLongBits(maxPer)
                        == Double.doubleToLongBits(other.maxPer)
                && Double.doubleToLongBits(maxValue)
                        == Double.doubleToLongBits(other.maxValue)
                && Double.doubleToLongBits(minPer)
                        == Double.doubleToLongBits(other.minPer)
                && Double.doubleToLongBits(minValue)
                        == Double.doubleToLongBits(other.minValue)
                && Double.doubleToLongBits(pulse) == Double.doubleToLongBits(other.pulse)
                && Double.doubleToLongBits(range) == Double.doubleToLongBits(other.range)
                && Double.doubleToLongBits(resist)
                        == Double.doubleToLongBits(other.resist)
                && Double.doubleToLongBits(soakScale)
                        == Double.doubleToLongBits(other.soakScale)
                && Double.doubleToLongBits(statPercent)
                        == Double.doubleToLongBits(other.statPercent);
    }

    /** Parse an effect.
	 *
	 * @param afx The affix the effect belongs to.
	 * @param scn The scanner to read from.
	 * @param scnSource The name of the scanner.
	 *
	 * @return An effect, read from the scanner.
	 */
	public static Effect parseEffect(Affix afx, Scanner scn, String scnSource) {
		return parseEffect(afx, scn, scnSource, new ArrayList<>());
	}

	/** Parse an effect.
	 *
	 * @param afx The affix the effect belongs to.
	 * @param scn The scanner to read from.
	 * @param scnSource The name of the scanner.
	 * @param errs Repository for errors found while parsing.
	 *
	 * @return An effect, read from the scanner. */
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
				efct.group.name = splits[1];
			} else if (ln.contains("DAMAGE_TYPE")) {
				efct.group.damageType = splits[1];
			} else if (ln.contains("TYPE")) {
				efct.group.type = splits[1];
			} else if (ln.contains("ACTIVATION")) {
				switch (splits[1]) {
					case "DYNAMIC":
					case "PASSIVE":
						// Passive is the default, and
						// dynamic doesn't have much
						// actual difference.
						break;
					case "TRANSFER":
						efct.group.isTransfer = true;
						break;
					default:
						errs.add(String.format("Malformed activation type: (%s) (%s) (%s)\n", splits[1], efct.group.name, afx.intName));
				}
			} else if (ln.contains("DURATION")) {
				if (splits[1].equals("ALWAYS")) {
					efct.group.hasDuration = false;

					efct.duration = Double.POSITIVE_INFINITY;
				} else if (splits[1].equals("INSTANT")) {
					efct.group.hasDuration = false;

					efct.duration = Double.NaN;
				} else if (splits[1].equals("PERCENT")) {
					efct.group.hasDuration = false;

					efct.duration = Double.NaN;

					errs.add(String.format("WARN: Punting on DURATION:PERCENT for %s\n", scnSource));
				} else if (splits[1].equals("0")) {
					efct.group.hasDuration = false;
					efct.duration = 0.0;
				} else {
					efct.group.hasDuration = true;

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
				efct.group.exclusive = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("GRAPHOVERRIDE")) {
				efct.group.graphOverride = splits[1];
			} else if (ln.contains("USEOWNERLEVEL")) {
				efct.group.ownerLevel = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("NOGRAPH")) {
				efct.group.useGraph = Boolean.parseBoolean(splits[1]);
			} else if (ln.contains("STATNAME")) {
				efct.group.statName = splits[1];
			} else if (ln.contains("STATPERCENT")) {
				efct.statPercent = Double.parseDouble(splits[1]);
			} else if (ln.contains("STATMODIFIERISBONUS")) {
				efct.group.isStatBonus = Boolean.parseBoolean(splits[1]);
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

			double seconds = (((endTime - startTime) / 1000000000));
			errs.add(String.format(fmt, efct.group.name, scnSource, endTime - startTime, seconds));
		}

		effectCount += 1;

		return efct;
	}
}
