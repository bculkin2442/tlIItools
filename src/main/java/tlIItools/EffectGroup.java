package tlIItools;

import java.util.*;

/** An 'effect group'.
 * 
 * This groups similar effects together, the same way affix groups group affixs
 * together.
 * 
 * @author Ben Culkin */
public class EffectGroup {
    /** The name of the effect. */
    public String name;
    /** The specific effect that happens. */
    public String type;
    /** Damage type for the effect, if applicable. */
    public String damageType = "physical";
    /** Whether or not we have a duration or not. */
    public boolean hasDuration;
    /** The name of the stat that applies to this affect. */
    public String statName;
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
    /** Whether or not this effect is a 'transfer' effect (Applied to the enemy on a hit). */
    public boolean isTransfer;
    
    /**
     * Retrieve a summary of this effect group.
     * 
     * @return A summary of the effect group
     */
	public String summary() {
		StringBuilder sb = new StringBuilder();
		
		if (isTransfer) sb.append("Transfer: ");
		sb.append(type);
		if (name != null && !name.equals("")) {
			sb.append(" (named ");
			sb.append(name);
			sb.append(")");
		}

		if (hasDuration) sb.append(" (timed)");
		if (statName != null && !name.equals("")) {
			sb.append(" (uses stat ");
			sb.append(statName);
			if (isStatBonus) sb.append(" as a bonus");
			sb.append(")");
		}

		if (ownerLevel) sb.append(" (uses owner level)");
		if (useGraph) {
			sb.append(" (uses ");
			if (graphOverride != null && !graphOverride.equals("")) sb.append(graphOverride);
			else sb.append("default");
			sb.append(" graph)");
		}

		if (exclusive) sb.append(" (exclusive)");

		// @TODO Ben Culkin 12/31/2020 :FancyEffectSummary
		//
		// EffectGroups should probably use something from EffectRepo to better
		// output the summary for a particular group type
		// String fmt = "%s (name %s, damageType %s, hasDuration %s, stat %s, isBonus %s, ownerLevel %s, useGraph %s, graphOverride %s, exclusive %s, isTransfer %s)";
	
		// sb.append(String.format(fmt, type, name, damageType, hasDuration, statName, isStatBonus, ownerLevel, useGraph, graphOverride, exclusive, isTransfer));

		return sb.toString();
	}

    @Override
    public String toString() {
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
    public int hashCode() {
        return Objects.hash(damageType, exclusive, graphOverride, hasDuration,
                isStatBonus, isTransfer, name, ownerLevel, statName, type, useGraph);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)                  return true;
        if (obj == null)                  return false;
        if (getClass() != obj.getClass()) return false;
        
        EffectGroup other = (EffectGroup) obj;
        
        return Objects.equals(damageType, other.damageType)
                && Objects.equals(graphOverride, other.graphOverride)
                && Objects.equals(name, other.name)
                && Objects.equals(statName, other.statName)
                && Objects.equals(type, other.type)
                && exclusive == other.exclusive
                && hasDuration == other.hasDuration
                && isStatBonus == other.isStatBonus
                && isTransfer == other.isTransfer
                && ownerLevel == other.ownerLevel
                && useGraph == other.useGraph;
    }
}
