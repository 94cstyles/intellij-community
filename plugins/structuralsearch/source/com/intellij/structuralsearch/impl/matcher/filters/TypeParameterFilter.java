package com.intellij.structuralsearch.impl.matcher.filters;

import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiJavaCodeReferenceElement;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 03.01.2004
 * Time: 1:06:23
 * To change this template use Options | File Templates.
 */
public class TypeParameterFilter extends NodeFilter {
  public void visitTypeElement(PsiTypeElement psiTypeElement) {
    result = true;
  }

  public void visitTypeParameter(PsiTypeParameter psiTypeParameter) {
    result = true;
  }

  public void visitReferenceElement(PsiJavaCodeReferenceElement psiJavaCodeReferenceElement) {
    result = true;
  }

  private TypeParameterFilter() {}

  public static NodeFilter getInstance() {
    if (instance==null) instance = new TypeParameterFilter();
    return instance;
  }
  private static NodeFilter instance;
}
