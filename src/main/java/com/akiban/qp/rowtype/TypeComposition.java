/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.qp.rowtype;


import com.akiban.ais.model.UserTable;
import com.akiban.util.ArgumentValidation;

import java.util.*;

public class TypeComposition
{
    /**
     * Indicates whether this is an ancestor of that: this is identical to that, or:
     * - the tables comprising this and that are disjoint, and
     * - the rootmost table of that has an ancestor among the tables of this.
     * @param that
     * @return true if this is an ancestor of that, false otherwise.
     */
    public boolean isAncestorOf(TypeComposition that)
    {
        Boolean ancestor;
        if (this == that) {
            ancestor = Boolean.TRUE;
        } else {
            ancestor = ancestorOf.get(that.rowType);
            if (ancestor == null) {
                // Check for tables in common
                ancestor = Boolean.TRUE;
                for (UserTable table : that.tables) {
                    if (this.tables.contains(table)) {
                        ancestor = Boolean.FALSE;
                    }
                }
                if (ancestor) {
                    // Find rootmost table in that
                    UserTable thatRoot = that.tables.iterator().next();
                    while (thatRoot.parentTable() != null && that.tables.contains(thatRoot.parentTable())) {
                        thatRoot = thatRoot.parentTable();
                    }
                    // this is an ancestor of that if that's rootmost table has an ancestor in this.
                    UserTable thatAncestor = thatRoot;
                    ancestor = Boolean.FALSE;
                    while (thatAncestor != null && !ancestor) {
                        thatAncestor = thatAncestor.parentTable();
                        ancestor = this.tables.contains(thatAncestor);
                    }
                }
                ancestorOf.put(that.rowType, ancestor);
            }
        }
        return ancestor;
    }

    public Set<UserTable> tables()
    {
        return tables;
    }

    public TypeComposition(RowType rowType, UserTable table)
    {
        this(rowType, Arrays.asList(table));
    }

    public TypeComposition(RowType rowType, Collection<UserTable> tables)
    {
        ArgumentValidation.notNull("rowType", rowType);
        ArgumentValidation.notEmpty("tables", tables);
        this.rowType = rowType;
        this.tables = Collections.unmodifiableSet(new HashSet<UserTable>(tables));
    }

    // Object state

    private final RowType rowType;
    private final Set<UserTable> tables;
    private final Map<RowType, Boolean> ancestorOf = new HashMap<RowType, Boolean>();
}
