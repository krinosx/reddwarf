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

package com.sun.sgs.impl.service.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/** Controls how to serialize class descriptors. */
interface ClassSerialization {

    /**
     * Writes a class descriptor to an object output stream.
     *
     * @param	classDesc the class descriptor
     * @param	out the object output stream
     * @throws	IOException if an I/O error occurs
     */
    void writeClassDescriptor(
	ObjectStreamClass classDesc, ObjectOutputStream out)
	throws IOException;

    /**
     * Reads a class descriptor from an object input stream.
     *
     * @param	in the object input stream
     * @return	the class descriptor
     * @throws	ClassNotFoundException if a class referred to by the class
     *		descriptor representation cannot be found
     * @throws	IOException if an I/O error occurs
     */
    ObjectStreamClass readClassDescriptor(ObjectInputStream in)
	throws ClassNotFoundException, IOException;
}

