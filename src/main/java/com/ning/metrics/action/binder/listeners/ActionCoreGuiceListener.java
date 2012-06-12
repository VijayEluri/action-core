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

package com.ning.metrics.action.binder.listeners;

import com.ning.jetty.base.modules.ServerModuleBuilder;
import com.ning.jetty.core.listeners.SetupServer;
import com.ning.metrics.action.binder.config.ActionCoreConfig;
import com.ning.metrics.action.binder.modules.ActionCoreServicesModule;
import com.ning.metrics.action.binder.modules.HdfsModule;
import com.ning.metrics.action.healthchecks.HDFSHealthCheck;

import javax.servlet.ServletContextEvent;

public class ActionCoreGuiceListener extends SetupServer
{
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        final ServerModuleBuilder builder = new ServerModuleBuilder()
            .addConfig(ActionCoreConfig.class)
            .addHealthCheck(HDFSHealthCheck.class)
            .addJMXExport(HDFSHealthCheck.class)
            .setAreciboProfile(System.getProperty("action.arecibo.profile", "ning.jmx:name=MonitoringProfile"))
            .addModule(new HdfsModule())
            .addModule(new ActionCoreServicesModule())
            .enableLog4J()
            .addJerseyResource("com.ning.metrics.action.endpoint");

        guiceModule = builder.build();

        super.contextInitialized(event);
    }
}
