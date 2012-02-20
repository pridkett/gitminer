class Helpers {
    static printSortedMap(Map inmap) {
        for (p in inmap.sort{a,b -> a.key <=> b.key}) {
            println "==>" + p.key + "=" + p.value
        }
    }
	
	static setDifference(Collection s1, Collection s2) {
		def diff = (s1 as Set) + s2
		def tmp = s1 as Set
		tmp.retainAll(s2)
		diff.removeAll(tmp)
		return diff;
	}
	
	static setDifferenceLeft(Collection s1, Collection s2) {
		return s1.intersect(setDifference(s1, s2))
	}
}
