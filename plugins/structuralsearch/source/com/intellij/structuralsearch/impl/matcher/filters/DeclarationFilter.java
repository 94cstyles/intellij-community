package com.intellij.structuralsearch.impl.matcher.filters;

import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.PsiDeclarationStatement;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 26.12.2003
 * Time: 19:23:24
 * To change this template use Options | File Templates.
 */
public class DeclarationFilter extends NodeFilter {
  public void visitDeclarationStatement(PsiDeclarationStatement dcl) {
    result = true;
  }

  public void visitVariable(PsiVariable psiVar) {
    result = true;
  }

  public void visitClass(PsiClass psiClass) {
    result = true;
  }

  private static NodeFilter instance;

  public static NodeFilter getInstance() {
    if (instance==null) instance = new DeclarationFilter();
    return instance;
  }

  private DeclarationFilter() {
  }
}
