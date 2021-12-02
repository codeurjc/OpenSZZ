package com.SZZ.jiraAnalyser;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public boolean calculateBugIntroductionCommits(String bugFixingCommit, long creationDateMillis) throws MalformedURLException {

		try {

			Transaction bfc = this.git.getCommitByHash(bugFixingCommit);

			Link link = new Link(bfc, creationDateMillis);

			link.calculateSuspects(this.git, null);
			for (Suspect s : link.getSuspects()) {
				System.out.println(link.transaction.getId() + "," + s.getCommitId());
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}

}
