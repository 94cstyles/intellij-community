package com.intellij.structuralsearch.impl.matcher.strategies;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

/**
 * CommonStrategy to match the class
 */
public final class ClassMatchingStrategy extends MatchingStrategyBase {
  public void visitJavaFile(final PsiJavaFile file) {
    result = true;
  }
  public void visitClass(final PsiClass clazz) {
    result = true;
  }
}
