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

public class AndroidModule extends AbstractModule {

    private String server;

    private String username;

    private String password;

    public AndroidModule(final String server, final String username, final String password) {
        this.server = server;
        this.username = username;
        this.password = password;
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

        // bind the server location, username and password
        bind(String.class)
                .annotatedWith(Names.named("configuration.server.url"))
                .toInstance(server);
        bind(String.class)
                .annotatedWith(Names.named("configuration.server.username"))
                .toInstance(username);
        bind(String.class)
                .annotatedWith(Names.named("configuration.server.password"))
                .toInstance(password);
    }
}
