import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.*;

public class AnalysisTransformer extends SceneTransformer {
    static CallGraph cg;

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        cg = Scene.v().getCallGraph();

        String analysisMode = "INTER";
        if (analysisMode.equals("INTRA")) {
            intraMonomorphism();
        } else {
            var entryPoints = Scene.v().getEntryPoints();
            assert (entryPoints.size() == 1);
            SootMethod entryMethod = entryPoints.get(0);
            handleMethod(entryMethod);
        }
    }

    void handleMethod(SootMethod method) {
        Analysis analysis = new Analysis();
        analysis.setCallGraph(cg);
        analysis.setAnalysisMode("INTER"); // (INTER/INTRA) Make sure to check the mode before running the program

        var body = method.getActiveBody();
        analysis.run(body);

        printMethodStats();

    }

    void intraMonomorphism() {
        // for each method and call analysis.run(body) for each method. So not just the
        // entry point but all methods in the program. This is to be used when
        // ANALYSIS_MODE is INTRA.

        Analysis analysis = new Analysis();
        analysis.setCallGraph(cg);
        analysis.setAnalysisMode("INTRA"); // (INTER/INTRA) Make sure to check the mode before running the
                                           // program

        int totalVirtualCalls = 0;

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {

            if (sootClass.isPhantom() || sootClass.isInterface())
                continue;

            List<SootMethod> methodsToAnalyze = new ArrayList<>(sootClass.getMethods());
            for (SootMethod method : methodsToAnalyze) {
                System.out.println("Analyzing method: " + method.getSignature());

                if (method.isConcrete() && method.hasActiveBody() && !method.isPhantom()) {
                    Body body = method.getActiveBody();

                    for (Unit unit : body.getUnits()) {
                        Stmt stmt = (Stmt) unit;
                        if (stmt.containsInvokeExpr()) {
                            InvokeExpr expr = stmt.getInvokeExpr();
                            if (expr instanceof JVirtualInvokeExpr
                                    && expr.getMethodRef().getDeclaringClass().isApplicationClass()) {
                                totalVirtualCalls++;
                            }
                        }
                    }

                    // analysis.run(body);
                    System.out.println("Finished analyzing method: " + method.getSignature());
                }
            }
        }

        // Depending on how you manage your stats, you can print them here
        System.out.println("Mapped total intraprocedural virtual call sites: " + totalVirtualCalls);
        printMethodStats();
    }

    void printMethodStats() {
        System.out.println("Total virtual calls before transformation: " + Analysis.totalVirtualCalls);
        System.out
                .println("Total virtual calls transformed to static calls: " + Analysis.totalVirtualCallsToStaticCalls);
        System.out.println("Virtual call to static call transformation percentage: " +
                (Analysis.totalVirtualCallsToStaticCalls * 100.0 / Analysis.totalVirtualCalls) + "%");
    }
}