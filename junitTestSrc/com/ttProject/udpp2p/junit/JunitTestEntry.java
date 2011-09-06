package com.ttProject.udpp2p.junit;

import com.ttProject.junit.TestEntry;

public class JunitTestEntry extends TestEntry {
	@Override
	public void setUp() throws Exception {
		setPackagePath("com.ttProject.udpp2p");
		super.setUp();
	}
}
