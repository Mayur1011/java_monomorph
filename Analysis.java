import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.*;

public class Analysis {
    private static final Obj UNKNOWN_ALLOC = new Obj(-1, true);
    private static final Obj NULL_STRING = new Obj(-2, false, true);
    private static final Map<Unit, Obj> allocSiteByUnit = new HashMap<>();
    private static CallGraph cg;
    private static final int CONTEXT_DEPTH = 3;

    // if we see the same context again instead of running the while pts analysis
    // again we can use previous inset and outset.
    private static final Map<MethodContext, MethodSummary> summaryCache = new HashMap<>();

    private static final Set<MethodContext> activeAnalyses = new HashSet<>();

    private static String ANALYSIS_MODE = "INTER"; // "INTRA" or "INTER"
    // Metrics for virtual call to static call transformation
    public static int totalVirtualCalls = 0;
    public static int totalVirtualCallsToStaticCalls = 0;

    private static final class CallContext {
        private final List<Unit> callString; // can't i just put line-numbers here?
        private static final CallContext EMPTY = new CallContext(Collections.emptyList());

        private CallContext(List<Unit> callString) {
            this.callString = Collections.unmodifiableList(callString);
        }

        // this function will make sure that we are always maintaining the k depth of
        // call string. if the current call string is [C, B] and we are extending
        // with a new call site D, the new call string will be [D, C] (B is dropped
        // because of 2-depth).
        CallContext extend(Unit callSite) {
            List<Unit> next = new ArrayList<>();
            next.add(callSite);
            int copyCount = Math.min(callString.size(), CONTEXT_DEPTH - 1);
            next.addAll(callString.subList(0, copyCount));
            return new CallContext(next);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CallContext))
                return false;
            return callString.equals(((CallContext) o).callString);
        }

        @Override
        public int hashCode() {
            return callString.hashCode();
        }

        @Override
        public String toString() {
            if (callString.isEmpty())
                return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < callString.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                Unit u = callString.get(i);
                sb.append("L").append(u.getJavaSourceStartLineNumber());
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private static final class MethodContext {
        final SootMethod method;
        final CallContext context; // this is call string. (for same method call string can be diff.)

        MethodContext(SootMethod method, CallContext context) {
            this.method = method;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MethodContext))
                return false;
            MethodContext mc = (MethodContext) o;
            return Objects.equals(method, mc.method) && Objects.equals(context, mc.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, context);
        }

        @Override
        public String toString() {
            return method.getSignature() + "@" + context;
        }
    }

    private static final class MethodSummary {
        final Map<Unit, PointsToData> inMap;
        final Map<Unit, PointsToData> outMap;

        MethodSummary(Map<Unit, PointsToData> inMap, Map<Unit, PointsToData> outMap) {
            this.inMap = inMap;
            this.outMap = outMap;
        }
    }

    private static final class Obj {
        private final int id;
        private final boolean unknown;
        private final boolean isNullString;
        private SootMethod method = null;
        private final Type type;

        Obj(int id, boolean unknown) {
            this.id = id;
            this.unknown = unknown;
            this.isNullString = false;
            this.type = null;
        }

        Obj(int id, boolean unknown, Type type) {
            this.id = id;
            this.unknown = unknown;
            this.isNullString = false;
            this.type = type;
        }

        Obj(int id, boolean unknown, boolean isNullString) {
            this.id = id;
            this.unknown = unknown;
            this.isNullString = isNullString;
            this.type = null;
        }

        public void setMethod(SootMethod method) {
            this.method = method;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Obj)) {
                return false;
            }
            Obj other = (Obj) obj;
            return id == other.id && unknown == other.unknown && isNullString == other.isNullString;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, unknown, isNullString);
        }

        @Override
        public String toString() {
            if (unknown) {
                return "UNKNOWN";
            }
            if (isNullString) {
                return "NULL";
            }
            return "O_" + id;
        }
    }

    private static final class LocalKey {
        private final Value local;
        private final SootMethod method;

        private LocalKey(Value local, SootMethod method) {
            this.local = local;
            this.method = method;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocalKey)) {
                return false;
            }
            LocalKey other = (LocalKey) obj;
            return Objects.equals(local.toString(), other.local.toString()) && Objects.equals(method, other.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(local.toString(), method);
        }

        @Override
        public String toString() {
            if (local instanceof StaticFieldRef) {
                return ((StaticFieldRef) local).getField().getDeclaringClass().getName() + "."
                        + ((StaticFieldRef) local).getField().getName();
            }
            return local.toString() + "@" + method.getName();
        }
    }

    private static final class FieldKey {
        private final Obj site;
        private final SootField field;

        private FieldKey(Obj site, SootField field) {
            this.site = site;
            this.field = field;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FieldKey)) {
                return false;
            }
            FieldKey other = (FieldKey) obj;
            return Objects.equals(site, other.site) && Objects.equals(field, other.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(site, field);
        }

        @Override
        public String toString() {
            return site.toString() + "." + field.getName();
        }
    }

    private static final class PointsToData {
        private final Map<LocalKey, Set<Obj>> stack;
        private final Map<FieldKey, Set<Obj>> heap;

        private PointsToData() {
            this.stack = new HashMap<>();
            this.heap = new HashMap<>();
        }

        private static PointsToData empty() {
            return new PointsToData();
        }

        private PointsToData copy() {
            PointsToData copy = new PointsToData();
            for (Map.Entry<LocalKey, Set<Obj>> entry : stack.entrySet()) {
                copy.stack.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            for (Map.Entry<FieldKey, Set<Obj>> entry : heap.entrySet()) {
                copy.heap.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return copy;
        }

        private PointsToData union(PointsToData other) {
            PointsToData result = new PointsToData();
            Set<LocalKey> allLocals = new HashSet<>();
            allLocals.addAll(this.stack.keySet());
            allLocals.addAll(other.stack.keySet());
            for (LocalKey local : allLocals) {
                Set<Obj> merged = new HashSet<>();
                Set<Obj> left = Collections.emptySet();
                Set<Obj> right = Collections.emptySet();
                if (this.stack.containsKey(local)) {
                    left = this.stack.getOrDefault(local, setOf(UNKNOWN_ALLOC));
                }
                if (other.stack.containsKey(local)) {
                    right = other.stack.getOrDefault(local, setOf(UNKNOWN_ALLOC));
                }
                merged.addAll(left);
                merged.addAll(right);
                result.setVar(local, merged);
            }

            Set<FieldKey> allFields = new HashSet<>();
            allFields.addAll(this.heap.keySet());
            allFields.addAll(other.heap.keySet());
            for (FieldKey key : allFields) {
                Set<Obj> merged = new HashSet<>();
                Set<Obj> left = Collections.emptySet();
                Set<Obj> right = Collections.emptySet();
                if (this.heap.containsKey(key)) {
                    left = this.heap.getOrDefault(key, setOf(NULL_STRING));
                }
                if (other.heap.containsKey(key)) {
                    right = other.heap.getOrDefault(key, setOf(NULL_STRING));
                }
                merged.addAll(left);
                merged.addAll(right);
                result.setField(key.site, key.field, merged);
            }
            return result;
        }

        private Set<Obj> getVar(LocalKey local) {
            return stack.getOrDefault(local, setOf(UNKNOWN_ALLOC));
        }

        private void setVar(LocalKey local, Set<Obj> sites) {
            if (sites == null || sites.isEmpty()) {
                stack.remove(local);
                return;
            }
            Set<Obj> newSites = new HashSet<>(sites);
            stack.put(local, newSites);
        }

        private Set<Obj> getField(Obj site, SootField field) {
            return heap.getOrDefault(new FieldKey(site, field), Collections.emptySet());
        }

        private void setField(Obj site, SootField field, Set<Obj> sites) {
            if (sites == null || sites.isEmpty()) {
                heap.remove(new FieldKey(site, field));
            } else {
                heap.put(new FieldKey(site, field), new HashSet<>(sites));
            }
        }

        private void addField(Obj site, SootField field, Set<Obj> sites) {
            FieldKey key = new FieldKey(site, field);
            Set<Obj> existing = heap.computeIfAbsent(key, k -> new HashSet<>());
            existing.addAll(sites);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PointsToData)) {
                return false;
            }
            PointsToData other = (PointsToData) obj;
            return Objects.equals(stack, other.stack)
                    && Objects.equals(heap, other.heap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack, heap);
        }

    }

    public void setCallGraph(CallGraph callGraph) {
        cg = callGraph;
    }

    public void setAnalysisMode(String mode) {
        if (mode.equals("INTRA") || mode.equals("INTER")) {
            ANALYSIS_MODE = mode;
        } else {
            throw new IllegalArgumentException("Invalid analysis mode: " + mode);
        }
        totalVirtualCalls = 0;
        totalVirtualCallsToStaticCalls = 0;
    }

    protected void run(Body body) {
        UnitGraph graph = new BriefUnitGraph(body);
        Map<Unit, Integer> unitToIndex = buildUnitToIndex(body);
        Map<Unit, PointsToData> ptsIn = new HashMap<>();
        Map<Unit, PointsToData> ptsOut = new HashMap<>();
        runPointsToAnalysis(graph, unitToIndex, ptsIn, ptsOut, null, body.getMethod(), CallContext.EMPTY);
    }

    private static Map<Unit, Integer> buildUnitToIndex(Body body) {
        Map<Unit, Integer> map = new HashMap<>();
        for (Unit u : body.getUnits()) {
            map.put(u, u.getJavaSourceStartLineNumber());
        }
        return map;
    }

    private static void runPointsToAnalysis(
            UnitGraph graph,
            Map<Unit, Integer> unitToIndex,
            Map<Unit, PointsToData> inMap,
            Map<Unit, PointsToData> outMap,
            PointsToData initialState,
            SootMethod currentMethod,
            CallContext currentContext) {

        MethodContext mc = new MethodContext(currentMethod, currentContext);
        activeAnalyses.add(mc);

        Deque<Unit> worklist = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();

        for (Unit unit : graph) {
            inMap.put(unit, PointsToData.empty());
            outMap.put(unit, PointsToData.empty());
            worklist.add(unit);
        }

        while (!worklist.isEmpty()) {
            Unit unit = worklist.removeFirst();
            PointsToData inState = mergePointsTo(graph.getPredsOf(unit), outMap, visited, initialState);
            PointsToData outState = transferPointsTo(unit, inState, unitToIndex, inMap, outMap, currentMethod,
                    currentContext);
            visited.add(unit);
            if (!outState.equals(outMap.get(unit))) {
                inMap.put(unit, inState);
                outMap.put(unit, outState);
                for (Unit succ : graph.getSuccsOf(unit)) {
                    worklist.add(succ);
                }
            } else {
                inMap.put(unit, inState);
            }
        }
        activeAnalyses.remove(mc);
        summaryCache.put(mc, new MethodSummary(inMap, outMap));
        // debug(currentMethod.getDeclaringClass().getName() + ":" +
        // currentMethod.getName());
        // for (Unit unit : graph) {
        // debug("--------------------------------------------------------------------------");
        // debug("Line [" + unit.getJavaSourceStartLineNumber() + "]: " + unit + "{" +
        // unit.getClass().getTypeName()
        // + "}");
        // printPointsToData("IN", inMap.get(unit));
        // printPointsToData("OUT", outMap.get(unit));
        // }
        // debug("======================================================================\n\n\n");
    }

    private static PointsToData mergePointsTo(
            List<Unit> preds,
            Map<Unit, PointsToData> outMap,
            Set<Unit> visited,
            PointsToData initialState) {
        if (preds == null || preds.isEmpty()) {
            if (initialState != null) {
                return initialState;
            }
            return PointsToData.empty();
        }
        PointsToData merged = null;
        for (Unit pred : preds) {
            if (!visited.contains(pred)) {
                continue;
            }
            PointsToData state = outMap.get(pred);
            if (merged == null) {
                merged = state.copy();
            } else {
                merged = merged.union(state);
            }
        }
        return merged == null ? PointsToData.empty() : merged;
    }

    private static void processInvokeExprIntra(
            PointsToData state,
            InvokeExpr invokeExpr,
            SootMethod currentMethod) {
        Queue<Obj> queue = new ArrayDeque<>();
        System.out.println("[INTRA]: Processing invoke: " + invokeExpr);

        // Get the allocSite (Obj) for the base
        if (invokeExpr instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr instanceInvoke = (InstanceInvokeExpr) invokeExpr;
            Value base = instanceInvoke.getBase();
            if (base instanceof Local) {
                Set<Obj> basePts = state.getVar(new LocalKey((Local) base, currentMethod));
                for (Obj site : basePts) {
                    if (!site.equals(UNKNOWN_ALLOC)) {
                        queue.add(site);
                    }
                }
            }
        }

        // Get the allocSite (Obj) for each argument
        for (Value arg : invokeExpr.getArgs()) {
            if (arg instanceof Local) {
                Set<Obj> argPts = state.getVar(new LocalKey((Local) arg, currentMethod));
                for (Obj site : argPts) {
                    if (!site.equals(UNKNOWN_ALLOC)) {
                        queue.add(site);
                    }
                }
            }
        }

        Set<Obj> processed = new HashSet<>();
        while (!queue.isEmpty()) {
            Obj site = queue.poll();
            if (processed.contains(site)) {
                continue;
            }
            processed.add(site);

            // For the current site (O1), get all the AllocSite that O1.field points to.
            Map<SootField, Set<Obj>> fieldsToProcess = new HashMap<>();
            Set<SootField> fields = new HashSet<>();
            for (FieldKey key : state.heap.keySet()) {
                if (key.site.equals(site)) {
                    fields.add(key.field);
                }
            }
            for (SootField field : fields) {
                Set<Obj> fieldPts = state.getField(site, field);
                if (!fieldPts.isEmpty()) {
                    fieldsToProcess.put(field, new HashSet<>(fieldPts));
                }
            }

            // We remove all existing points-to facts for O1.field here
            List<FieldKey> keysToRemove = new ArrayList<>();
            for (FieldKey key : state.heap.keySet()) {
                if (key.site.equals(site)) {
                    keysToRemove.add(key);
                }
            }

            for (FieldKey key : keysToRemove) {
                state.heap.remove(key);
            }

            for (Map.Entry<SootField, Set<Obj>> entry : fieldsToProcess.entrySet()) {
                for (Obj targetSite : entry.getValue()) {
                    if (!targetSite.equals(UNKNOWN_ALLOC) && !processed.contains(targetSite)) {
                        queue.add(targetSite);
                    }
                }
            }
        }
    }

    private static void processInvokeExprIntraStatic(
            PointsToData state,
            InvokeExpr invokeExpr,
            SootMethod currentMethod,
            Unit curr,
            CallContext currentContext) {
        Queue<Obj> queue = new ArrayDeque<>();

        if (!currentMethod.hasActiveBody() || currentMethod.isConstructor() || !currentMethod.isConcrete())
            return;
        if (!invokeExpr.getMethodRef().getDeclaringClass().isApplicationClass())
            return;
        // Check if the target of the curr invoke statement is exactly one.
        int count = 0;
        Iterator<Edge> targets = cg.edgesOutOf((Stmt) curr);
        SootMethod targetMethod = null;
        while (targets.hasNext()) {
            Edge edge = targets.next();
            targetMethod = edge.tgt().method();
            ++count;
        }
        if (invokeExpr instanceof JVirtualInvokeExpr) {
            totalVirtualCalls++;
        }
        if (invokeExpr instanceof JVirtualInvokeExpr && targetMethod != null
                && targetMethod.hasActiveBody() && !targetMethod.isConstructor()) {
            CallContext childContext = currentContext.extend(curr);
            performCodeTransformation(invokeExpr, curr, currentMethod, state, targetMethod, childContext);
            if (curr instanceof InvokeStmt) {
                invokeExpr = ((InvokeStmt) curr).getInvokeExpr();
            } else if (curr instanceof AssignStmt) {
                invokeExpr = ((AssignStmt) curr).getRightOp() instanceof InvokeExpr
                        ? (InvokeExpr) ((AssignStmt) curr).getRightOp()
                        : invokeExpr;
            }
        } else {
        }

        // Get the allocSite (Obj) for the base
        if (invokeExpr instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr instanceInvoke = (InstanceInvokeExpr) invokeExpr;
            Value base = instanceInvoke.getBase();
            if (base instanceof Local) {
                Set<Obj> basePts = state.getVar(new LocalKey((Local) base, currentMethod));
                for (Obj site : basePts) {
                    if (!site.equals(UNKNOWN_ALLOC)) {
                        queue.add(site);
                    }
                }
            }
        }

        // Get the allocSite (Obj) for each argument
        for (Value arg : invokeExpr.getArgs()) {
            if (arg instanceof Local) {
                Set<Obj> argPts = state.getVar(new LocalKey((Local) arg, currentMethod));
                for (Obj site : argPts) {
                    if (!site.equals(UNKNOWN_ALLOC)) {
                        queue.add(site);
                    }
                }
            }
        }

        Set<Obj> processed = new HashSet<>();
        while (!queue.isEmpty()) {
            Obj site = queue.poll();
            if (processed.contains(site)) {
                continue;
            }
            processed.add(site);

            // For the current site (O1), get all the AllocSite that O1.field points to.
            Map<SootField, Set<Obj>> fieldsToProcess = new HashMap<>();
            Set<SootField> fields = new HashSet<>();
            for (FieldKey key : state.heap.keySet()) {
                if (key.site.equals(site)) {
                    fields.add(key.field);
                }
            }
            for (SootField field : fields) {
                Set<Obj> fieldPts = state.getField(site, field);
                if (!fieldPts.isEmpty()) {
                    fieldsToProcess.put(field, new HashSet<>(fieldPts));
                }
            }

            // We remove all existing points-to facts for O1.field here
            List<FieldKey> keysToRemove = new ArrayList<>();
            for (FieldKey key : state.heap.keySet()) {
                if (key.site.equals(site)) {
                    keysToRemove.add(key);
                }
            }

            for (FieldKey key : keysToRemove) {
                state.heap.remove(key);
            }

            for (Map.Entry<SootField, Set<Obj>> entry : fieldsToProcess.entrySet()) {
                for (Obj targetSite : entry.getValue()) {
                    if (!targetSite.equals(UNKNOWN_ALLOC) && !processed.contains(targetSite)) {
                        queue.add(targetSite);
                    }
                }
            }
        }
    }

    private static PointsToData processInvokeExprInter(
            PointsToData state,
            InvokeExpr invokeExpr,
            SootMethod currentMethod,
            Unit curr,
            CallContext currentContext) {
        if (!currentMethod.hasActiveBody())
            return state;
        if (!invokeExpr.getMethodRef().getDeclaringClass().isApplicationClass())
            return state;

        int count = 0;
        SootMethod targetMethod = null;
        Iterator<Edge> targets = cg.edgesOutOf((Stmt) curr);
        while (targets.hasNext()) {
            targets.next();
            ++count;
        }

        debug("[INTER]: Processing invoke: " + invokeExpr + " in method: " + currentMethod.getSignature() + " at line: "
                + curr.getJavaSourceStartLineNumber() + " with context: " + currentContext + " and " + count
                + " targets.");
        CallContext childContext = currentContext.extend(curr);

        if (invokeExpr instanceof JVirtualInvokeExpr) {
            totalVirtualCalls++;
        }

        PointsToData mergedState = PointsToData.empty();
        Set<Obj> mergedReturnPts = new HashSet<>();
        targets = cg.edgesOutOf((Stmt) curr);
        while (targets.hasNext()) {
            Edge edge = targets.next();
            SootMethod method = edge.tgt().method();
            targetMethod = method;
            if (!method.hasActiveBody())
                continue;

            PointsToData originalState = state.copy();
            PointsToData calleeState = originalState.copy();
            SootMethod methodToAnalyze = targetMethod;
            if (invokeExpr instanceof JVirtualInvokeExpr) {
                performCodeTransformation(invokeExpr, curr, currentMethod, state, targetMethod, childContext);
                if (curr instanceof InvokeStmt)
                    invokeExpr = ((InvokeStmt) curr).getInvokeExpr();
                else
                    invokeExpr = ((AssignStmt) curr).getRightOp() instanceof InvokeExpr
                            ? (InvokeExpr) ((AssignStmt) curr).getRightOp()
                            : invokeExpr;
                if (invokeExpr instanceof JStaticInvokeExpr) {
                    methodToAnalyze = invokeExpr.getMethod();
                    debug("[INTER]: Virtual call at " + curr + " resolved to static method: "
                            + methodToAnalyze.getSignature());
                }
            }
            if (!invokeExpr.getMethod().hasActiveBody()) {
                // debug("[INTER]: Skipping invoke: " + invokeExpr + " with target method: " +
                // method.getSignature());
                continue;
            }
            // debug("[INTER]: Processing invoke: " + invokeExpr + " with target method: "
            // + invokeExpr.getMethod().getSignature());
            Body body = methodToAnalyze.retrieveActiveBody();
            UnitGraph graph = new BriefUnitGraph(body);
            Map<Unit, Integer> unitToIndex = buildUnitToIndex(body);
            Map<Unit, PointsToData> calleeIn = new HashMap<>();
            Map<Unit, PointsToData> calleeOut = new HashMap<>();
            calleeState.stack.clear();
            replaceLocalWithArgs(originalState, invokeExpr, currentMethod, curr, calleeState);

            MethodContext calleeMC = new MethodContext(methodToAnalyze, childContext);
            MethodSummary summary;
            if (summaryCache.containsKey(calleeMC)) {
                // Re-use previously computed summary for this (callee, context)
                summary = summaryCache.get(calleeMC);
            } else if (activeAnalyses.contains(calleeMC)) {
                // Recursive call — return empty (conservative approximation)
                debug("[WARN] Recursive call detected for " + calleeMC + ", skipping.");
                continue;
            } else {
                runPointsToAnalysis(graph, unitToIndex, calleeIn, calleeOut, calleeState, methodToAnalyze,
                        childContext);
                summary = summaryCache.get(calleeMC); // populated by runPointsToAnalysis
                if (summary == null)
                    summary = new MethodSummary(calleeIn, calleeOut);
            }
            for (Unit unit : graph.getTails()) {
                PointsToData calleeRetState = summary.outMap.getOrDefault(unit, null);
                if (calleeRetState == null)
                    continue;
                if (unit instanceof ReturnStmt) {
                    if (((ReturnStmt) unit).getOp() instanceof Local) {
                        Local ret = (Local) ((ReturnStmt) unit).getOp();
                        Set<Obj> returnPts = calleeRetState.getVar(new LocalKey(ret, methodToAnalyze));
                        mergedReturnPts.addAll(returnPts);
                    }
                }
                // replaceParamsWithLocal(calleeRetState, method, invokeExpr,
                // currentMethod,body);
                mergedState = mergedState.union(calleeRetState);
            }
        }
        state.heap.putAll(mergedState.heap);
        if (curr instanceof AssignStmt) {
            Local lhs = (Local) ((AssignStmt) curr).getLeftOp();
            LocalKey leftKey = new LocalKey((Local) lhs, currentMethod);
            state.setVar(leftKey, mergedReturnPts);
        }
        return state;
    }

    private static void performCodeTransformation(
            InvokeExpr invokeExpr,
            Unit unit,
            SootMethod currentMethod,
            PointsToData state,
            SootMethod targetMethod,
            CallContext currentContext) {
        // Replace virtual calls with static calls.

        // check which virtual calls can be monomorphic based on the pts analysis.
        Local base = (Local) ((JVirtualInvokeExpr) invokeExpr).getBase();
        LocalKey baseKey = new LocalKey(base, currentMethod);
        Set<Obj> basePts = state.stack.getOrDefault(baseKey, setOf(UNKNOWN_ALLOC));
        if (basePts.contains(UNKNOWN_ALLOC) || basePts.contains(NULL_STRING)) {
            return;
        }
        Type baseType = basePts.iterator().next().type;
        for (Obj site : basePts) {
            if (!site.type.equals(baseType)) {
                return;
            }
        }
        SootClass baseClass = Scene.v().getSootClass(baseType.toString());

        // if we found the call to be monomorphic replace the virtual call with a static
        // call.

        SootMethod staticMethod = getStaticMethodSignature(invokeExpr, baseType, currentContext);
        Body body;
        SootMethod found = baseClass.getMethodUnsafe(staticMethod.getSubSignature());
        if (found == null) {
            baseClass.addMethod(staticMethod);
            body = prepareBody(staticMethod, targetMethod.retrieveActiveBody());
            staticMethod.setActiveBody(body);
            found = staticMethod;
        } else {
            body = found.retrieveActiveBody();
        }
        List<Value> newArgs = new ArrayList<>(invokeExpr.getArgs());
        newArgs.add(base);
        StaticInvokeExpr staticInvoke = Jimple.v().newStaticInvokeExpr(found.makeRef(), newArgs);
        if (unit instanceof InvokeStmt) {
            ((InvokeStmt) unit).setInvokeExpr(staticInvoke);
        } else if (unit instanceof AssignStmt) {
            AssignStmt stmt = (AssignStmt) unit;
            stmt.setRightOp(staticInvoke);
        }
        totalVirtualCallsToStaticCalls++;
    }

    private static Body prepareBody(SootMethod staticMethod, Body originalBody) {
        Body updatedBody = (Body) originalBody.clone();
        PatchingChain<Unit> units = updatedBody.getUnits();
        List<Type> paramTypes = staticMethod.getParameterTypes();
        int totalParams = paramTypes.size();
        for (Unit u : units) {
            if (u instanceof IdentityStmt) {
                IdentityStmt identityStmt = (IdentityStmt) u;
                Value rightOp = identityStmt.getRightOp();
                if (rightOp instanceof ThisRef) {
                    ParameterRef thisParam = new ParameterRef(paramTypes.get(totalParams - 1), totalParams - 1);
                    identityStmt.setRightOp(thisParam);
                }
            }
        }

        // for every invoke statement inside the new static method body copy the CG
        // edges that existed in the original body then add new edges
        // from staticMethod as the source so that processInvokeExpr fucntion can find
        // targets when it analyses this static function afterwards.
        Map<String, Unit> originalSigToUnit = new HashMap<>();
        for (Unit u : originalBody.getUnits()) {
            if (u instanceof Stmt) {
                Stmt s = (Stmt) u;
                if (s.containsInvokeExpr()) {
                    String sig = s.getInvokeExpr().getMethodRef().getSubSignature().getString() + "@"
                            + Integer.toString(u.getJavaSourceStartLineNumber());
                    // debug("[" + u.getJavaSourceStartLineNumber() + "]" + "Original
                    // sig: " + sig);
                    originalSigToUnit.put(sig, u);
                }
            }
        }

        for (Unit u : units) {
            if (!(u instanceof Stmt))
                continue;
            Stmt s = (Stmt) u;
            if (!s.containsInvokeExpr())
                continue;

            String sig = s.getInvokeExpr().getMethodRef().getSubSignature().getString() + "@"
                    + Integer.toString(s.getJavaSourceStartLineNumber());
            Unit originalUnit = originalSigToUnit.get(sig);
            if (originalUnit == null)
                continue;

            Iterator<Edge> originalEdges = cg.edgesOutOf((Stmt) originalUnit);
            while (originalEdges.hasNext()) {
                Edge originalEdge = originalEdges.next();
                SootMethod tgt = originalEdge.tgt().method();
                cg.addEdge(new Edge(staticMethod, s, tgt, Kind.STATIC));
            }
        }

        return updatedBody;
    }

    private static SootMethod getStaticMethodSignature(InvokeExpr invokeExpr, Type baseType,
            CallContext currentContext) {
        SootMethod methodRef = invokeExpr.getMethod();
        ArrayList<Type> paramTypes = new ArrayList<>(methodRef.getParameterTypes());
        paramTypes.add(baseType);
        String context = "_" + currentContext.toString()
                .replaceAll("[\\[\\]]", "") // Remove [ and ]
                .replaceAll(", ", "_"); // Replace ", " with _
        // remove last underscore if present
        if (context.endsWith("_"))
            context = context.substring(0, context.length() - 1);

        int newMethodModifiers = methodRef.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE | Modifier.STATIC;

        SootMethod staticMethod = new SootMethod(
                methodRef.getName() + "_static" + context,
                paramTypes,
                methodRef.getReturnType(),
                newMethodModifiers);
        return staticMethod;
    }

    private static void replaceLocalWithArgs(PointsToData state, InvokeExpr invokeExpr,
            SootMethod currentMethod, Unit u, PointsToData calleeState) {
        SootMethod targetMethod = invokeExpr.getMethod();
        Body body = targetMethod.retrieveActiveBody();
        List<Local> params = body.getParameterLocals();
        List<Value> args = invokeExpr.getArgs();
        if (params.size() != args.size()) {
            return;
        }
        for (int i = 0; i < params.size(); i++) {
            LocalKey param = new LocalKey(params.get(i), targetMethod);
            LocalKey arg = new LocalKey(args.get(i), currentMethod);
            Set<Obj> argPts = state.stack.getOrDefault(arg, setOf(UNKNOWN_ALLOC));
            calleeState.stack.put(param, new HashSet<>(argPts));
            // state.stack.remove(arg);
        }
        if (invokeExpr instanceof JVirtualInvokeExpr || invokeExpr instanceof JSpecialInvokeExpr) {
            LocalKey thisKey = new LocalKey((Local) body.getThisLocal(), body.getMethod());
            Local base = null;
            if (invokeExpr instanceof JVirtualInvokeExpr)
                base = (Local) ((JVirtualInvokeExpr) invokeExpr).getBase();
            else
                base = (Local) ((JSpecialInvokeExpr) invokeExpr).getBase();
            LocalKey baseKey = new LocalKey(base, currentMethod);
            Set<Obj> basePts = state.stack.getOrDefault(baseKey, setOf(UNKNOWN_ALLOC));
            calleeState.stack.put(thisKey, new HashSet<>(basePts));
            // calleeState.stack.remove(baseKey);
        }

    }

    private static PointsToData transferPointsTo(
            Unit unit,
            PointsToData inState,
            Map<Unit, Integer> unitToIndex,
            Map<Unit, PointsToData> inMap,
            Map<Unit, PointsToData> outMap,
            SootMethod currentMethod,
            CallContext currentContext) {
        PointsToData outState = inState.copy();
        if (unit instanceof IdentityStmt) {
            return outState;
        }

        if (unit instanceof InvokeStmt) {
            InvokeStmt invokeStmt = (InvokeStmt) unit;
            InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            if (ANALYSIS_MODE.equals("INTRA")) {
                // processInvokeExprIntra(outState, unit, currentMethod);
                processInvokeExprIntraStatic(outState, invokeExpr, currentMethod, unit, currentContext);
                return outState;
            }
            return processInvokeExprInter(outState, invokeExpr, currentMethod, unit, currentContext);
        }

        if (!(unit instanceof AssignStmt)) {
            return outState;
        }

        AssignStmt stmt = (AssignStmt) unit;
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();

        if (rhs instanceof InvokeExpr) {
            InvokeExpr invokeExpr = (InvokeExpr) rhs;
            if (ANALYSIS_MODE.equals("INTRA")) {
                // processInvokeExprIntra(outState, unit, currentMethod);
                processInvokeExprIntraStatic(outState, invokeExpr, currentMethod, unit, currentContext);
                return outState;
            }
            return processInvokeExprInter(outState, invokeExpr, currentMethod, unit, currentContext);
        }

        if (lhs instanceof StaticFieldRef && rhs instanceof Local) {
            LocalKey left = new LocalKey(lhs, null);
            LocalKey right = new LocalKey((Local) rhs, currentMethod);
            Set<Obj> rhsPts = outState.getVar(right);
            outState.setVar(left, rhsPts);
            return outState;
        }

        if (lhs instanceof Local) {
            LocalKey left = new LocalKey((Local) lhs, currentMethod);
            if (rhs instanceof AnyNewExpr) {
                outState.setVar(left, setOf(getAllocSite(unit, unitToIndex, currentMethod)));
            } else if (rhs instanceof NullConstant) {
                outState.setVar(left, setOf(NULL_STRING));
            } else if (rhs instanceof Local) {
                outState.setVar(left, outState.getVar(new LocalKey((Local) rhs, currentMethod)));
            } else if (rhs instanceof StaticFieldRef) {
                outState.setVar(left, outState.getVar(new LocalKey((StaticFieldRef) rhs, null)));
            } else if (rhs instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) rhs;
                LocalKey base = new LocalKey((Local) fieldRef.getBase(), currentMethod);
                SootField field = fieldRef.getField();
                Set<Obj> basePts = outState.getVar(base);
                Set<Obj> result = new HashSet<>();
                boolean addUnknown = basePts.contains(UNKNOWN_ALLOC);
                boolean addNull = basePts.contains(NULL_STRING);
                for (Obj site : basePts) {
                    if (site.equals(UNKNOWN_ALLOC) || site.equals(NULL_STRING)) {
                        continue;
                    }
                    Set<Obj> heap = outState.getField(site, field);
                    if (heap.isEmpty()) {
                        addUnknown = true;
                    } else {
                        result.addAll(heap);
                    }
                }
                if (addUnknown || result.isEmpty()) {
                    result.add(UNKNOWN_ALLOC);
                }
                if (addNull) {
                    result.add(NULL_STRING);
                }
                outState.setVar(left, result);
            } else if (rhs instanceof Constant) {
                outState.setVar(left, setOf(UNKNOWN_ALLOC));
            } else {
                outState.setVar(left, setOf(UNKNOWN_ALLOC));
            }
            return outState;
        }

        if (lhs instanceof InstanceFieldRef) {
            InstanceFieldRef fieldRef = (InstanceFieldRef) lhs;
            Local base = (Local) fieldRef.getBase();
            SootField field = fieldRef.getField();
            Set<Obj> basePts = outState.getVar(new LocalKey(base, currentMethod));
            if (rhs instanceof Local) {
                Set<Obj> rhsPts = outState.getVar(new LocalKey((Local) rhs, currentMethod));

                boolean strong = basePts.size() == 1 && !basePts.contains(UNKNOWN_ALLOC)
                        && !basePts.contains(NULL_STRING);
                if (strong) {
                    Obj site = basePts.iterator().next();
                    outState.setField(site, field, rhsPts);
                } else {
                    for (Obj site : basePts) {
                        if (!site.equals(UNKNOWN_ALLOC) && !site.equals(NULL_STRING)) {
                            outState.addField(site, field, rhsPts);
                        }
                    }
                }
            }
        }

        return outState;
    }

    private static Set<Obj> setOf(Obj site) {
        Set<Obj> set = new HashSet<>();
        set.add(site);
        return set;
    }

    private static Obj getAllocSite(Unit unit, Map<Unit, Integer> unitToIndex, SootMethod method) {
        Obj site = allocSiteByUnit.get(unit);
        if (site == null) {
            Type objectType = ((AssignStmt) unit).getRightOp().getType();
            debug("Creating alloc site for unit: " + unit + " with type: " + objectType);
            int id = unitToIndex.get(unit);
            site = new Obj(id, false, objectType);
            site.setMethod(method);
            allocSiteByUnit.put(unit, site);
        }
        return site;
    }

    private static String allocSiteStr(Obj site) {
        if (site.unknown)
            return "UNKNOWN";
        if (site.isNullString)
            return "NULL";
        return "O_" + site.id;
    }

    private static String allocSiteSetStr(Set<Obj> sites) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Obj s : sites) {
            if (!first)
                sb.append(", ");
            sb.append(allocSiteStr(s));
            first = false;
        }
        return sb.toString();
    }

    private static void debug(String s) {
        System.out.println(s);
    }

    private static void printPointsToData(String label, PointsToData state) {
        debug("  " + label + ":");
        if (state == null) {
            debug("    [NULL STATE]");
            return;
        }

        Map<LocalKey, Set<Obj>> stack = state.stack;
        if (stack.isEmpty()) {
            debug("    Variables: [EMPTY]");
        } else {
            debug("    Variables:");
            for (Map.Entry<LocalKey, Set<Obj>> entry : stack.entrySet()) {
                LocalKey var = entry.getKey();
                Set<Obj> sites = entry.getValue();
                debug("      " + var.toString() + " -> {" + allocSiteSetStr(sites) + "}");
            }
        }

        Map<FieldKey, Set<Obj>> heap = state.heap;
        if (!heap.isEmpty()) {
            debug("    Fields:");
            for (Map.Entry<FieldKey, Set<Obj>> entry : heap.entrySet()) {
                FieldKey key = entry.getKey();
                Set<Obj> sites = entry.getValue();
                Obj site = key.site;
                String siteStr = allocSiteStr(site);
                debug("      " + siteStr + "." + key.field.getName() + " -> {" + allocSiteSetStr(sites) + "}");
            }
        }

    }
}