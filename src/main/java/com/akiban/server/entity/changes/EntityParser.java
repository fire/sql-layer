/**
 * Copyright (C) 2009-2013 Akiban Technologies, Inc.
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
package com.akiban.server.entity.changes;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.ais.model.NopVisitor;
import com.akiban.ais.model.TableName;
import com.akiban.ais.model.UserTable;
import com.akiban.ais.model.aisb2.AISBBasedBuilder;
import com.akiban.ais.model.aisb2.NewAISBuilder;
import com.akiban.ais.model.aisb2.NewUserTableBuilder;
import com.akiban.server.api.DDLFunctions;
import com.akiban.server.service.session.Session;

public final class EntityParser {
    private static final Logger LOG = LoggerFactory.getLogger(EntityParser.class);
    private int stringWidth = 128;
    
    public EntityParser () {
    }
        
    public void setStringWidth(int width) {
        stringWidth = width;
    }
    public UserTable parse (TableName tableName, JsonNode node) throws IOException {
        NewAISBuilder builder = AISBBasedBuilder.create(tableName.getSchemaName());
        processContainer (node, builder, tableName);
        return builder.ais().getUserTable(tableName);
    }

    public UserTable create (final DDLFunctions ddlFunctions, final Session session, UserTable newRoot) throws IOException {
        newRoot.traverseTableAndDescendants(new NopVisitor() {
            @Override
            public void visitUserTable(UserTable table) {
                ddlFunctions.createTable(session, table);
            }
        });
        return ddlFunctions.getUserTable(session, newRoot.getName());
    }

    public UserTable parseAndCreate (final DDLFunctions ddlFunctions, final Session session,
                                     TableName tableName, JsonNode node) throws IOException {
        return create(ddlFunctions, session, parse(tableName, node));
    }
    
    private void processContainer (JsonNode node, NewAISBuilder builder, TableName tableName) throws IOException {
        boolean first = true;
        if (node.isObject()) {
            processTable (node, builder, tableName);
        } else if (node.isArray()) {
            // For an array of elements, process the first one and discard the rest
            for (JsonNode arrayElement : node) {
                if (first && arrayElement.isObject()) { 
                    processTable(arrayElement, builder, tableName);
                    first = false;
                }
                else if (first && !arrayElement.isContainerNode()) {
                    NewUserTableBuilder table = builder.userTable(tableName.getSchemaName(), tableName.getTableName());
                    addColumnToTable (arrayElement, "value", table);
                    first = false;
                }
                // else throw Bad Json Format Exception
            }
            // If no elements in the array, add a placeholder column
            if (first) {
                NewUserTableBuilder table = builder.userTable(tableName.getSchemaName(), tableName.getTableName());
                table.colString("placeholder", stringWidth, true);
            }
        }
        // else throw Bad Json Format Exception
    }

    private void processTable (JsonNode node, NewAISBuilder builder, TableName tableName) throws IOException {
        
        LOG.trace("Creating Table {}", tableName);
        // Pass one, insert fields from the table
        boolean columnsAdded = false;
        NewUserTableBuilder table = builder.userTable(tableName.getSchemaName(), tableName.getTableName());
        Iterator<Entry<String,JsonNode>> i = node.getFields();
        while (i.hasNext()) {
            Entry<String,JsonNode> field = i.next();
            if (field.getValue().isValueNode()) {
                LOG.trace("Column {}", field.getKey());
                addColumnToTable(field.getValue(), field.getKey(), table);
                columnsAdded = true;
            }
        }
        
        if (!columnsAdded) {
            table.colString("placeholder", stringWidth, true);
            LOG.trace("Column added placeholder");
        }
        // pass 2: insert the child nodes
        boolean first = true;
        String columnName = "_" + tableName.getTableName() + "_id";
        i = node.getFields();
        while (i.hasNext()) {
            Entry<String,JsonNode> field = i.next();
            if (field.getValue().isContainerNode()) {
                LOG.trace("Creating child table {} - first {}", field.getKey(), first);
                if (first) {
                    table.autoIncLong(columnName, 0);
                    table.pk(columnName);
                    first = false;
                }
                TableName childTable = TableName.parse(tableName.getSchemaName(), field.getKey());
                processContainer (field.getValue(), builder, childTable);
                NewUserTableBuilder child = builder.getUserTable(childTable);
                child.colLong(columnName);
                LOG.trace("Column added {}", columnName);
                child.joinTo(tableName).on(columnName, columnName);
                builder.getUserTable(tableName);
            }
        }
    }
    
    private void addColumnToTable (JsonNode node, String name, NewUserTableBuilder table) {
        if (node.isTextual()) {
            int  len = Math.max(node.asText().length(), stringWidth);
            table.colString(name, len, true);
        } else if (node.isIntegralNumber()) {
            table.colBigInt(name, true);
        } else if (node.isDouble()) {
            table.colDouble(name, true);
        } else if (node.isBoolean()) {
            table.colBoolean(name, true);
        } else if (node.isNull()) {
            // wild guess
            table.colString(name, stringWidth, true);
        }
    }
}