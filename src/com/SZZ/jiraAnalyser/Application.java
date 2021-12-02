package com.SZZ.jiraAnalyser;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import com.SZZ.jiraAnalyser.entities.Link;
import com.SZZ.jiraAnalyser.entities.LinkManager;
import com.SZZ.jiraAnalyser.entities.Suspect;
import com.SZZ.jiraAnalyser.entities.Transaction;
import com.SZZ.jiraAnalyser.entities.TransactionManager;

public class Application {

	public URL sourceCodeRepository;

	private final TransactionManager transactionManager = new TransactionManager();
	private final LinkManager linkManager = new LinkManager();
	public boolean hasFinished = false;

	public Application() {
	}

	public boolean mineData(String git, String projectName) throws MalformedURLException {

		this.sourceCodeRepository = new URL(git);

		try {

			// GET BUGFIXING COMMIT
			System.out.println("Downloading Git logs for project " + projectName);
			List<Transaction> transactions = transactionManager.getBugFixingCommits(sourceCodeRepository, projectName);

			Transaction bfc = null;
			for(Transaction t: transactions){
				if (t.getId().equals("f959849a37c8b08871cec6d6276ab152e6ed08ce")){
					bfc = t;
					break;
				}
			}

			transactions.clear();
			transactions.add(bfc);

			System.out.println(bfc);

			// GET LINKS
			System.out.println("Calculating bug fixing commits for project " + projectName);
			List<Link> links = linkManager.getLinks(transactions, projectName, null);
			discartLinks(links);

			// CALCULATE BIC
			System.out.println("Calculating Bug inducing commits for project " + projectName);
			calculateBugInducingCommits(links, projectName);

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/*
	 * Only Links with sem > 1 OR ( sem = 1 AND syn > 0) must be considered
	 */
	private void discartLinks(List<Link> links) {
		List<Link> linksToDelete = new LinkedList<Link>();
		for (Link l : links) {
			if (l.getSemanticConfidence() < 1 && (l.getSemanticConfidence() != 1 || l.getSyntacticConfidence() < 0)) {
				linksToDelete.add(l);
			} else if (l.transaction.getTimeStamp().getTime() > l.issue.getClose()) {
				linksToDelete.add(l);
			}
		}
		links.removeAll(linksToDelete);
	}

	private void calculateBugInducingCommits(List<Link> links, String projectName) {
		System.out.println("Calculating Bug Inducing Commits");
		int count = links.size();
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(projectName + "_BugInducingCommits.csv");
			printWriter.println("bugFixingId;bugFixingTs;bugFixingfileChanged;bugInducingId;bugInducingTs;issueType");
			for (Link l : links) {
				if (count % 100 == 0)
					System.out.println(count + " Commits left");
				l.calculateSuspects(transactionManager.getGit(), null);
				String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
				SimpleDateFormat format1 = new SimpleDateFormat(pattern);
				for (Suspect s : l.getSuspects()) {
					printWriter.println();
					printWriter.println(
							l.transaction.getId() + ";" +
									format1.format(l.transaction.getTimeStamp()) + ";" +
									s.getFileName() + ";" +
									s.getCommitId() + ";" +
									format1.format(s.getTs()) + ";" +
									l.issue.getType());
				}
				count--;
			}
			printWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println((e.getStackTrace()));
		}

	}
}
