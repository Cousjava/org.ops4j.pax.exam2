/*
 * Copyright 2012 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;

import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ContextName;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class TomcatTestContainer implements TestContainer
{
    private static final Logger LOG = LoggerFactory.getLogger( TomcatTestContainer.class );

    private Stack<String> deployed = new Stack<String>();

    private ExamSystem system;

    private TestDirectory testDirectory;

    private Tomcat tomcat;

    private TomcatHostConfig hostConfig;

    private File webappDir;

    public TomcatTestContainer( ExamSystem system, FrameworkFactory frameworkFactory )
    {
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();
    }

    public synchronized void call( TestAddress address )
    {
        TestInstantiationInstruction instruction = testDirectory.lookup( address );
        ProbeInvokerFactory probeInvokerFactory =
            ServiceProviderFinder.loadUniqueServiceProvider( ProbeInvokerFactory.class );
        ProbeInvoker invoker =
            probeInvokerFactory.createProbeInvoker( null, instruction.toString() );
        invoker.call( address.arguments() );
    }

    public synchronized long install( String location, InputStream stream )
    {
        deployModule( "Pax-Exam-Probe", stream);
        return -1;
    }

    public synchronized long install( InputStream stream )
    {
        return install( "local", stream );
    }

    public void deployModules()
    {
        UrlDeploymentOption[] deploymentOptions = system.getOptions( UrlDeploymentOption.class );
        int numModules = 0;
        for( UrlDeploymentOption option : deploymentOptions )
        {
            numModules++;
            if( option.getName() == null )
            {
                option.name( "app" + numModules );
            }
            deployModule( option );
        }
    }

    private void deployModule( UrlDeploymentOption option )
    {
        try
        {
            URL applUrl = new URL( option.getURL() );
            deployModule(option.getName(), applUrl.openStream());
        }
        catch ( MalformedURLException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
        }
    }
    
    private void deployModule( String applicationName, InputStream stream)
    {
        
        try
        {
            File warFile = new File(webappDir, applicationName + ".war");
            StreamUtils.copyStream( stream, new FileOutputStream(warFile), true );            
            hostConfig.deployWAR( new ContextName( applicationName ), warFile);            
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
    }

    public void cleanup()
    {
        undeployModules();
        LOG.info( "stopping Tomcat");
        try
        {
            tomcat.stop();
        }
        catch ( LifecycleException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    private void undeployModules()
    {
        while( !deployed.isEmpty() )
        {
            String applicationName = deployed.pop();
            hostConfig.unmanageApp( "/" + applicationName );
        }
    }

    public TestContainer start() 
    {
        LOG.info( "starting Tomcat");
        
        
        if (tomcat != null) {
            return this;
        }

        
        File tempDir = system.getTempFolder();
        webappDir = new File(tempDir, "webapps");
        webappDir.mkdirs();
        
        tomcat = new Tomcat();
        tomcat.setBaseDir(tempDir.getPath());

       
        Host host = tomcat.getHost();
        host.setDeployOnStartup( false );
        host.setAutoDeploy( false );
        host.setConfigClass( TomcatContextConfig.class.getName() );
        hostConfig = new TomcatHostConfig();
        host.addLifecycleListener( hostConfig );
        
        try
        {
            int httpPort = 9080;
            Connector connector = tomcat.getConnector();
            connector.setPort( httpPort );
            // see https://issues.apache.org/bugzilla/show_bug.cgi?id=50360
            connector.setProperty( "bindOnInit", "false" );
            tomcat.start();
            testDirectory.setAccessPoint( new URI( "http://localhost:" + httpPort + "/Pax-Exam-Probe/" ) );
        }
        catch ( URISyntaxException exc )
        {
            new TestContainerException( exc );
        }
        catch ( LifecycleException exc )
        {
            new TestContainerException( exc );
        }
        return this;
    }

    public TestContainer stop()
    {
        cleanup();
        system.clear();
        return this;
    }

    @Override
    public String toString()
    {
        return "TomcatContainer";
    }
}
