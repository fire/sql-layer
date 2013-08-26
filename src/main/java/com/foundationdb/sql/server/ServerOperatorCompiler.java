/**
 * Copyright (C) 2009-2013 FoundationDB, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foundationdb.sql.server;

import com.foundationdb.server.service.tree.KeyCreator;
import com.foundationdb.sql.optimizer.OperatorCompiler;

public abstract class ServerOperatorCompiler extends OperatorCompiler
{
    protected ServerOperatorCompiler() {
    }

    protected void initServer(ServerSession server, KeyCreator keyCreator) {
        initProperties(server.getCompilerProperties());
        initAIS(server.getAIS(), server.getDefaultSchemaName());
        initParser(server.getParser());
        initFunctionsRegistry(server.t3RegistryService());
        initCostEstimator(server.costEstimator(this, keyCreator));
        initPipelineConfiguration(server.getPipelineConfiguration());
        initT3Registry(server.t3RegistryService());
        
        server.getBinderContext().setBinderAndTypeComputer(binder, typeComputer);

        server.setAttribute("compiler", this);
    }

}