package org.metacsp.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PowerSet {
	
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	        sets.add(new HashSet<T>());
	        return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	        newSet.add(head);
	        newSet.addAll(set);
	        sets.add(newSet);
	        sets.add(set);
	    }           
	    return sets;
	}
	
	public static void main(String[] args) {
		HashSet<String> set = new HashSet<String>();
		set.add("One");
		set.add("Two");
		set.add("Three");
		System.out.println(powerSet(set));
	}


}
