/*
 * Copyright 2016 Harald Wellmann
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
package org.ops4j.pax.exam.invoker.testng.internal;

import java.util.Collections;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.util.Exceptions;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestNGProbeInvoker implements ProbeInvoker {

    private BundleContext ctx;

    public TestNGProbeInvoker(BundleContext bundleContext, Injector injector) {
        ctx = bundleContext;
    }

    @Override
    public void runTest(final TestDescription description, final TestListener listener) {
        ClassLoader classLoader = ctx.getBundle().adapt(BundleWiring.class).getClassLoader();
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            runTestWithTestNG(description, listener);
        }
        // CHECKSTYLE:SKIP - doWithClassLoader API
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }

    }

    private void runTestWithTestNG(TestDescription description, TestListener listener) {
        TestNG testNG = new TestNG();
        testNG.setVerbose(0);
        testNG.setUseDefaultListeners(false);
        ContainerResultListener resultListener = new ContainerResultListener(listener);
        testNG.addListener(resultListener);

        XmlSuite suite = new XmlSuite();
        suite.setName("PaxExamInternal");
        XmlTest xmlTest = new XmlTest(suite);
        XmlClass xmlClass = new XmlClass(description.getClassName());
        xmlTest.getClasses().add(xmlClass);
        if (description.getMethodName() != null) {
            XmlInclude xmlInclude = new XmlInclude(description.getMethodName());
            xmlClass.getIncludedMethods().add(xmlInclude);
        }

        testNG.setXmlSuites(Collections.singletonList(suite));
        testNG.run();
    }



    @Override
    public void runTestClass(String description) {
    }
}
