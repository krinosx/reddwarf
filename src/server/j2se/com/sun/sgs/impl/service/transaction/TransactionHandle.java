/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.impl.service.transaction;

import com.sun.sgs.app.ExceptionRetryStatus;
import com.sun.sgs.app.TransactionAbortedException;
import com.sun.sgs.app.TransactionNotActiveException;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionParticipant;

/** Defines an interface for managing a transaction. */
public interface TransactionHandle {

    /**
     * Returns the transaction associated with this handle.
     *
     * @return	the transaction
     */
    Transaction getTransaction();

    /**
     * Prepares and commits the transaction associated with this handle. <p>
     *
     * If the transaction has been aborted, or when preparing a transaction
     * participant aborts the transaction without throwing an exception, then
     * the exception thrown will have as its cause the value provided in the
     * first call to {@link Transaction#abort abort} on the transaction, if
     * any.  If the cause implements {@link ExceptionRetryStatus}, then the
     * exception thrown will, too, and its {@link
     * ExceptionRetryStatus#shouldRetry shouldRetry} method will return the
     * value returned by calling that method on the cause.  If no cause was
     * supplied, then {@code shouldRetry} will either not implement {@code
     * ExceptionRetryStatus} or its {@code shouldRetry} method will return
     * {@code false}.
     *
     * @throws	TransactionNotActiveException if the transaction has been
     *		aborted
     * @throws	TransactionAbortedException if a call to {@link
     *		TransactionParticipant#prepare prepare} on a transaction
     *		participant aborts the transaction but does not throw an
     *		exception
     * @throws	IllegalStateException if {@code prepare} has been called on any
     *		transaction participant and {@link Transaction#abort abort} has
     *		not been called on the transaction
     * @throws	Exception any exception thrown when calling {@code prepare} on
     *		a participant
     */
    void commit() throws Exception;
}
