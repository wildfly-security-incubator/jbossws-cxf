/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.cxf.bus;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * A test case that verifies Bus references do not leak into EJB3 clients 
 * 
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2010
 *
 */
public class EJB3ClientBusTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(EJB3ClientBusTestCase.class, isTargetJBoss7() ? "jaxws-cxf-bus-as7.war" : "jaxws-cxf-bus.war");
   }
   
   public void testSingleDeploy() throws Exception
   {
      final boolean as7 = isTargetJBoss7();
      deploy(as7 ? "jaxws-cxf-bus-ejb3-client-as7.jar" : "jaxws-cxf-bus-ejb3-client.jar");
      InitialContext iniCtx = null;
      try
      {
         String host = getServerHost();
         iniCtx = getServerInitialContext();
         Object obj = iniCtx.lookup((as7 ? "ejb:/jaxws-cxf-bus-ejb3-client-as7//EJB3Client!" : "ejb:/jaxws-cxf-bus-ejb3-client//EJB3Client!") + EJB3ClientRemoteInterface.class.getName());
         EJB3ClientRemoteInterface ejb3Remote = (EJB3ClientRemoteInterface)obj;
         ejb3Remote.testBusCreation();
         ejb3Remote.testSOAPConnection(host);
         ejb3Remote.testWebServiceClient(host);
         ejb3Remote.testWebServiceRef();
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
         undeploy(as7 ? "jaxws-cxf-bus-ejb3-client-as7.jar" : "jaxws-cxf-bus-ejb3-client.jar");
      }
   }
}
