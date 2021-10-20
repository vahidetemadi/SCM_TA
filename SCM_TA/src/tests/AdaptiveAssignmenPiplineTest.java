package tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.java.context.State;

public class AdaptiveAssignmenPiplineTest {
	State s;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Before
	public void setUp() throws Exception {
		s=new State("s2", 3);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetState() {
		assertEquals("its working", null, s.getActionSet().isEmpty()?null:s.getActionSet()); // TODO
	}

}
