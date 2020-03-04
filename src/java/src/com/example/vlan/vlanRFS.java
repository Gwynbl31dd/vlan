package com.example.vlan;

import java.util.Properties;

import com.tailf.conf.ConfBool;
import com.tailf.conf.ConfBuf;
import com.tailf.conf.ConfException;
import com.tailf.conf.ConfKey;
import com.tailf.conf.ConfObject;
import com.tailf.conf.ConfTag;
import com.tailf.conf.ConfXMLParam;
import com.tailf.conf.ConfXMLParamValue;
import com.tailf.dp.DpActionTrans;
import com.tailf.dp.DpCallbackException;
import com.tailf.dp.annotations.ActionCallback;
import com.tailf.dp.annotations.ServiceCallback;
import com.tailf.dp.proto.ActionCBType;
import com.tailf.dp.proto.ServiceCBType;
import com.tailf.dp.services.ServiceContext;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuList;
import com.tailf.navu.NavuNode;
import com.tailf.ncs.template.Template;
import com.tailf.ncs.template.TemplateVariables;

/**
 * This is a simple VLAN service.
 * The particularity is that I double check.
 * I also attached a Unit test using JUNit 4 and mockito as
 * an example.
 * 
 * @author Anthony Paulin <apaulin@cisco.com>
 * @version 1.0
 * @since 31/01/2019
 *
 */
public class vlanRFS {
	
	/**
	 * Create callback method. This method is called when a service instance
	 * committed due to a create or update event.
	 *
	 * This method returns a opaque as a Properties object that can be null. If not
	 * null it is stored persistently by Ncs. This object is then delivered as
	 * argument to new calls of the create method for this service (fastmap
	 * algorithm). This way the user can store and later modify persistent data
	 * outside the service model that might be needed.
	 *
	 * @param context
	 *            - The current ServiceContext object
	 * @param service
	 *            - The NavuNode references the service node.
	 * @param ncsRoot
	 *            - This NavuNode references the ncs root.
	 * @param opaque
	 *            - Parameter contains a Properties object. This object may be used
	 *            to transfer additional information between consecutive calls to
	 *            the create callback. It is always null in the first call. I.e.
	 *            when the service is first created.
	 * @return Properties the returning opaque instance
	 * @throws ConfException 
	 */
	@ServiceCallback(servicePoint = "vlan-servicepoint", callType = ServiceCBType.CREATE)
	public Properties create(ServiceContext context, NavuNode service, NavuNode ncsRoot, Properties opaque)
			throws ConfException {
		String servicePath = null;
		try {
			
			
			//String ipV4 = ncsRoot.list("services").container("your_service").container("ip_v4_something").leaf("value").valueAsString();
			//service.list("ip_v4_something").leaf("value").set(ipV4);
			
			
			servicePath = service.getKeyPath();
			Template vlanTemplate = new Template(context,"vlan-template");
			TemplateVariables vlanVar = new TemplateVariables();
			setupInterfaces(service.list("device-if"),service,vlanTemplate,service.leaf("vlan-id").valueAsString(),vlanVar);
			
		} catch (Exception e) {
			throw new DpCallbackException("Cannot create service " + servicePath, e);
		}
		return opaque;
	}
	
	public boolean setupInterfaces(NavuList interfaces,NavuNode service,Template vlanTemplate,String vlanString,TemplateVariables vlanVar) throws Exception {
		//Loop through the services' interfaces
		for (NavuContainer intf : interfaces.elements()) {
			String deviceNameString = intf.leaf("device").valueAsString();
			//Interface type is validated by yang and doesn't need to 
			//be revalidated. The type is defined by a choice-case.		
			String ifType = intf.getSelectedCase("interface").getTag();
			String ifNameString = intf.leaf(ifType).valueAsString();
			
			//validate if the vlanString can be converted as an Integer
			if(vlanRFS.isValidVlan(vlanString))
			{
				// Pass the variables to the template variable
				vlanVar.putQuoted("VLAN_ID", vlanString);
				vlanVar.putQuoted("DEVICE", deviceNameString);
				vlanVar.putQuoted("INTF_NAME", ifNameString);
				vlanVar.putQuoted("INT_TYPE", ifType);
				// Obviously here we know our expected output.
				// Therefore, you can use it in the unit test.
				vlanTemplate.apply(service, vlanVar);
			}
			else {
				throw new Exception();
			}
		}
		return true;
	}
	
	/**
	 * This test if a string is an integer
	 * and the integer a valid VLAN
	 * @param str
	 * @return
	 */
	public static boolean isValidVlan(String str) {
	    if (str == null || str.isEmpty()) {
	        return false;
	    }
	    try {
	        long value = Long.valueOf(str);
	        return value >= 1 && value <= 4094;
	    } catch (Exception ex) {
	        return false;
	    }
	}
	
	/**
	 * Init method for selftest action
	 * @param trans
	 * @throws DpCallbackException
	 */
	@ActionCallback(callPoint = "vlan-self-test", callType = ActionCBType.INIT)
	public void init(DpActionTrans trans) throws DpCallbackException {
	}

	/**
	 * Selftest action implementation for service
	 * @param trans
	 * @param name
	 * @param kp
	 * @param params
	 * @return
	 * @throws DpCallbackException
	 */
	@ActionCallback(callPoint = "vlan-self-test", callType = ActionCBType.ACTION)
	public ConfXMLParam[] selftest(DpActionTrans trans, ConfTag name, ConfObject[] kp, ConfXMLParam[] params)
			throws DpCallbackException {
		try {
			// Refer to the service yang model prefix
			String nsPrefix = "vlan";
			// Get the service instance key
			String str = ((ConfKey) kp[0]).toString();
			return new ConfXMLParam[] { new ConfXMLParamValue(nsPrefix, "success", new ConfBool(true)),
					new ConfXMLParamValue(nsPrefix, "message", new ConfBuf(str)) };
		} catch (Exception e) {
			throw new DpCallbackException("self-test failed", e);
		}
	}
}
