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
	/** The name of the effect. */
	public String name;

	/** The specific effect that happens. */
	public String type;

	/** Damage type for the effect, if applicable. */
	public String damageType = "physical";

	/** Duration of the effect. */
	public double duration;

	/** Whether or not we have a duration or not. */
	public boolean hasDuration;

	/** Minimum value for the effect. */
	public double minValue;
	/** Maximum value for the effect. */
	public double maxValue;

	/** The name of the stat that applies to this affect. */
	public String statName;
	/** The percent of the stat value to apply. */
	public double statPercent;
	/** Whether or not this stat is a bonus value. */
	public boolean isStatBonus;

	/** Whether or not this uses the owners level to modify any applicable graph. */
	public boolean ownerLevel;

	/** Whether or not a graph is used for this effect. */
	public boolean useGraph = true;
	/** The graph to use instead of the default graph. */
	public String graphOverride;

	/** Whether this effect can stack with itself. */
	public boolean exclusive;

	/** The amount the targets armor is reduced by for this effect. */
	public double soakScale = 1.0;

	/** Level of the effect. */
	public int level = -1;

	/** Whether or not this effect is a 'transfer' effect (Applied to the enemy on a hit). */
	public boolean isTransfer;

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

	/** Gets the 'effect group' this effect belongs to.
	 * 
	 * An 'effect group is essentially any other effect that is the same general sort of effect, just with different details.
	 * 
	 * For instance, an effect that grants +4 strength would group with one granting +8 strength,
	 * assuming that most other details were equal.
	 * 
	 * @return The 'effect group' this effect belongs to. */
	public String getEffectGroup() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(name);
		sb.append(type);
		sb.append(damageType);
		sb.append(hasDuration);
		sb.append(statName);
		sb.append(isStatBonus);
		sb.append(ownerLevel);
		sb.append(graphOverride);
		sb.append(useGraph);
		sb.append(exclusive);
		sb.append(isTransfer);
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isTransfer) {
			sb.append("Inflict on Hit: ");
		}

		Map<String, String> detMap = hasDuration ? EffectRepo.timeDetals : EffectRepo.detals;

		if (detMap.containsKey(type) ||
			(hasDuration 
			 && !EffectRepo.timeDetals.containsKey(type) 
			 && EffectRepo.detals.containsKey(type))) 
		{
			String fmt;
			if (hasDuration 
				&& !EffectRepo.timeDetals.containsKey(type) 
				&& EffectRepo.detals.containsKey(type)) 
			{
				AffixLister.errOut.printf("Improvised details for timed %s\n", type);
				fmt = EffectRepo.detals.get(type) + "for <DUR> seconds";
			} else {
				fmt = detMap.get(type);
			}

			// Expand aliases first.

			for (ReplPair repl : EffectRepo.replList) fmt = fmt.replaceAll(repl.find, repl.replace);
			
			if (minValue <= 0 && maxValue <= 0) { fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$1"); }
			if (minValue >= 0 && maxValue >= 0) { fmt = fmt.replaceAll("<C\\|([^|>]+)\\|([^|>]+)>", "$2"); }
			if (minPer <= 0 && maxPer <= 0) { fmt = fmt.replaceAll("<MC\\|(\\w+)\\|(\\w+)>", "$1"); }
			if (minPer >= 0 && maxPer >= 0) { fmt = fmt.replaceAll("<MC\\|([^|>]+)\\|([^|>]+)>", "$2"); }
			
			if (fmt.contains("<") || fmt.contains(">")) {
				AffixLister.errOut.printf("WARN: Details for effect %s are malformatted (contains < or >):\n\t%s\n", type, fmt);
			}

			sb.append(String.format(fmt,
					Math.abs(minValue), Math.abs(maxValue),
					duration, damageType.toLowerCase(), level, resist, name,
					Math.abs(minPer), Math.abs(maxPer), range, maxCount, pulse));
		} else {
			sb.append("No effect details for effect ");
			sb.append(type);
			sb.append(String.format(
					" with parameters (min %.2f, max %.2f, dur %.2f, type %s, level %d)",
					minValue, maxValue, duration, damageType.toLowerCase(), level, name));

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((damageType == null) ? 0 : damageType.hashCode());
		long temp;
		temp = Double.doubleToLongBits(duration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (exclusive ? 1231 : 1237);
		result = prime * result
				+ ((graphOverride == null) ? 0 : graphOverride.hashCode());
		result = prime * result + (hasDuration ? 1231 : 1237);
		result = prime * result + (isStatBonus ? 1231 : 1237);
		result = prime * result + (isTransfer ? 1231 : 1237);
		result = prime * result + level;
		temp = Double.doubleToLongBits(maxCount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxPer);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minPer);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (ownerLevel ? 1231 : 1237);
		temp = Double.doubleToLongBits(pulse);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(range);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(resist);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(soakScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((statName == null) ? 0 : statName.hashCode());
		temp = Double.doubleToLongBits(statPercent);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (useGraph ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)                  return true;
		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;

		Effect other = (Effect) obj;

		if (damageType == null) {
			if (other.damageType != null) return false;
		} else if (!damageType.equals(other.damageType))
			return false;
		if (Double.doubleToLongBits(duration) != Double.doubleToLongBits(other.duration))
			return false;
		if (exclusive != other.exclusive)
			return false;
		if (graphOverride == null) {
			if (other.graphOverride != null)
				return false;
		} else if (!graphOverride.equals(other.graphOverride))
			return false;
		if (hasDuration != other.hasDuration)
			return false;
		if (isStatBonus != other.isStatBonus)
			return false;
		if (isTransfer != other.isTransfer)
			return false;
		if (level != other.level)
			return false;
		if (Double.doubleToLongBits(maxCount) != Double.doubleToLongBits(other.maxCount))
			return false;
		if (Double.doubleToLongBits(maxPer) != Double.doubleToLongBits(other.maxPer))
			return false;
		if (Double.doubleToLongBits(maxValue) != Double.doubleToLongBits(other.maxValue))
			return false;
		if (Double.doubleToLongBits(minPer) != Double.doubleToLongBits(other.minPer))
			return false;
		if (Double.doubleToLongBits(minValue) != Double.doubleToLongBits(other.minValue))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ownerLevel != other.ownerLevel)
			return false;
		if (Double.doubleToLongBits(pulse) != Double.doubleToLongBits(other.pulse))
			return false;
		if (Double.doubleToLongBits(range) != Double.doubleToLongBits(other.range))
			return false;
		if (Double.doubleToLongBits(resist) != Double.doubleToLongBits(other.resist))
			return false;
		if (Double.doubleToLongBits(soakScale)
				!= Double.doubleToLongBits(other.soakScale))
			return false;
		if (statName == null) {
			if (other.statName != null)
				return false;
		} else if (!statName.equals(other.statName))
			return false;
		if (Double.doubleToLongBits(statPercent)
				!= Double.doubleToLongBits(other.statPercent))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (useGraph != other.useGraph)
			return false;
		return true;
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

			double seconds = (((endTime - startTime) / 1000000000));
			errs.add(String.format(fmt, efct.name, scnSource, endTime - startTime, seconds));
		}

		effectCount += 1;

		return efct;
	}
}
