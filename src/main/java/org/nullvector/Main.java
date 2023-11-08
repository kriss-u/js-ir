package org.nullvector;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    // Run the application using ./gradlew run -q --args='filename' > output.ir
    public static void main(String[] args) throws ClassHierarchyException {
        // Get the filename from the command line
        Path path = Paths.get(args[0]);

        // Use Rhino to parse Javascript
        JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());

        // Build a class hierarchy, for access to code info
        IClassHierarchy cha = JSCallGraphUtil.makeHierarchyForScripts(path.toString());

        // Constructing IRs
        IRFactory<IMethod> factory = AstIRFactory.makeDefaultFactory();
        for (IClass klass : cha) {
            // Ignore models of built-in Javascript methods from prologue.js
            if (!klass.getName().toString().startsWith("Lprologue.js")) {
                // Get the IMethod representing the code (the 'do' method)
                IMethod m = klass.getMethod(AstMethodReference.fnSelector);
                if (m != null) {
                    IR ir = factory.makeIR(m, Everywhere.EVERYWHERE, new SSAOptions());
                    System.out.println(ir);
                }
            }
        }
    }
}