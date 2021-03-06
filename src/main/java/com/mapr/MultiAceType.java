package com.mapr;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MultiAceType {

	FILEREAD("rf"), 
	FILEWRITE("wf"), 
	FILEEXECUTE("ef"), 
	READDIR("rd"),
	ADDCHILD("ac"),
	DELETECHILD("dc"),
	LOOKUPDIR("ld");
	
	

	private final String accessType;
	private static final Map<String, MultiAceType> ENUM_MAP;

	/**
	 * @param text
	 */
	private MultiAceType(final String accessType) {
		this.accessType = accessType;
	}

	private String getAccessType() {
		return accessType;
	}

	static {
		Map<String, MultiAceType> map = new ConcurrentHashMap<String, MultiAceType>();
		for (MultiAceType instance : MultiAceType.values()) {
			map.put(instance.getAccessType(), instance);
		}
		ENUM_MAP = Collections.unmodifiableMap(map);
	}

	public static MultiAceType get(String accessType) {
		return ENUM_MAP.get(accessType);
	}


	@Override
	public String toString() {
		return accessType;
	}
}