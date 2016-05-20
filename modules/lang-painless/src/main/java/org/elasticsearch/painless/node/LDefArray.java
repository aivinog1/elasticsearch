/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless.node;

import org.elasticsearch.painless.CompilerSettings;
import org.elasticsearch.painless.Definition;
import org.elasticsearch.painless.DefBootstrap;
import org.elasticsearch.painless.Variables;
import org.objectweb.asm.Type;
import org.elasticsearch.painless.MethodWriter;

import static org.elasticsearch.painless.WriterConstants.DEF_BOOTSTRAP_HANDLE;

/**
 * Represents an array load/store or shortcut on a def type.  (Internal only.)
 */
final class LDefArray extends ALink implements IDefLink {

    AExpression index;

    LDefArray(final int line, final String location, final AExpression index) {
        super(line, location, 2);

        this.index = index;
    }

    @Override
    ALink analyze(final CompilerSettings settings, final Variables variables) {
        index.analyze(settings, variables);
        index.expected = index.actual;
        index = index.cast(settings, variables);

        after = Definition.defType;

        return this;
    }

    @Override
    void write(final CompilerSettings settings, final MethodWriter adapter) {
        index.write(settings, adapter);
    }

    @Override
    void load(final CompilerSettings settings, final MethodWriter adapter) {
        final String desc = Type.getMethodDescriptor(after.type, Definition.defType.type, index.actual.type);
        adapter.invokeDynamic("arrayLoad", desc, DEF_BOOTSTRAP_HANDLE, DefBootstrap.ARRAY_LOAD);
    }

    @Override
    void store(final CompilerSettings settings, final MethodWriter adapter) {
        final String desc = Type.getMethodDescriptor(Definition.voidType.type, Definition.defType.type,
            index.actual.type, after.type);
        adapter.invokeDynamic("arrayStore", desc, DEF_BOOTSTRAP_HANDLE, DefBootstrap.ARRAY_STORE);
    }
}
