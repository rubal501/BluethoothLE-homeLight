/*
 *   Project: "Modellbahn Projekt"
 *   Copyright (c) 2015  Dirk Marciniak
 *   File: BufferOverflowException.java
 *   Class: de.dmarcini.rail.dirailviewapp.exceptions.BufferOverflowException
 *
 *   Last modified: 19.08.15 23:25
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/
 *
 *
 */

package de.dmarcini.bt.homelight.exceptions;

import java.io.IOException;

/**
 * Created by dmarc on 16.07.2015.
 */
public class BufferOverflowException extends IOException
{
  public BufferOverflowException( String msg )
  {
    super( msg );
  }

}
