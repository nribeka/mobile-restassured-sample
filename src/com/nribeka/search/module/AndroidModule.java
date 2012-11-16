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

package com.nribeka.search.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.nribeka.search.sample.ServerConfigRegistry;

public class AndroidModule extends AbstractModule {

    private ServerConfigRegistry registry;

    public AndroidModule(final String server, final String username, final String password) {
        registry = new ServerConfigRegistry();
        registry.putEntry("server", server);
        registry.putEntry("username", username);
        registry.putEntry("password", password);
    }

    @Override
    protected void configure() {
        // bind the lucene location in the filesystem
        bind(String.class)
                .annotatedWith(Names.named("configuration.lucene.directory"))
                .toInstance("/mnt/sdcard/lucene");
        // bind the default search key
        bind(String.class)
                .annotatedWith(Names.named("configuration.lucene.document.key"))
                .toInstance("name");

        bind(ServerConfigRegistry.class).toInstance(registry);
    }
}
