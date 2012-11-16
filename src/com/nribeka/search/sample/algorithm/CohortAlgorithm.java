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
package com.nribeka.search.sample.algorithm;

import com.burkeware.search.api.serialization.Algorithm;
import com.jayway.jsonpath.JsonPath;
import com.nribeka.search.sample.domain.Cohort;

public class CohortAlgorithm implements Algorithm {

    /**
     * Implementation of this method will define how the object will be serialized from the String representation.
     *
     * @param serialized the string representation
     * @return the concrete object
     */
    @Override
    public Object deserialize(final String serialized) {
        Cohort cohort = new Cohort();

        Object jsonObject = JsonPath.read(serialized, "$");
        String uuid = JsonPath.read(jsonObject, "$.uuid");
        cohort.setUuid(uuid);
        String name = JsonPath.read(jsonObject, "$.display");
        cohort.setName(name);

        cohort.setJson(serialized);

        return cohort;
    }

    /**
     * Implementation of this method will define how the object will be de-serialized into the String representation.
     *
     * @param object the object
     * @return the string representation
     */
    @Override
    public String serialize(final Object object) {
        Cohort cohort = (Cohort) object;
        return cohort.getJson();
    }
}
