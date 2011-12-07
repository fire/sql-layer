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

package com.akiban.server.expression.std;

import com.akiban.server.expression.Expression;
import com.akiban.server.expression.ExpressionComposer;
import com.akiban.server.expression.ExpressionEvaluation;
import com.akiban.server.expression.ExpressionType;
import com.akiban.server.service.functions.Scalar;
import com.akiban.server.types.AkType;
import java.util.List;

public class DateTimeArithExpression extends ArithExpression
{
    @Scalar("timediff")
    public static final ExpressionComposer TIMEDIFF_COMPOSER = new InternalComposer(AkType.TIME, AkType.TIME);

    @Scalar("datediff")
    public static final ExpressionComposer DATEDIFF_COMPOSER = new InternalComposer(AkType.DATE, AkType.LONG);

    private static class InternalComposer extends BinaryComposer
    {
        private final AkType topT;
        private final AkType argT;
        public InternalComposer (AkType argT, AkType topT)
        {
            this.topT = topT;
            this.argT = argT;
        }

        @Override
        protected Expression compose(Expression first, Expression second)
        {
            return new DateTimeArithExpression(first, second, topT);
        }

        @Override
        protected ExpressionType composeType(ExpressionType first, ExpressionType second)
        {
            return ExpressionTypes.newType(topT, 0, 0);
        }

        @Override
        public void argumentTypes(List<AkType> argumentTypes)
        {
            // TODO: DATETIME AND TIMESTAMP are the two types
            // that always work for both date/time ariths,
            // we dont know how to specify that in this
            // method yet
            for (int n = 0; n < argumentTypes.size(); ++n)
                argumentTypes.set(n, argT);
        }
    }

    protected static class InnerValueSource extends ArithExpression.InnerValueSource
    {
        private static final double SECS_OF_DAY = 86400;

        public InnerValueSource (ArithOp op, AkType topT)
        {
            super(op, topT);
        }

        /**
         *
         * @return number of DAY between two events
         */
        @Override
        public long getLong ()
        {
            check(AkType.LONG);
            long seconds = rawInterval() / 1000L;

            return seconds < 0 ?
                (long)Math.floor(seconds /SECS_OF_DAY) : (long) Math.ceil(seconds / SECS_OF_DAY);
        }

        /**
         *
         * @return INTERVAL between two events expressed in HOUR:MINUTE:SECOND
         */
        @Override
        public long getTime ()
        {
            check(AkType.TIME);
            long seconds = rawInterval() / 1000L;
            long sign;
            if (seconds < 0)
                seconds *= (sign = -1);
            else
                sign = 1;
            long hours = seconds / 3600;
            long minutes = (seconds - hours * 3600) / 60;
            seconds -= hours * 3600 + minutes * 60;
            return sign * (hours * 10000L + minutes * 100 + seconds);
        }
    }

    /**
     *  topT
     *  means INTERVAL expressed in this type
     *  For example, time minus time => interval
     *  if topT is set to TIME => result is INTERVAL in HOURS, SECONDS, MINUTES
     */
    private final AkType topT;
    protected DateTimeArithExpression (Expression left, Expression right, AkType topT)
    {
        super(left, ArithOps.MINUS, right);
        this.topT = topT;
    }

    @Override
    protected InnerValueSource getValueSource (ArithOp op, AkType topT)
    {
        return new InnerValueSource(op, topT);
    }

    @Override
    public ExpressionEvaluation evaluation ()
    {
        return new InnerEvaluation(op, topT, this, childrenEvaluations());
    }
}
