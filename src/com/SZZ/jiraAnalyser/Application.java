package com.SZZ.jiraAnalyser;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.SZZ.jiraAnalyser.entities.Link;
import com.SZZ.jiraAnalyser.entities.Suspect;
import com.SZZ.jiraAnalyser.entities.Transaction;
import com.SZZ.jiraAnalyser.git.Git;

public class Application {

	public URL sourceCodeRepository;

	private Git git = null;
	public boolean hasFinished = false;

	public Application() {

	}

	public void setUpRepository(String url){
		Path fileStoragePath = Paths.get(System.getProperty("user.dir"));
		try {
			this.git = new Git(fileStoragePath, new URL(url));
			this.git.cloneRepository();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean mineData(String projectName, String bugFixingCommit) throws MalformedURLException {

		try {

			// GET BUGFIXING COMMIT
			System.out.println("Getting bug fixing commit " + projectName);
			List<Transaction> transactions = this.git.getCommits();

			Transaction bfc = null;
			for(Transaction t: transactions){
				if (t.getId().equals(bugFixingCommit)){
					bfc = t;
					break;
				}
			}

			transactions.clear();
			bfc.hasBugId();
			transactions.add(bfc);

			// GET LINKS
			System.out.println("Calculating bug fixing commits for project " + projectName);
			List<Link> links = new ArrayList<Link>(); 
			links.add(new Link(bfc, projectName));
			System.out.println(links);
			// CALCULATE BIC
			System.out.println("Calculating Bug inducing commits for project " + projectName);
			calculateBugInducingCommits(links, projectName);

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private void calculateBugInducingCommits(List<Link> links, String projectName) {
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(projectName + "_BugInducingCommits.csv");
			printWriter.println("BugFixingCommit,BugInducingCommit");
			for (Link l : links) {
				l.calculateSuspects(this.git, null);
				for (Suspect s : l.getSuspects()) {
					printWriter.println(l.transaction.getId() + "," + s.getCommitId());
				}
			}
			printWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println((e.getStackTrace()));
		}

	}
}
