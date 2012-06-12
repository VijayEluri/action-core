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

package com.ning.metrics.action.binder.modules;

import com.ning.metrics.action.binder.config.ActionCoreConfig;
import com.ning.metrics.action.schema.GoodwillRegistrar;
import com.ning.metrics.action.schema.NoOpRegistrar;
import com.ning.metrics.action.schema.Registrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class RegistrarProvider implements Provider<Registrar>
{
    private static final Logger log = LoggerFactory.getLogger(RegistrarProvider.class);

    private ActionCoreConfig config;

    @Inject
    public RegistrarProvider(final ActionCoreConfig config)
    {
        this.config = config;
    }

    @Override
    public Registrar get()
    {
        if (config.isRegistrarEnabled()) {
            try {
                return new GoodwillRegistrar(config);
            }
            catch (Exception e) {
                log.error("Unable to connect to Goodwill", e);
                return new NoOpRegistrar();
            }
        }
        else {
            return new NoOpRegistrar();
        }
    }
}
