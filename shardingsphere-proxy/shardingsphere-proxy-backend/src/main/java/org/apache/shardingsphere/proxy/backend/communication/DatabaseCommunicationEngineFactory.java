/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.communication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.jdbc.JDBCExecuteEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.impl.PreparedStatementAccessor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.impl.StatementAccessor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.kernel.LogicSQLContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database communication engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCommunicationEngineFactory {
    
    private static final DatabaseCommunicationEngineFactory INSTANCE = new DatabaseCommunicationEngineFactory();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static DatabaseCommunicationEngineFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param sqlStatement SQL statement
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return text protocol backend handler
     */
    public DatabaseCommunicationEngine newTextProtocolInstance(final SQLStatement sqlStatement, final String sql, final BackendConnection backendConnection) {
        LogicSQLContext logicSQLContext = createLogicSQLContext(sqlStatement, sql, Collections.emptyList(), backendConnection);
        return new JDBCDatabaseCommunicationEngine(logicSQLContext, new JDBCExecuteEngine(backendConnection, new StatementAccessor()));
    }
    
    /**
     * Create new instance of binary protocol backend handler.
     *
     * @param sqlStatement SQL statement
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @return binary protocol backend handler
     */
    public DatabaseCommunicationEngine newBinaryProtocolInstance(final SQLStatement sqlStatement, final String sql, final List<Object> parameters, final BackendConnection backendConnection) {
        LogicSQLContext logicSQLContext = createLogicSQLContext(sqlStatement, sql, new ArrayList<>(parameters), backendConnection);
        return new JDBCDatabaseCommunicationEngine(logicSQLContext, new JDBCExecuteEngine(backendConnection, new PreparedStatementAccessor()));
    }
    
    private LogicSQLContext createLogicSQLContext(final SQLStatement sqlStatement, final String sql, final List<Object> parameters, final BackendConnection backendConnection) {
        SchemaContext schemaContext = ProxyContext.getInstance().getSchema(backendConnection.getSchemaName());
        return new LogicSQLContext(schemaContext, sql, parameters, sqlStatement);
    }
}
