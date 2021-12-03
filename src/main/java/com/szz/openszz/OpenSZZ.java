package com.szz.openszz;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

import com.szz.openszz.entities.Link;
import com.szz.openszz.entities.Transaction;
import com.szz.openszz.git.Git;

public class OpenSZZ {

	public static final String DEFAULT_REPOSITORY_LOCATION = System.getProperty("user.dir")+"/tmp/";

	private Git git = null;

	public void setUpRepository(String url) throws Exception{
		this.setUpRepository(url, DEFAULT_REPOSITORY_LOCATION);
	}

	public void setUpRepository(String url, String repositoryDirectory) throws Exception{
		this.git = new Git(repositoryDirectory);
		if(!this.git.repositoryExist()){
			System.out.println("Repository not found at "+repositoryDirectory);
			System.out.println("Cloning repository from "+url);
			this.git.cloneRepository(url); 
		}
	}

	public List<String> calculateBugIntroductionCommits(String bugFixingCommit, long creationDateMillis) throws MalformedURLException {

		Transaction bfc = this.git.getCommitByHash(bugFixingCommit);

		Link link = new Link(bfc, creationDateMillis);

		link.calculateSuspects(this.git, null);

		return link.getSuspects().stream()
			.map((s)-> s.getCommitId())
			.distinct()
			.collect(Collectors.toList());
	}

}
