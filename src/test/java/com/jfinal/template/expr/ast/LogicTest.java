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

package com.jfinal.template.expr.ast;

import com.jfinal.template.expr.Sym;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.Logic;
import com.jfinal.template.stat.Location;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LogicTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void isFalseInputNullOutputTrue() {

    // Arrange
    final Object v = null;

    // Act
    final boolean retval = Logic.isFalse(v);

    // Assert result
    Assert.assertEquals(true, retval);
  }

  @Test
  public void isFalseInputPositiveOutputFalse() {

    // Arrange
    final Object v = 1;

    // Act
    final boolean retval = Logic.isFalse(v);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void isTrueInputNotNullOutputFalse() {

    // Arrange
    final Object v = "";

    // Act
    final boolean retval = Logic.isTrue(v);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void isTrueInputNotNullOutputTrue() {

    // Arrange
    final Object v = "    ";

    // Act
    final boolean retval = Logic.isTrue(v);

    // Assert result
    Assert.assertEquals(true, retval);
  }

  @Test
  public void isTrueInputNullOutputFalse() {

    // Arrange
    final Object v = null;

    // Act
    final boolean retval = Logic.isTrue(v);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void isTrueInputPositiveOutputTrue() {

    // Arrange
    final Object v = 0x0.0000000000004p-1022 /* 1.97626e-323 */;

    // Act
    final boolean retval = Logic.isTrue(v);

    // Assert result
    Assert.assertEquals(true, retval);
  }

  @Test
  public void isTrueInputTrueOutputTrue() {

    // Arrange
    final Object v = true;

    // Act
    final boolean retval = Logic.isTrue(v);

    // Assert result
    Assert.assertEquals(true, retval);
  }
}
