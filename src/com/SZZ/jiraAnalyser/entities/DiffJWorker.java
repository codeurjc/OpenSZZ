package com.SZZ.jiraAnalyser.entities;

import com.SZZ.jiraAnalyser.git.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.incava.analysis.FileDiff;
import org.incava.analysis.FileDiffs;
import org.incava.analysis.Report;
import org.incava.diffj.app.DiffJ;
import org.incava.diffj.app.Options;
import org.incava.ijdk.text.LocationRange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiffJWorker {

    public static List<LocationRange> getChanges(Git git, String commitId, String fileName) {
        RevCommit commit = git.getCommit(commitId);
        RevCommit parent = git.getCommit(commit.getParent(0).getName());
        Report diffjReport;
        try(Repository repository = org.eclipse.jgit.api.Git.open(git.workingDirectory).getRepository()){
            diffjReport = DiffJWorker.getReport(repository, commit, parent, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        FileDiffs diffs = diffjReport.getDifferences();

        List<FileDiff.Type> targetTypes = List.of(FileDiff.Type.DELETED, FileDiff.Type.CHANGED);
        List<FileDiff> changes = diffs.stream().filter(diff -> targetTypes.contains(diff.getType())).collect(Collectors.toList());
        return changes.stream().map(change -> change.getFirstLocation()).collect(Collectors.toList());
    }

    public static Report getReport(Repository repository, RevCommit commitFrom, RevCommit commitTo, String fileName) {
        File fileTo = new File("diffj_to_" + commitTo.getName() + ".java");
        saveFileContent(repository, commitTo, fileName, fileTo);

        File fileFrom = new File("diffj_from_" + commitFrom.getName() + ".java");
        saveFileContent(repository, commitFrom, fileName, fileFrom);

        Options opts = new Options();
        String[] args = {fileTo.getPath(), fileFrom.getPath()};
        List<String> names = opts.process(Arrays.asList(args));

        DiffJ diffj = new DiffJ(opts.showBriefOutput(), opts.showContextOutput(), opts.highlightOutput(),
                                opts.recurse(),
                                opts.getFirstFileName(), opts.getFromSource(),
                                opts.getSecondFileName(), opts.getToSource());

        diffj.processNames(names);
        try {
            Files.delete(fileFrom.toPath());
            Files.delete(fileTo.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diffj.getReport();
    }

    private static void saveFileContent(Repository repository, RevCommit commit, String fileName, File outputFile) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            if (commit == null) {
                try {
                    outputFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(fileName));
                    if (treeWalk.next()) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                            outputStream.write(loader.getBytes());
                        }
                    } else {
                        outputFile.createNewFile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
