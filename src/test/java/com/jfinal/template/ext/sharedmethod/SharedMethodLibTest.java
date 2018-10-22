/*
 * Copyright 2018 Diffblue Limited
 *
 * Diffblue Limited licenses this file to You under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.template.ext.sharedmethod;

import com.jfinal.template.ext.sharedmethod.SharedMethodLib;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;

public class SharedMethodLibTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void isEmptyInput0OutputTrue() {

    // Arrange
    final SharedMethodLib objectUnderTest = new SharedMethodLib();
    final HashMap v = new HashMap();

    // Act
    final Boolean retval = objectUnderTest.isEmpty(v);

    // Assert result
    Assert.assertEquals(new Boolean(true), retval);
  }

  @Test
  public void isEmptyInput1OutputFalse() {

    // Arrange
    final SharedMethodLib objectUnderTest = new SharedMethodLib();
    final ArrayList v = new ArrayList();
    v.add(null);

    // Act
    final Boolean retval = objectUnderTest.isEmpty(v);

    // Assert result
    Assert.assertEquals(new Boolean(false), retval);
  }

  @Test
  public void isEmptyInputNullOutputTrue() {

    // Arrange
    final SharedMethodLib objectUnderTest = new SharedMethodLib();
    final Object v = null;

    // Act
    final Boolean retval = objectUnderTest.isEmpty(v);

    // Assert result
    Assert.assertEquals(new Boolean(true), retval);
  }

  @Test
  public void notEmptyInputNullOutputFalse() {

    // Arrange
    final SharedMethodLib objectUnderTest = new SharedMethodLib();
    final Object v = null;

    // Act
    final Boolean retval = objectUnderTest.notEmpty(v);

    // Assert result
    Assert.assertEquals(new Boolean(false), retval);
  }

  @Test
  public void notEmptyInputPositiveOutputIllegalArgumentException() {

    // Arrange
    final SharedMethodLib objectUnderTest = new SharedMethodLib();
    final Object v = 1;

    // Act
    thrown.expect(IllegalArgumentException.class);
    objectUnderTest.notEmpty(v);

    // Method is not expected to return due to exception thrown
  }
}
