/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.action.hdfs.data.parser;

import com.ning.metrics.action.binder.config.ActionCoreConfig;
import com.ning.metrics.action.hdfs.data.RowAccessException;
import com.ning.metrics.action.hdfs.data.Rows;
import com.ning.metrics.action.schema.Registrar;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RowParser implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(RowParser.class);

    private final Map<String, RowSerializer> serializations = new HashMap<String, RowSerializer>();
    private final ClassLoader classLoader;

    @Inject
    public RowParser(final ActionCoreConfig conf)
    {
        classLoader = RowParser.class.getClassLoader();

        final String defaultSerializations = "" +
            "com.ning.metrics.action.hdfs.data.parser.ThriftEnvelopeRowSerializer," +
            "com.ning.metrics.action.hdfs.data.parser.ThriftRowSerializer," +
            "com.ning.metrics.action.hdfs.data.parser.SmileRowSerializer," +
            "com.ning.metrics.action.hdfs.data.parser.WritableRowSerializer," +
            "com.ning.metrics.action.hdfs.data.parser.StringRowSerializer,";

        // TODO ServiceLoader
        for (final String serializerName : StringUtils.split(defaultSerializations + conf.getRowSerializations(), ",")) {
            try {
                add(serializerName);
            }
            catch (ClassNotFoundException e) {
                log.warn("Ignoring specified RowSerializer [{}], as it is not in classpath", serializerName);
            }
            catch (InstantiationException e) {
                log.warn("Ignoring specified RowSerializer [{}]: {}", serializerName, e);
            }
            catch (IllegalAccessException e) {
                log.warn("Ignoring specified RowSerializer [{}]: {}", serializerName, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void add(final String serializationName) throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        final Class<? extends RowSerializer> serializionClass = (Class<? extends RowSerializer>) Class.forName(serializationName, true, classLoader);
        serializations.put(serializationName, serializionClass.newInstance());
    }

    /**
     * Given a line (or an object in a SequenceFile) in a file, deserialize it into a list of rows
     *
     * @param r    Registrar, to match with schema
     * @param line payload to deserialize
     * @return Rows, list of Row
     * @throws RowAccessException if we don't know how to deserialize the line
     */
    public Rows valueToRows(final Registrar r, final Object line) throws RowAccessException
    {
        for (final RowSerializer serialization : serializations.values()) {
            if (serialization.accept(line)) {
                return serialization.toRows(r, line);
            }
        }
        throw new RowAccessException(String.format("unknown class type: %s", line.getClass().getName()));
    }
}
