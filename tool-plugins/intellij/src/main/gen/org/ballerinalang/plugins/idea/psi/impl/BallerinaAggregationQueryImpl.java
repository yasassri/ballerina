/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// This is a generated file. Not intended for manual editing.
package org.ballerinalang.plugins.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.ballerinalang.plugins.idea.psi.BallerinaTypes.*;
import org.ballerinalang.plugins.idea.psi.*;

public class BallerinaAggregationQueryImpl extends BallerinaCompositeElementImpl implements BallerinaAggregationQuery {

  public BallerinaAggregationQueryImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitAggregationQuery(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaOrderByClause getOrderByClause() {
    return PsiTreeUtil.getChildOfType(this, BallerinaOrderByClause.class);
  }

  @Override
  @Nullable
  public BallerinaSelectClause getSelectClause() {
    return PsiTreeUtil.getChildOfType(this, BallerinaSelectClause.class);
  }

  @Override
  @NotNull
  public BallerinaStreamingInput getStreamingInput() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, BallerinaStreamingInput.class));
  }

  @Override
  @NotNull
  public PsiElement getFrom() {
    return notNullChild(findChildByType(FROM));
  }

}
