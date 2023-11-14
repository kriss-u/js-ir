package org.nullvector;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.ssa.JavaScriptPropertyWrite;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

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
                    SSAInstruction[] instructions = ir.getInstructions();
                    // May modify set (of SSA instructions vn)
                    Set<Integer> modVN = new LinkedHashSet<>();
                    // May modify set with  names of JS variables
                    Set<String> modJSVars = new LinkedHashSet<>();
                    // Run loop from the end of the method to the beginning
                    for (int i = instructions.length - 1; i >= 0; i--) {
                        SSAInstruction currentInstruction = instructions[i];
                        if (currentInstruction != null) {
//                            System.out.println(currentInstruction.iIndex() + ": " + currentInstruction + ": " + currentInstruction.getClass());
                            // Property write instruction for object instantiations
                            // TODO: check if the instruction is a local var or a field as mentioned in the algorithm
                            if (currentInstruction instanceof JavaScriptPropertyWrite) {
                                JavaScriptPropertyWrite propertyWrite = (JavaScriptPropertyWrite) currentInstruction;
                                // do something with propertyWrite.getObjectRef() and propertyWrite.getMemberRef()
                            }
                            // hasDef() return true if the instruction has vn (value number)
                            if (currentInstruction.hasDef()) {
                                // Add the vn to the set
                                // FIXME: This might be wrong as getDef returns a value for a declaration
                                modVN.add(currentInstruction.getDef());
                                String[] localNames = ir.getLocalNames(currentInstruction.iIndex(), currentInstruction.getDef());
                                // Add the name of the JS variable(s) to the set if it exists
                                if (localNames.length > 0) {
                                    modJSVars.addAll(Arrays.asList(localNames));
                                }
                            }
                        }
                    }
                    System.out.println("Method: " + m);
                    System.out.println("Mod set: " + modVN);
                    System.out.println("Mod names: " + modJSVars);
                }
            }
        }
    }
}