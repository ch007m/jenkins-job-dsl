import hudson.FilePath
import hudson.remoting.VirtualChannel
import org.jenkinsci.remoting.RoleChecker

import java.nio.file.Files
import java.nio.file.Path

class FilePathHelper {
    public static void backupPOM(FilePath srcFile) throws Exception {
        srcFile.act(new dev.snowdrop.jenkins.FilePathHelper.PomBackup());
    }
    // copy the pom.xml file to a backup file
    private static final class PomBackup implements FilePath.FileCallable<Void>{
        private static final long serialVersionUID = 1;
        @Override public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            // Destination file
            FilePath dest = new FilePath(channel,"pom.xml.bk");
            Files.copy(f.toPath(), Path.of(dest.toURI().getPath()));
            return null;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            //
        }
    }


}
