package com.intellij.structuralsearch.impl.matcher.strategies;

import com.intellij.psi.*;

/**
 * CommonStrategy to match symbols
 */
public class SymbolMatchingStrategy extends ExprMatchingStrategy {
  public void visitReferenceList(final PsiReferenceList list) {
    result = true;
  }

  public void visitTypeParameterList(PsiTypeParameterList psiTypeParameterList) {
    result = true;
  }

  public void visitTypeElement(PsiTypeElement psiTypeElement) {
    result = true;
  }

  public void visitReferenceElement(PsiJavaCodeReferenceElement psiJavaCodeReferenceElement) {
    result = true;
  }

  public void visitReferenceParameterList(PsiReferenceParameterList psiReferenceParameterList) {
    result = true;
  }

  private SymbolMatchingStrategy() {}
  private static SymbolMatchingStrategy instance;

  public static MatchingStrategy getInstance() {
    if (instance==null) instance = new SymbolMatchingStrategy();
    return instance;
  }
}
