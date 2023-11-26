package org.nullvector;

import com.ibm.wala.cast.ir.ssa.*;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final IRFactory<IMethod> factory = AstIRFactory.makeDefaultFactory();

    // Run the application using ./gradlew run -q --args='filename' > output.ir
    public static void main(String[] args) throws WalaException, MalformedURLException, CancelException {
        // Get the filename from the command line
        Path path = Paths.get(args[0]);

        // Use Rhino to parse Javascript
        JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());

        // Build a class hierarchy, for access to code info
        IClassHierarchy cha = JSCallGraphUtil.makeHierarchyForScripts(path.toString());
        for (IClass klass : cha) {
            // Ignore models of built-in Javascript methods from prologue.js
            if (!klass.getName().toString().startsWith("Lprologue.js")) {
                // Get the IMethod representing the code (the 'do' method)
                IMethod m = klass.getMethod(AstMethodReference.fnSelector);
                if (m != null) {

                    Set<String> liveVars = generateEntryLivenessAnalysisSet(m);

                    System.out.println("Method: " + m);
                    System.out.println("Liveness names: " + liveVars);
                }
            }
        }
    }

    public static Set<String> generateMayModifyAnalysisSet(IR ir) {
        SSAInstruction[] instructions = ir.getInstructions();
        // May modify set with  names of JS variables
        Set<String> mod = new LinkedHashSet<>();

        for (int i = instructions.length - 1; i >= 0; i--) {
            SSAInstruction currentInstruction = instructions[i];
            if (currentInstruction != null) {
                System.out.println(currentInstruction.iIndex() + ": " + currentInstruction + ": " + currentInstruction.getClass());
                if (currentInstruction.hasDef()) {
                    String[] localNames = ir.getLocalNames(currentInstruction.iIndex(), currentInstruction.getDef());
                    // Add the name of the JS variable(s) to the set if it exists
                    if (localNames.length > 0) {
                        System.out.println("Local names: " + Arrays.toString(localNames));
                        mod.addAll(Arrays.asList(localNames));
                    }
                }
            }
        }
        return mod;
    }

    public static Set<String> generateEntryLivenessAnalysisSet(IMethod m) {
        IR ir = factory.makeIR(m, Everywhere.EVERYWHERE, new SSAOptions());
        SymbolTable symbolTable = ir.getSymbolTable();

        System.out.println(ir);

        SSAInstruction[] instructions = ir.getInstructions();

        // Entry live set with names of JS variables
        Set<Integer> undeterminedVars = new LinkedHashSet<>();

        // Object prototype lookup map
        Map<Integer, Integer> objectPrototypeMap = new HashMap<>();

        // Entry live set names of JS variables
        Set<String> liveVars = new LinkedHashSet<>();

        // Entry live set names of JS variables
        Map<Integer, EntryLiveSet> entryLiveSetMap = new HashMap<>();

        for (int i = instructions.length - 1; i >= 0; i--) {
            SSAInstruction currentInstruction = instructions[i];
            if (currentInstruction != null) {
                int currentIndex = currentInstruction.iIndex();
                System.out.println(currentIndex + ": " + currentInstruction + ": " + currentInstruction.getClass());

                int currentLineNumber = m.getLineNumber(currentIndex);

                EntryLiveSet currentLineEntryLiveSet = entryLiveSetMap.get(currentLineNumber);
                EntryLiveSet nextLineEntryLiveSet = entryLiveSetMap.get(currentLineNumber + 1);

                if (currentLineEntryLiveSet == null) {
                    if (nextLineEntryLiveSet == null) {
                        currentLineEntryLiveSet = new EntryLiveSet();
                    } else {
                        currentLineEntryLiveSet = new EntryLiveSet(new HashSet<>(nextLineEntryLiveSet.getUndeterminedVns()), new HashSet<>(nextLineEntryLiveSet.getLiveVars()));
                    }
                }

                Set<String> cLLiveVars = currentLineEntryLiveSet.getLiveVars();
                Set<Integer> cLUndeterminedVars = currentLineEntryLiveSet.getUndeterminedVns();


                // Analysis for binary operations
                if (currentInstruction instanceof SSABinaryOpInstruction) {
                    SSABinaryOpInstruction binaryOpInstruction = (SSABinaryOpInstruction) currentInstruction;
                    int assignedVarNumber = binaryOpInstruction.getDef();

                    // Step 1: Remove the assigned variables from the undetermined set
                    //
                    // v13 = binaryop(add) v14 , v17
                    // v17 = binaryop(mul) v12:#20.0 , v19     simple-fn.js [176->191] (line 10) [12=[b]]
                    //
                    // v17 will replace the operands from the v17 instruction

                    undeterminedVars.remove(assignedVarNumber);

                    cLUndeterminedVars.remove(assignedVarNumber);

                    // Step 2: Get the numbers of the operands
                    int n1 = binaryOpInstruction.getUse(0);
                    int n2 = binaryOpInstruction.getUse(1);

                    // Step 3: Get the names of the operands
                    // If the variables are defined in the same block, then the names will be available
                    // Otherwise, add the variable numbers to the undetermined set
                    // It will be available through lexical read
                    String[] n1Names = ir.getLocalNames(currentIndex, n1);
                    String[] n2Names = ir.getLocalNames(currentIndex, n2);

                    // Step 4: Add the intermediate variables to the undetermined set
                    if (n1Names.length > 0) {
                        liveVars.add(n1Names[0]);
                        cLLiveVars.add(n1Names[0]);
                        undeterminedVars.remove(n1);
                        cLUndeterminedVars.remove(n1);
                    } else {
                        if (!symbolTable.getValueString(n1).contains(":#")) {
                            undeterminedVars.add(n1);
                            cLUndeterminedVars.add(n1);
                        }
                    }

                    if (n2Names.length > 0) {
                        liveVars.add(n2Names[0]);
                        cLLiveVars.add(n2Names[0]);
                        undeterminedVars.remove(n2);
                        cLUndeterminedVars.remove(n2);
                    } else {
                        if (!symbolTable.getValueString(n2).contains(":#")) {
                            undeterminedVars.add(n2);
                            cLUndeterminedVars.add(n2);
                        }
                    }
                }

                // Analysis for lexical reads
                if (currentInstruction instanceof AstLexicalRead) {
                    AstLexicalRead lexicalRead = (AstLexicalRead) currentInstruction;

                    // Step 1: get the access that is being read
                    AstLexicalAccess.Access access = lexicalRead.getAccess(0);

                    int vn = access.valueNumber;
                    String name = access.variableName;

                    // Step 2: Remove the assigned var number from the undetermined set
                    undeterminedVars.remove(vn);
                    cLUndeterminedVars.remove(vn);

                    // Step 3: Remove the variables from the live set
                    String[] localNames = ir.getLocalNames(currentIndex, vn);

                    if (localNames.length > 0) {
                        liveVars.remove(localNames[0]);
                        cLLiveVars.remove(localNames[0]);
                    }

                    liveVars.add(name);
                    cLLiveVars.add(name);
                    System.out.println("Lexical read: " + vn + " " + name);

                }

                // Analysis for global reads
                if (currentInstruction instanceof AstGlobalRead) {
                    AstGlobalRead globalRead = (AstGlobalRead) currentInstruction;

                    // If the global variable is $$undefined, then it is being defined the first time
                    // If the variable is defined, then the name will be available at the first basic block
                    // For entry live analysis, we can remove the variable from the live vars at this stage

                    if (globalRead.getGlobalName().equals("global $$undefined")) {
                        int vn = globalRead.getDef();
                        String[] localNames = ir.getLocalNames(globalRead.iIndex(), vn);

                        if (localNames.length > 0) {
                            liveVars.remove(localNames[0]);
                            cLLiveVars.remove(localNames[0]);
                        }
                    }
                }

                // Analysis for lexical writes
                if (currentInstruction instanceof AstLexicalWrite) {
                    AstLexicalWrite lexicalWrite = (AstLexicalWrite) currentInstruction;

                    // Step 1: get the access that is being written
                    AstLexicalAccess.Access access = lexicalWrite.getAccess(0);

                    // Step 2: get the assigned variable name and the assigned value number
                    int assignedValueNumber = access.valueNumber; // equivalent to lexicalWrite.getUse(0)
                    String assignedVar = access.variableName;

                    // Step 3: Remove the assigned variable from the live set because it is reassigning the variable
                    liveVars.remove(assignedVar);
                    cLLiveVars.remove(assignedVar);

                    // Step 4: Get the name of the assigned value
                    String valString = symbolTable.getValueString(assignedValueNumber);
                    String[] localNames = ir.getLocalNames(lexicalWrite.iIndex(), assignedValueNumber);

                    // Step 5: Add the assigned value to the live set
                    if (localNames.length > 0) {
                        liveVars.add(localNames[0]);
                        cLLiveVars.add(localNames[0]);
                    } else {
                        // If the assigned value is a constant, it will have a :# in the value string
                        // So, we don't need to consider the vns of constants
                        if (!valString.contains(":#")) {
                            undeterminedVars.add(access.valueNumber);
                            cLUndeterminedVars.add(access.valueNumber);
                        }
                    }
                }

                currentLineEntryLiveSet.setUndeterminedVns(new HashSet<>(undeterminedVars));
                currentLineEntryLiveSet.setUndeterminedVns(new HashSet<>(cLUndeterminedVars));
                currentLineEntryLiveSet.setLiveVars(new HashSet<>(liveVars));
                currentLineEntryLiveSet.setLiveVars(new HashSet<>(cLLiveVars));

                System.out.println("Current line " + currentLineNumber + " entry live set: " + currentLineEntryLiveSet);
                entryLiveSetMap.put(currentLineNumber, currentLineEntryLiveSet);

            }
        }
        System.out.println("Final entry live set: " + entryLiveSetMap);


//        for (int i = instructions.length - 1; i >= 0; i--) {
//            SSAInstruction currentInstruction = instructions[i];
//
//            if (currentInstruction != null) {
//                System.out.println(currentInstruction.iIndex() + " ---  " + currentInstruction + " --- " + currentInstruction.getClass());
//                if (currentInstruction instanceof AstGlobalRead) {
//                    AstGlobalRead globalRead = (AstGlobalRead) currentInstruction;
//
//                    // If the global variable is undefined, then it is being defined the first time
//                    if (globalRead.getGlobalName().equals("global $$undefined")) {
//                        int vn = globalRead.getDef();
//                        String[] localNames = ir.getLocalNames(globalRead.iIndex(), vn);
//                        System.out.println("Global read: " + vn + " " + Arrays.toString(localNames));
//                        if (localNames.length > 0) {
//                            liveVars.remove(localNames[0]);
//                        }
//                    }
//                }
//
//                if (currentInstruction instanceof JavaScriptInvoke) {
//                    JavaScriptInvoke jsInvoke = (JavaScriptInvoke) currentInstruction;
//                    if (jsInvoke.hasDef()) {
//                        int vn = jsInvoke.getDef();
//                        String[] localNames = ir.getLocalNames(jsInvoke.iIndex(), vn);
//                        if (localNames.length > 0) {
//                            if (undeterminedVars.contains(vn)) {
//                                liveVars.add(localNames[0]);
//                            }
//                            // else {
//                            // Remove if it is a new assignment
////                                liveVars.remove(localNames[0]);
////                        }
//                        }
//                        undeterminedVars.remove(vn);
//                    }
//                }
//
//                /*
//                if (currentInstruction instanceof SSAConditionalBranchInstruction) {
//                    SSAConditionalBranchInstruction condBranch = (SSAConditionalBranchInstruction) currentInstruction;
//                    int n1 = condBranch.getUse(0);
//                    int n2 = condBranch.getUse(1);
//
//                    String[] n1Names = ir.getLocalNames(currentInstruction.iIndex(), n1);
//                    String[] n2Names = ir.getLocalNames(currentInstruction.iIndex(), n2);
//
//                    if (n1Names.length > 0) {
//                        liveVars.add(n1Names[0]);
//                    } else {
//                        if (!symbolTable.getValueString(n1).contains(":#")) undeterminedVars.add(n1);
//                    }
//
//                    if (n2Names.length > 0) {
//                        liveVars.add(n2Names[0]);
//                    } else {
//                        if (!symbolTable.getValueString(n2).contains(":#")) undeterminedVars.add(n2);
//                    }
//
//                    System.out.println("Conditional branch: " + condBranch.getNumberOfUses());
//                }
//                */
//
//
//                if (currentInstruction instanceof JavaScriptPropertyWrite) {
//                    JavaScriptPropertyWrite jsWrite = (JavaScriptPropertyWrite) currentInstruction;
//                    int objectRef = jsWrite.getObjectRef();
//                    int memberRef = jsWrite.getMemberRef();
//                }
//
//                if (currentInstruction instanceof SSABinaryOpInstruction) {
//                    SSABinaryOpInstruction binaryOpInstruction = (SSABinaryOpInstruction) currentInstruction;
//
//                    int assignedVn = binaryOpInstruction.getDef();
//                    int n1 = binaryOpInstruction.getUse(0);
//                    int n2 = binaryOpInstruction.getUse(1);
//
//
//                    String[] n1Names = ir.getLocalNames(currentInstruction.iIndex(), n1);
//                    String[] n2Names = ir.getLocalNames(currentInstruction.iIndex(), n2);
//                    String[] defNames = ir.getLocalNames(currentInstruction.iIndex(), assignedVn);
//
//                    // Removed defined variables first
//                    // e.g. mul = mul * 2;
//                    // since mul is read, it should be live
//                    if (defNames.length > 0) {
//                        liveVars.remove(defNames[0]);
//                    }
//
//                    // Add the name of the JS variable(s) to the set if it exists
//                    // Add the name to the live var set
//                    if (n1Names.length > 0) {
//                        liveVars.add(n1Names[0]);
//                    } else {
//                        if (!symbolTable.getValueString(n1).contains(":#")) undeterminedVars.add(n1);
//                    }
//
//                    if (n2Names.length > 0) {
//                        liveVars.add(n2Names[0]);
//                    } else {
//                        if (!symbolTable.getValueString(n2).contains(":#")) undeterminedVars.add(n2);
//                    }
//
//
//                    // Remove this anyway because either assignedVn is an intermediate variable
//                    // or it is an assigned variable that is being overwritten
//                    undeterminedVars.remove(assignedVn);
//
//                }
//
//                // In case of a global variable write,
//                // e.g. if sum is declared outside the current block, but set inside the block
//                // var sum;
//                // void onClick() {
//                //    sum = 0; // This assignment is a lexical write
//                // }
//                if (currentInstruction instanceof AstLexicalWrite) {
//                    AstLexicalWrite lexicalWrite = (AstLexicalWrite) currentInstruction;
//
//                    // get the assigned variable name
//                    String assignedVar = lexicalWrite.getAccess(0).variableName;
//                    liveVars.remove(assignedVar);
//
//                    // getUse(0) is the value being assigned (RHS)
//                    int readVn = lexicalWrite.getUse(0);
//                    String valString = symbolTable.getValueString(readVn);
//                    String[] localNames = ir.getLocalNames(lexicalWrite.iIndex(), readVn);
//
//                    if (localNames.length > 0) {
//                        liveVars.add(localNames[0]);
//                    } else {
//                        if (!valString.contains(":#")) {
//                            undeterminedVars.add(readVn);
//                        }
//                    }
//                }
//                if (currentInstruction instanceof AstLexicalRead) {
//                    AstLexicalRead lexicalRead = (AstLexicalRead) currentInstruction;
//                    AstLexicalAccess.Access access = lexicalRead.getAccess(0);
//
//                    int vn = access.valueNumber;
//                    String name = access.variableName;
//
//                    // Live: [15]
//                    // This block: vn = 15, name = sum
//                    // Add remaining live variables to the live set
//                    if (undeterminedVars.contains(vn)) {
//                        liveVars.add(name);
//
//                        // Better way would be to track if `check` instruction is present
//                        undeterminedVars.remove(vn);
//                    }
//                    System.out.println("Lexical read: " + vn + " " + name);
//                }
//
//
//                if (currentInstruction.toString().contains("getfield < JavaScriptLoader,")) {
//                    if (currentInstruction instanceof SSAGetInstruction) {
//                        SSAGetInstruction getInstruction = (SSAGetInstruction) currentInstruction;
//
//                        if (getInstruction.getNumberOfUses() > 0) {
//                            int objectRef = getInstruction.getUse(0);
//                            undeterminedVars.add(objectRef);
//                        }
//                        System.out.println("Get instruction field name: " + getInstruction.getDeclaredField().getName());
//                    }
//                }
//
//                // For getField instructions
////                if (currentInstruction instanceof SSAGetInstruction) {
////                    SSAGetInstruction getInstruction = (SSAGetInstruction) currentInstruction;
////                    if (getInstruction.getNumberOfUses() > 0) {
////                        int objectRef = getInstruction.getUse(0);
////                        undeterminedVars.add(objectRef);
////                    }
////                    System.out.println("Get instruction field name: " + getInstruction.getDeclaredField().getName());
////                }
//
//                if (currentInstruction.hasDef()) {
//                }
//                System.out.println("To determine live: " + undeterminedVars);
//                System.out.println("Live vars: " + liveVars);
//            }
//        }
        return entryLiveSetMap.get(entryLiveSetMap.keySet().stream()
                .min(Integer::compareTo)
                .orElseThrow()).getLiveVars();
    }
}