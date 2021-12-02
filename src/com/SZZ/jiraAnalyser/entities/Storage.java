package  com.SZZ.jiraAnalyser.entities;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.SZZ.jiraAnalyser.git.*;


public class Storage {
	
	private Path fileStoragePath = Paths.get(
			System.getProperty("user.dir")
	);
	
	{
		// Create working directory if it does not exist.
		fileStoragePath.toFile().mkdirs();
	}
	
	private Git git = null;
	private final Pattern pGit = Pattern.compile(".+\\.git$");
	
	public Storage(String projectName) {
		fileStoragePath = Paths.get(
				System.getProperty("user.dir") 
				);
	}
	
	
	/**
	 * Gets a list of presumed bug-fixing-commits
	 * @param url
	 * @param projectName
	 * @return
	 */
	public List<Transaction> checkoutCvs(URL url, String projectName) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		Matcher mGit = pGit.matcher(url.toString());
		if(mGit.find()) {
			this.git = new Git(fileStoragePath, url);
			try {
				this.git.cloneRepository();
				this.git.pullUpdates();
				this.git.saveLog();
				transactions = git.getCommits();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return transactions;
	}
	
	public Git getGit(){
		return this.git;
	}
}
