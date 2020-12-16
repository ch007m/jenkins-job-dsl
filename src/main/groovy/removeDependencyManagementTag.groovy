package groovy

import dev.snowdrop.jenkins.FilePathHelper
import hudson.model.AbstractBuild;
import hudson.remoting.Channel;

def Channel channel;
def AbstractBuild build;

if(build.getWorkspace().isRemote()){
    channel = build.getWorkspace().channel
    println "Got a channel :-)"
}

def wksString = build.get
println "Remote WKS: $wksString"

String src = wksString + "/" + "pom.xml"
pomFile = new hudson.FilePath(channel, src)
println "Get the pom.xml src file"

FilePathHelper.backupPOM(pomFile)

// String dst = wksString + "/" + "pom.xml.bak"
// pomBkFile = new hudson.FilePath(channel, dst)
// println "Create pom.xml backup file"
// pomFile.copyTo(pomBkFile)

println "Backup file : pomBkFile"

println "Remove <dependencyManagement> tag from the pom.xml to allow to control if the GAVs exist"
def pomModified = src.text
        .replace('<dependencyManagement>', '')
        .replace('</dependencyManagement>', '')
src.text = pomModified
println src.txt


