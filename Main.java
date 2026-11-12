import soot.*;
import soot.options.Options;

public class Main {
    public static void main(String[] args) {
        String classPath = "./testcases/" + args[0];

        Options.v().set_keep_line_number(true);
        SceneTransformer sceneTransformer = new AnalysisTransformer();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.idfa", sceneTransformer));

        // Set up arguments for Soot
        String[] sootArgs = {
                "-cp", classPath,
                "-pp",
                "-w",
                "-app",
                "-allow-phantom-refs",
                "-no-bodies-for-excluded",
                "-exclude", "java.*",
                "-exclude", "javax.*",
                "-exclude", "sun.*",
                "-exclude", "com.sun.*",
                "-exclude", "jdk.*",
                // "-p", "jb", "use-original-names:true",
                "-f", "J",
                "-t", "1",
                "-main-class", "Test", // specify the main class
                "-process-dir", classPath
        };

        soot.Main.main(sootArgs);
    }
}