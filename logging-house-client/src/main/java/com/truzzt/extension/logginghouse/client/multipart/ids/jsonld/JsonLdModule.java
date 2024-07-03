/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package com.truzzt.extension.logginghouse.client.multipart.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.net.URI;
import javax.xml.datatype.XMLGregorianCalendar;

public class JsonLdModule extends SimpleModule {

    public JsonLdModule() {
        super();

        addSerializer(URI.class, new UriSerializer());
        addDeserializer(URI.class, new UriDeserializer());

        addSerializer(XMLGregorianCalendar.class, new XmlGregorianCalendarSerializer());
        addDeserializer(XMLGregorianCalendar.class, new XmlGregorianCalendarDeserializer());
    }
}
