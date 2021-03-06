/*
Copyright 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.jboss.as.cli.impl.aesh;

import org.aesh.command.completer.CompleterInvocation;
import org.wildfly.core.cli.command.aesh.CLICompleterInvocation;
import org.aesh.command.completer.CompleterInvocationProvider;
import org.jboss.as.cli.impl.CommandContextImpl;

/**
 * A CLI specific {@code CompleterInvocationProvider} that creates
 * {@code CLICompleterInvocation}.
 *
 * @author jdenise@redhat.com
 */
public class CLICompleterInvocationProvider implements CompleterInvocationProvider<CLICompleterInvocation> {

    private final CommandContextImpl ctx;

    public CLICompleterInvocationProvider(CommandContextImpl ctx) {
        this.ctx = ctx;
    }

    @Override
    public CLICompleterInvocation enhanceCompleterInvocation(CompleterInvocation completerInvocation) {
        return new CLICompleterInvocation(completerInvocation, ctx.newTimeoutCommandContext());
    }
}
