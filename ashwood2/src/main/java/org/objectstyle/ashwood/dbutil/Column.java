/* ====================================================================
 *
 * Copyright(c) 2003, Andriy Shapochka
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the ASHWOOD nor the
 *    names of its contributors may be used to endorse or
 *    promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by
 * individuals on behalf of the ASHWOOD Project and was originally
 * created by Andriy Shapochka.
 *
 */

package org.objectstyle.ashwood.dbutil;

import java.io.Serializable;

public class Column implements Serializable {

  private Table owner;
  private String name;
  private int dataType;
  private String typeName;
  private int size;
  private int decimalDigits;
  private int radix;
  private int nullable;
  private String remarks;
  private String defaultValue;
  private int charOctetLength;
  private int ordinalPosition;
  public Column() {
  }
  public Table getOwner() {
    return owner;
  }
  public void setOwner(Table owner) {
    this.owner = owner;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }
  public void setDataType(int dataType) {
    this.dataType = dataType;
  }
  public int getDataType() {
    return dataType;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
  public String getTypeName() {
    return typeName;
  }
  public void setSize(int size) {
    this.size = size;
  }
  public int getSize() {
    return size;
  }
  public void setDecimalDigits(int decimalDigits) {
    this.decimalDigits = decimalDigits;
  }
  public int getDecimalDigits() {
    return decimalDigits;
  }
  public void setRadix(int radix) {
    this.radix = radix;
  }
  public int getRadix() {
    return radix;
  }
  public void setNullable(int nullable) {
    this.nullable = nullable;
  }
  public int getNullable() {
    return nullable;
  }
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
  public String getRemarks() {
    return remarks;
  }
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  public String getDefaultValue() {
    return defaultValue;
  }
  public void setCharOctetLength(int charOctetLength) {
    this.charOctetLength = charOctetLength;
  }
  public int getCharOctetLength() {
    return charOctetLength;
  }
  public void setOrdinalPosition(int ordinalPosition) {
    this.ordinalPosition = ordinalPosition;
  }
  public int getOrdinalPosition() {
    return ordinalPosition;
  }
}
