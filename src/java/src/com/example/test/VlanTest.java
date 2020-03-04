package com.example.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.example.vlan.vlanRFS;
import com.tailf.conf.ConfException;
import com.tailf.maapi.MaapiSchemas.CSCase;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuException;
import com.tailf.navu.NavuLeaf;
import com.tailf.navu.NavuList;
import com.tailf.navu.NavuNode;
import com.tailf.ncs.template.Template;
import com.tailf.ncs.template.TemplateVariables;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unit test using Mockito and JUnit Used as a tutorial for the NSO Factory
 * This package uses Mockito and JUNIT
 * Make sure you add these jar files to your NSO jar folder,
 * and also add that jars to your eclipse.
 * 
 * @author Anthony Paulin <apaulin@cisco.com>
 * @version 1.0
 * @since 31/01/2019
 *
 */
public class VlanTest {
	// Variable used as a user input
	private String vlanString;
	private String deviceName;
	private String interfaceId;
	private String interfaceType;
	// Expected output
	private String myExpectation;

	@Test
	public void testSetupInterfaces() {
		// This is the object we are testing
		// It's just a simple service with a simple mechanism.
		vlanRFS vlanRFS = new vlanRFS();
		// This is the input from the user
		vlanString = "50";
		deviceName = "c1";
		interfaceId = "0/0";
		interfaceType = "FastEthernet";
		// This is the expected result
		buildOutput();
		// Mock the objects for the test
		// This is considered as our initial context
		NavuList interfaces = mock(NavuList.class);
		NavuNode service = mock(NavuNode.class);
		Template vlanTemplate = mock(Template.class);
		NavuContainer container = mock(NavuContainer.class);
		NavuLeaf leafDevice = mock(NavuLeaf.class);
		NavuLeaf leafIntType = mock(NavuLeaf.class);
		CSCase interfaceCase = mock(CSCase.class);
		TemplateVariables vlanVar = new TemplateVariables();
		try {
			buildMocks(interfaces, service, vlanTemplate, container, leafDevice, leafIntType, interfaceCase);
			vlanRFS.setupInterfaces(interfaces, service, vlanTemplate, vlanString, vlanVar);
			// Here, we test if the template's variables are what we expect (This is our
			// oracle)
			assertEquals(myExpectation, vlanVar.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
		// Lets try again, using another device name
		deviceName = "trolololololololo";
		buildOutput();
		try {
			buildMocks(interfaces, service, vlanTemplate, container, leafDevice, leafIntType, interfaceCase);
			vlanRFS.setupInterfaces(interfaces, service, vlanTemplate, vlanString, vlanVar);
			// Here, we test if the template's variables are what we expect (This is our
			// oracle)
			assertEquals(myExpectation, vlanVar.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
		// We can also check with another interface id
		interfaceId = "0/0/0/0/0/0/0/";
		buildOutput();
		try {
			buildMocks(interfaces, service, vlanTemplate, container, leafDevice, leafIntType, interfaceCase);
			vlanRFS.setupInterfaces(interfaces, service, vlanTemplate, vlanString, vlanVar);
			// Here, we test if the template's variables are what we expect (This is our
			// oracle)
			assertEquals(myExpectation, vlanVar.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	/**
	 * This will do a simple test on the valid VLAN method
	 */
	@Test
	public void testIsValidVlan() {
		// Build our VLANRFS object
		vlanRFS vlanRFS = new vlanRFS();
		String vlan = "5";
		// Now, lets try to add our vlan to the test method
		boolean result = vlanRFS.isValidVlan(vlan);
		// I expect the test to be true
		assertTrue(result);
		//Simple test with a negative vlan
		vlan = "-8";
		// Now, lets try to add our vlan to the test method
		result = vlanRFS.isValidVlan(vlan);
		// I expect the test to be false
		assertFalse(result);
		//Lets try using a vlan at the uper limit
		vlan = "4094";
		// Now, lets try to add our vlan to the test method
		result = vlanRFS.isValidVlan(vlan);
		// I expect the test to be true
		assertTrue(result);
		//Lets try with a vlan out of range
		vlan = "5000";
		// Now, lets try to add our vlan to the test method
		result = vlanRFS.isValidVlan(vlan);
		// I expect the test to be false
		assertFalse(result);

	}

	/**
	 * Build a simplified version of the VLAN NavuObjects
	 * 
	 * @param interfaces
	 * @param service
	 * @param vlanTemplate
	 * @param container
	 * @param leafDevice
	 * @param leafIntType
	 * @param interfaceCase
	 * @throws NavuException
	 */
	private void buildMocks(NavuList interfaces, NavuNode service, Template vlanTemplate, NavuContainer container,
			NavuLeaf leafDevice, NavuLeaf leafIntType, CSCase interfaceCase) throws NavuException {
		//Here I create a mocking of all the objects interaction.
		//It is easy to understand, I use "when" you use this command,
		//Then I return this object/value.
		when(leafDevice.valueAsString()).thenReturn(deviceName);
		when(leafIntType.valueAsString()).thenReturn(interfaceId);
		when(container.leaf("device")).thenReturn(leafDevice);
		when(container.getSelectedCase("interface")).thenReturn(interfaceCase);
		when(interfaceCase.getTag()).thenReturn(interfaceType);
		when(container.leaf(interfaceType)).thenReturn(leafIntType);
		Collection<NavuContainer> collection = new ArrayList<NavuContainer>();
		collection.add(container);
		when(interfaces.elements()).thenReturn(collection);
	}

	/**
	 * Build the expected output (Must be called after modifying the initial
	 * variables)
	 */
	private void buildOutput() {
		//This is what I expect from the Template variable object.
		//I am not tacking care of the template xml, only what I will pass to it.
		// This is a code isolation.
		myExpectation = "{DEVICE='" + deviceName + "', INTF_NAME='" + interfaceId + "', INT_TYPE='" + interfaceType
				+ "', VLAN_ID='" + vlanString + "'}";
	}

}
