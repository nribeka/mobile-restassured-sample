/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package com.nribeka.search.sample.resolver;

import com.burkeware.search.api.Context;
import com.burkeware.search.api.resolver.Resolver;
import com.burkeware.search.api.util.ResolverUtil;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.net.URLConnection;

public abstract class AbstractResolver implements Resolver {

    protected String server;

    protected String username;

    protected String password;

    public AbstractResolver() {
        server = Context.getInstance(Key.get(String.class, Names.named("configuration.server.url")));
        username = Context.getInstance(Key.get(String.class, Names.named("configuration.server.username")));
        password = Context.getInstance(Key.get(String.class, Names.named("configuration.server.password")));
    }

    protected String getServer() {
        return server;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

    @Override
    public URLConnection authenticate(final URLConnection connection) {
        String basicAuth = ResolverUtil.getBasicAuth("admin", "test");
        connection.setRequestProperty("Authorization", basicAuth);
        return connection;
    }
}
