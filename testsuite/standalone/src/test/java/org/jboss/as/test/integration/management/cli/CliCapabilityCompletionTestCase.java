/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.as.test.integration.management.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.Util;
import org.jboss.as.test.integration.management.util.CLITestUtil;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.WildflyTestRunner;

/**
 *
 * @author jdenise@redhat.com
 */
@RunWith(WildflyTestRunner.class)
public class CliCapabilityCompletionTestCase {

    private static CommandContext ctx;
    private static final List<String> interfaces = new ArrayList();

    @BeforeClass
    public static void setup() throws Exception {
        ctx = CLITestUtil.getCommandContext(TestSuiteEnvironment.getServerAddress(),
                TestSuiteEnvironment.getServerPort(), System.in, System.out);
        ctx.connectController();
        ModelNode req = new ModelNode();
        req.get(Util.OPERATION).set("read-children-names");
        req.get("child-type").set("interface");
        ModelNode response = ctx.getModelControllerClient().execute(req);
        if (!response.get(Util.OUTCOME).asString().equals(Util.SUCCESS)) {
            throw new Exception("Can't retrieve interfaces " + response);
        }
        if (!response.get(Util.RESULT).isDefined()) {
            throw new Exception("Can't retrieve interfaces");
        }

        List<ModelNode> itfs = response.get(Util.RESULT).asList();
        if (itfs.isEmpty()) {
            throw new Exception("No interfaces found");
        }
        for(ModelNode mn : itfs) {
            interfaces.add(mn.asString());
        }
        Collections.sort(interfaces);
    }

    @AfterClass
    public static void cleanUp() throws CommandLineException {
        ctx.terminateSession();
    }

    /**
     * Activate completion for simple op argument and write-attribute.
     * Value-type completion testing is done in domain (usage of profiles).
     * @throws Exception
     */
    @Test
    public void testInterfaces() throws Exception {
        {
            String cmd = "/socket-binding-group=standard-sockets/socket-binding=toto:add(interface=";
            List<String> candidates = new ArrayList<>();
            ctx.getDefaultCommandCompleter().complete(ctx, cmd,
                    cmd.length(), candidates);
            assertTrue(candidates.toString(), candidates.equals(interfaces));
        }

        {
            String cmd = "/socket-binding-group=standard-sockets/socket-binding=toto:write-attribute(name=interface,value=";
            List<String> candidates = new ArrayList<>();
            ctx.getDefaultCommandCompleter().complete(ctx, cmd,
                    cmd.length(), candidates);
            assertTrue(candidates.toString(), candidates.equals(interfaces));
        }
    }
}
