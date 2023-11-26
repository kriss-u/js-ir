package org.nullvector;


import java.util.HashSet;
import java.util.Set;

public class EntryLiveSet {
    private Set<Integer> undeterminedVns;
    private Set<String> liveVars;

    public EntryLiveSet() {
        this.undeterminedVns = new HashSet<>();
        this.liveVars = new HashSet<>();
    }

    public EntryLiveSet(Set<Integer> undeterminedVns, Set<String> liveVars) {
        this.undeterminedVns = undeterminedVns;
        this.liveVars = liveVars;
    }

    public Set<Integer> getUndeterminedVns() {
        return undeterminedVns;
    }

    public void setUndeterminedVns(Set<Integer> undeterminedVns) {
        this.undeterminedVns = undeterminedVns;
    }

    public Set<String> getLiveVars() {
        return liveVars;
    }

    public void setLiveVars(Set<String> liveVars) {
        this.liveVars = liveVars;
    }

    @Override
    public String toString() {
        return "EntryLiveSet{" +
                "undeterminedVns=" + undeterminedVns +
                ", liveVars=" + liveVars +
                '}';
    }

}
