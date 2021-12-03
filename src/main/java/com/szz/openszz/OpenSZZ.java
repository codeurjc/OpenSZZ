package com.szz.openszz;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.szz.openszz.entities.Link;
import com.szz.openszz.entities.Transaction;
import com.szz.openszz.git.Git;

public class OpenSZZ {

	public URL sourceCodeRepository;

	private Git git = null;
	public boolean hasFinished = false;

	public OpenSZZ() {

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

	public List<String> calculateBugIntroductionCommits(String bugFixingCommit, long creationDateMillis) throws MalformedURLException {

		Transaction bfc = this.git.getCommitByHash(bugFixingCommit);

		Link link = new Link(bfc, creationDateMillis);

		link.calculateSuspects(this.git, null);

		List<String> suspects = link.getSuspects().stream()
										.map((s)-> s.getCommitId())
										.distinct()
										.collect(Collectors.toList());
		return suspects;
	}

}
