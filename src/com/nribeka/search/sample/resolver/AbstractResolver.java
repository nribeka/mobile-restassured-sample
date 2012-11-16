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
import com.nribeka.search.sample.ServerConfigRegistry;

import java.net.URLConnection;

public abstract class AbstractResolver implements Resolver {

    private ServerConfigRegistry registry;

    public AbstractResolver() {
        registry = Context.getInstance(ServerConfigRegistry.class);
    }

    protected String getServer() {
        return registry.getEntryValue("server");
    }

    @Override
    public URLConnection authenticate(final URLConnection connection) {
        String basicAuth =
                ResolverUtil.getBasicAuth(registry.getEntryValue("username"), registry.getEntryValue("password"));
        connection.setRequestProperty("Authorization", basicAuth);
        return connection;
    }
}
