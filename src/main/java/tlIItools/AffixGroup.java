package tlIItools;

import java.util.*;

import tlIItools.Affix.*;

/**
 * Represents a 'group' of related affixes.
 *
 * These are affixes that are do the same thing, but could be of varying levels;
 * or have effects of differing intensity.
 *
 * @author Ben Culkin */
public class AffixGroup implements Comparable<AffixGroup> {
    public List<EffectGroup> effects;
    /** The types of enchanters who can add this. */
    public List<String> enchantSources;
    /** The types of equipment this can spawn on. */
    public List<String> equipTypes;
    /** The types of equipment this cannot spawn on. */
    public List<String> nonequipTypes;
    /** The types of equipment this can be socketed into on. */
    public List<String> socketableTypes;
    
    /** The type of thing this affix applies to. */
    public AffixType type = AffixType.ITEM;
    
    /** Create a new affix group. */
    public AffixGroup() {
        effects         = new ArrayList<>();
        enchantSources  = new ArrayList<>();
        equipTypes      = new ArrayList<>();
        nonequipTypes   = new ArrayList<>();
        socketableTypes = new ArrayList<>();
    }

    /** Determine whether this affix group contains a particular affix.
     * 
     * By 'affix group', what we mean is that it is essentially the same affix, with
     * generally just differing levels/values.
     * 
     * For instance, an affix that granted +2 strength, and one that granted +4
     * strength would be considered to be in the same affix group (assuming that
     * both of those strength bonuses had the same effect name).
     * 
     * @param afx The affix to check if it is in the affix group with.
     * 
     * @return Whether or the specified affix is in this affix group. */
    public boolean contains(Affix afx) {
        return afx.toAffixGroup().equals(this);
    }
    
	public String groupSummary() {
		StringBuilder sb = new StringBuilder();

		sb.append("Affix Type: ");
		sb.append(type);
		sb.append("\n");
/*		sb.append("Effects: \n");
		for (EffectGroup group : effects) {
			sb.append("\t");
			sb.append(group);
			sb.append("\n");
		}
*/

		sb.append("Affix can spawn on: ");
		sb.append(String.join(", ", equipTypes));
		sb.append("\nAffix can't spawn on: ");
		sb.append(String.join(", ", nonequipTypes));
		sb.append("\n");

		if (type == AffixType.SOCKETABLE) {
			sb.append("Affix can be socketed into: ");
			sb.append(String.join(", ", socketableTypes));
			sb.append("\n");
		} else if (type == AffixType.ENCHANTMENT) {
			sb.append("Affix can be enchanted by: ");
			sb.append(String.join(", ", socketableTypes));
			sb.append("\n");
		}

		return sb.toString();
	}

    @Override
	public int compareTo(AffixGroup other) {
		return toString().compareTo(other.toString());
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (EffectGroup group:     effects)         sb.append(group);
        for (String enchantSource:  enchantSources)  sb.append(enchantSource);
        for (String equipType:      equipTypes)      sb.append(equipType);
        for (String nonEquipType:   nonequipTypes)   sb.append(nonEquipType);
        for (String socketableType: socketableTypes) sb.append(socketableType);
        
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(effects, enchantSources, equipTypes, nonequipTypes,
                socketableTypes, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AffixGroup other = (AffixGroup) obj;
        return Objects.equals(effects, other.effects)
                && Objects.equals(enchantSources, other.enchantSources)
                && Objects.equals(equipTypes, other.equipTypes)
                && Objects.equals(nonequipTypes, other.nonequipTypes)
                && Objects.equals(socketableTypes, other.socketableTypes)
                && type == other.type;
    }
}
