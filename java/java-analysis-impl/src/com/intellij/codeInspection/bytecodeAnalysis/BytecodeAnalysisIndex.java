/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInspection.bytecodeAnalysis;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.EnumeratorIntegerDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author lambdamix
 */
public class BytecodeAnalysisIndex extends FileBasedIndexExtension<Integer, IntIdEquation> {
  public static final ID<Integer, IntIdEquation> NAME = ID.create("bytecodeAnalysis");
  private final EquationExternalizer myExternalizer = new EquationExternalizer();
  private static final DataIndexer<Integer, IntIdEquation, FileContent> INDEXER =
    new ClassDataIndexer(BytecodeAnalysisConverter.getInstance());

  private static final int ourInternalVersion = 3;
  private static boolean ourEnabled = SystemProperties.getBooleanProperty("idea.enable.bytecode.contract.inference", isEnabledByDefault());

  private static boolean isEnabledByDefault() {
    Application application = ApplicationManager.getApplication();
    return application.isInternal() || application.isUnitTestMode();
  }

  @NotNull
  @Override
  public ID<Integer, IntIdEquation> getName() {
    return NAME;
  }

  @NotNull
  @Override
  public DataIndexer<Integer, IntIdEquation, FileContent> getIndexer() {
    return INDEXER;
  }

  @NotNull
  @Override
  public KeyDescriptor<Integer> getKeyDescriptor() {
    return EnumeratorIntegerDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<IntIdEquation> getValueExternalizer() {
    return myExternalizer;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(JavaClassFileType.INSTANCE) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        return ourEnabled && super.acceptInput(file);
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return ourInternalVersion + BytecodeAnalysisConverter.getInstance().getVersion() + (ourEnabled ? 0xFF : 0);
  }

  public static class EquationExternalizer implements DataExternalizer<IntIdEquation> {
    @Override
    public void save(@NotNull DataOutput out, IntIdEquation equation) throws IOException {
      out.writeInt(equation.id);
      IntIdResult rhs = equation.rhs;
      if (rhs instanceof IntIdFinal) {
        IntIdFinal finalResult = (IntIdFinal)rhs;
        out.writeBoolean(true); // final flag
        DataInputOutputUtil.writeINT(out, finalResult.value.ordinal());
      } else {
        IntIdPending pendResult = (IntIdPending)rhs;
        out.writeBoolean(false); // pending flag
        DataInputOutputUtil.writeINT(out, pendResult.delta.length);

        for (IntIdComponent component : pendResult.delta) {
          DataInputOutputUtil.writeINT(out, component.value.ordinal());
          int[] ids = component.ids;
          DataInputOutputUtil.writeINT(out, ids.length);
          for (int id : ids) {
            out.writeInt(id);
          }
        }
      }
    }

    @Override
    public IntIdEquation read(@NotNull DataInput in) throws IOException {
      int equationId = in.readInt();
      boolean isFinal = in.readBoolean(); // flag
      if (isFinal) {
        int ordinal = DataInputOutputUtil.readINT(in);
        Value value = Value.values()[ordinal];
        return new IntIdEquation(equationId, new IntIdFinal(value));
      } else {

        int sumLength = DataInputOutputUtil.readINT(in);
        IntIdComponent[] components = new IntIdComponent[sumLength];

        for (int i = 0; i < sumLength; i++) {
          int ordinal = DataInputOutputUtil.readINT(in);
          Value value = Value.values()[ordinal];
          int componentSize = DataInputOutputUtil.readINT(in);
          int[] ids = new int[componentSize];
          for (int j = 0; j < componentSize; j++) {
            ids[j] = in.readInt();
          }
          components[i] = new IntIdComponent(value, ids);
        }
        return new IntIdEquation(equationId, new IntIdPending(components));
      }
    }
  }
}
