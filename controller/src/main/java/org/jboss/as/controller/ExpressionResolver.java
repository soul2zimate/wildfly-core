/*
* JBoss, Home of Professional Open Source.
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.controller;

import java.util.regex.Pattern;

import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.dmr.ModelNode;

/**
 * Resolves {@link org.jboss.dmr.ModelType#EXPRESSION} expressions in a {@link ModelNode}.
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
@FunctionalInterface
public interface ExpressionResolver {

    /** A {@link Pattern} that can be used to identify strings that include expression syntax */
    Pattern EXPRESSION_PATTERN = Pattern.compile(".*\\$\\{.*\\}.*");

    /**
     * Resolves any expressions in the passed in ModelNode.
     *
     * Expressions may represent system properties, vaulted date, or a custom format to be handled by an
     * ExpressionResolver registered using the "org.wildfly.controller.expression-resolver" capability.
     *
     * For vaulted data the format is ${VAULT::vault_block::attribute_name::sharedKey}
     *
     * @param node the ModelNode containing expressions.
     * @return a copy of the node with expressions resolved
     *
     * @throws OperationFailedException if there is a value of type {@link org.jboss.dmr.ModelType#EXPRESSION} in the node tree and
     *            there is no system property or environment variable that matches the expression, or if a security
     *            manager exists and its {@link SecurityManager#checkPermission checkPermission} method doesn't allow
     *            access to the relevant system property or environment variable
     */
    ModelNode resolveExpressions(ModelNode node) throws OperationFailedException;

    /**
     * Resolves any expressions in the passed in ModelNode.
     *
     * Expressions may represent system properties, vaulted date, or a custom format to be handled by an
     * ExpressionResolver registered using the "org.wildfly.controller.expression-resolver" capability.
     *
     * For vaulted data the format is ${VAULT::vault_block::attribute_name::sharedKey}
     *
     * @param node the ModelNode containing expressions.
     * @param context the current {@code OperationContext} to provide additional contextual information.
     * @return a copy of the node with expressions resolved
     *
     * @throws OperationFailedException if there is a value of type {@link org.jboss.dmr.ModelType#EXPRESSION} in the node tree and
     *            there is no system property or environment variable that matches the expression, or if a security
     *            manager exists and its {@link SecurityManager#checkPermission checkPermission} method doesn't allow
     *            access to the relevant system property or environment variable
     */
    default ModelNode resolveExpressions(ModelNode node, OperationContext context) throws OperationFailedException {
        return resolveExpressions(node);
    }

    /**
     * An {@code ExpressionResolver} that can only resolve from system properties
     * and environment variables. Should not be used for most product resolution use cases as it does
     * not support resolution from a security vault.
     */
    ExpressionResolver SIMPLE = new ExpressionResolverImpl();

    /**
     * An {@code ExpressionResolver} suitable for test cases that can only resolve from system properties
     * and environment variables.
     * Should not be used for production code as it does not support resolution from a security vault.
     */
    ExpressionResolver TEST_RESOLVER = SIMPLE;

    /**
     * An expression resolver that will not throw an {@code OperationFailedException} when it encounters an
     * unresolvable expression, instead simply returning that expression. Should not be used for most product
     * resolution use cases as it does not support resolution from a security vault.
     */
    ExpressionResolver SIMPLE_LENIENT = new ExpressionResolverImpl(true);

    /**
     * An expression resolver that throws an {@code OperationFailedException} if any expressions are found.
     * Intended for use with APIs where an {@code ExpressionResolver} is required but the caller requires
     * that all expression have already been resolved.
     */
    ExpressionResolver REJECTING = new ExpressionResolverImpl() {
        @Override
        protected void resolvePluggableExpression(ModelNode node, OperationContext context) throws OperationFailedException {
            String expression = node.asString();
            if (EXPRESSION_PATTERN.matcher(expression).matches()) {
                throw ControllerLogger.ROOT_LOGGER.illegalUnresolvedModel(expression);
            }
            // It wasn't an expression anyway; convert the node to type STRING
            node.set(expression);
        }
    };
}
