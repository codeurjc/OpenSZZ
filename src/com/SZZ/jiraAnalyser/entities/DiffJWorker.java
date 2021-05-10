package com.SZZ.jiraAnalyser.entities;

import com.SZZ.jiraAnalyser.git.Git;
import org.eclipse.jgit.revwalk.RevCommit;
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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DiffJWorker {

    public static List<LocationRange> getChanges(Git git, String commitId, String fileName) {
        RevCommit commit = git.getCommit(commitId);
        RevCommit parent = git.getCommit(commit.getParent(0).getName());
        Report diffjReport;
        try {
            diffjReport = DiffJWorker.getReport(git, commit, parent, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
        FileDiffs diffs = diffjReport.getDifferences();

        List<FileDiff.Type> targetTypes = List.of(FileDiff.Type.DELETED, FileDiff.Type.CHANGED);
        List<FileDiff> changes = diffs.stream().filter(diff -> targetTypes.contains(diff.getType())).collect(Collectors.toList());
        return changes.stream().map(change -> change.getFirstLocation()).collect(Collectors.toList());
    }

    public static Report getReport(Git git, RevCommit commitFrom, RevCommit commitTo, String fileName) throws IOException {
        File fileTo = new File("diffj_to_" + commitTo.getName() + ".java");
        byte[] fileContentTo = git.getFileContent(commitTo, fileName);
        saveFileContent(fileContentTo, fileTo);

        File fileFrom = new File("diffj_from_" + commitTo.getName() + ".java");
        byte[] fileContentFrom = git.getFileContent(commitTo, fileName);
        saveFileContent(fileContentFrom, fileFrom);

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

    private static void saveFileContent(byte[] content, File outputFile) throws IOException {
        if (content == null) outputFile.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
