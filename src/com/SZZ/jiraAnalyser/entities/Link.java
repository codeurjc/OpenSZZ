package  com.SZZ.jiraAnalyser.entities;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.SZZ.jiraAnalyser.entities.Transaction.FileInfo;
import com.SZZ.jiraAnalyser.git.Git;

import org.eclipse.jgit.revwalk.RevCommit;


public class Link {

	public final Transaction transaction;
	private long creationDateMillis;
	
	private List<Suspect> suspects = new LinkedList<Suspect>();

	/**
	 * Object Representation of a Link Transaction, commit found from the log
	 * files Corresponding bug in Bugzilla The number, who links the transition
	 * with the bug
	 * 
	 * @param t
	 */
	public Link(Transaction t, long creationDateMillis) {
		this.transaction = t;
		this.creationDateMillis = creationDateMillis;
	}

	public List<Suspect> getSuspects() {
		return suspects;
	}

	public static String longestCommonSubstrings(String s, String t) {
		int[][] table = new int[s.length()][t.length()];
		int longest = 0;
		Set<String> result = new HashSet<>();

		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.charAt(i) != t.charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result.toString();
	}

	/**
	 * For each modified file it calculates the suspect
	 * 
	 * @param git
	 */
	public void calculateSuspects(Git git, PrintWriter l) {
		for (FileInfo fi : transaction.getFiles()) {
			if (fi.filename.endsWith(".java")) {
					String diff = git.getDiff(transaction.getId(), fi.filename, l);
					if (diff == null)
						break;
					List<Integer> linesMinus = git.getLinesMinus(diff);
					if (linesMinus == null)
						return;
					if (linesMinus.size() == 0)
						return;
					String previousCommit = git.getPreviousCommit(transaction.getId(), fi.filename,l);
					if (previousCommit != null) {
						Suspect s = getSuspect(previousCommit, git, fi.filename, linesMinus,l);
						if (s != null)
							this.suspects.add(s);
					}
			}
		}
	}

	/**
	 * It gets the commit closest to the Bug Open reposrt date
	 * 
	 * @param previous
	 * @param git
	 * @param fileName
	 * @param linesMinus
	 * @return
	 */
	private Suspect getSuspect(String previous, Git git, String fileName, List<Integer> linesMinus, PrintWriter l) {
    	RevCommit closestCommit = null; 
    	long tempDifference = Long.MAX_VALUE; 
    	for (int i : linesMinus){ 
    		try{ 
    			String sha = git.getBlameAt(previous,fileName,i);
    			if (sha == null)
    				break;
    			RevCommit commit = git.getCommit(sha,l); 
    			long difference =(creationDateMillis/1000) - (commit.getCommitTime()); 
    			if (difference > 0){ 
    				if (difference < tempDifference ){
    					closestCommit = commit; 
    					tempDifference = difference; } 
    				}
    			} catch (Exception e){ 
    				e.printStackTrace();
    				l.println(e);
    			}
    	} 
    	if (closestCommit != null){ 
    		Long temp = Long.parseLong(closestCommit.getCommitTime()+"") * 1000; 
    		Suspect s = new Suspect(closestCommit.getName(), new Date(temp), fileName);
    	return s; 
    	}
  
		return null;
	}

}
