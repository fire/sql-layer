/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.server.service.dxl;

import com.akiban.server.service.session.Session;

public interface DXLFunctionsHook {

    static enum DXLType {
        DDL_FUNCTIONS_WRITE,
        DDL_FUNCTIONS_READ,
        DML_FUNCTIONS_WRITE,
        DML_FUNCTIONS_READ
    }

    static enum DXLFunction {
        CREATE_TABLE(DXLType.DDL_FUNCTIONS_WRITE),
        RENAME_TABLE(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_TABLE(DXLType.DDL_FUNCTIONS_WRITE),
        ALTER_TABLE(DXLType.DDL_FUNCTIONS_WRITE),
        CREATE_VIEW(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_VIEW(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_SCHEMA(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_GROUP(DXLType.DDL_FUNCTIONS_WRITE),
        GET_AIS(DXLType.DDL_FUNCTIONS_READ),
        GET_TABLE_ID(DXLType.DDL_FUNCTIONS_READ),
        GET_TABLE_BY_ID(DXLType.DDL_FUNCTIONS_READ),
        GET_TABLE_BY_NAME(DXLType.DDL_FUNCTIONS_READ),
        GET_USER_TABLE_BY_NAME(DXLType.DDL_FUNCTIONS_READ),
        GET_USER_TABLE_BY_ID(DXLType.DDL_FUNCTIONS_READ),
        GET_ROWDEF(DXLType.DDL_FUNCTIONS_READ),
        GET_DDLS(DXLType.DDL_FUNCTIONS_READ),
        GET_SCHEMA_ID(DXLType.DDL_FUNCTIONS_READ),
        GET_SCHEMA_TIMESTAMP(DXLType.DDL_FUNCTIONS_READ),
        CREATE_INDEXES(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_INDEXES(DXLType.DDL_FUNCTIONS_WRITE),
        CHECK_AND_FIX_INDEXES(DXLType.DDL_FUNCTIONS_WRITE),
        CREATE_SEQUENCE(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_SEQUENCE(DXLType.DDL_FUNCTIONS_WRITE),
        CREATE_ROUTINE(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_ROUTINE(DXLType.DDL_FUNCTIONS_WRITE),
        CREATE_SQLJ_JAR(DXLType.DDL_FUNCTIONS_WRITE),
        REPLACE_SQLJ_JAR(DXLType.DDL_FUNCTIONS_WRITE),
        DROP_SQLJ_JAR(DXLType.DDL_FUNCTIONS_WRITE),
        
        ALTER_TABLE_TEMP_TABLE(DXLType.DDL_FUNCTIONS_WRITE),

        GET_TABLE_STATISTICS(DXLType.DML_FUNCTIONS_READ),
        OPEN_CURSOR(DXLType.DML_FUNCTIONS_READ),
        GET_CURSOR_STATE(DXLType.DML_FUNCTIONS_READ),
        SCAN_SOME(DXLType.DML_FUNCTIONS_READ),
        CLOSE_CURSOR(DXLType.DML_FUNCTIONS_READ),
        GET_CURSORS(DXLType.DML_FUNCTIONS_READ),
        CONVERT_NEW_ROW(DXLType.DML_FUNCTIONS_READ),
        CONVERT_ROW_DATA(DXLType.DML_FUNCTIONS_READ),
        CONVERT_ROW_DATAS(DXLType.DML_FUNCTIONS_READ),
        WRITE_ROW(DXLType.DML_FUNCTIONS_WRITE),
        DELETE_ROW(DXLType.DML_FUNCTIONS_WRITE),
        UPDATE_ROW(DXLType.DML_FUNCTIONS_WRITE),
        TRUNCATE_TABLE(DXLType.DML_FUNCTIONS_WRITE),
        UPDATE_TABLE_STATISTICS(DXLType.DML_FUNCTIONS_WRITE),

        
        // For use by Postgres
        UNSPECIFIED_DDL_WRITE(DXLType.DDL_FUNCTIONS_WRITE),
        UNSPECIFIED_DDL_READ(DXLType.DDL_FUNCTIONS_READ),
        UNSPECIFIED_DML_WRITE(DXLType.DML_FUNCTIONS_WRITE),
        UNSPECIFIED_DML_READ(DXLType.DML_FUNCTIONS_READ),
        ;

        private final DXLType type;

        DXLFunction(DXLType type) {
            this.type = type;
        }

        public DXLType getType() {
            return type;
        }
    }
    void hookFunctionIn(Session session, DXLFunction function);
    void hookFunctionCatch(Session session, DXLFunction function, Throwable throwable);
    void hookFunctionFinally(Session session, DXLFunction function, Throwable throwable);
}
