package com.szz.openszz.git;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.szz.openszz.entities.Transaction;
import com.szz.openszz.entities.Transaction.FileInfo;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

public class Git {

	public final String workingDirectory;
	public final String logCommand;
	private BlameResult blame;

	private static final char DELIMITER = ';';

	public Git(String repositoryDirectory) {;
		this.workingDirectory = repositoryDirectory;
		this.logCommand = "git log " +
				"--pretty=format:\'" +
				"%H" + DELIMITER +
				"%aI" + DELIMITER +
				"%aN" + DELIMITER +
				"%s" + DELIMITER +
				"\' " +
				"--name-status -M100%";
	}

	public void cloneRepository(String url) throws Exception {
		String cloneCommand = "git clone " + url + " " + this.workingDirectory;
		ProcessBuilder pb = new ProcessBuilder(cloneCommand.split(" "));
		pb.redirectErrorStream(true);
		pb.directory(new File(System.getProperty("user.dir")));
		pb.redirectOutput(Redirect.INHERIT);
		Process p = pb.start();
		p.waitFor();
	}

	public boolean repositoryExist(){
		return new File(this.workingDirectory).exists();
	}

	public Transaction getCommitByHash(String hash){

		String command = this.logCommand + " -p -1 " + hash;
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.directory(new File(workingDirectory));
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		String line1 = "";
		String hashId = "";
		Transaction transaction = null;
		try {
			while ((line = br.readLine()) != null){
				if (!line.isEmpty() && line.startsWith("\'")) {
					line = line.replaceAll("\'", "");
					String[] array = line.split(";");
					hashId = array[0];
					String timestamp = array[1];
					String author = array[2];
					String comment = array[3];
					List<FileInfo> filesAffected = new ArrayList<FileInfo>();
					line1 = br.readLine();
					if (line1 != null) {
						while (line1 != null && !(line1).equals("")) {
							int BUFFER_SIZE = 100000;
							br.mark(BUFFER_SIZE);
							if (!line1.startsWith("\'")) {
								String[] subarray = line1.split("	");
								String status = subarray[0];
								String file = subarray[1];
								FileInfo fileInfo = new FileInfo(status, file);
								filesAffected.add(fileInfo);
							} else {
								br.reset();
								break;
							}
							line1 = br.readLine();

						}
					}
					transaction = new Transaction(
							hashId,
							timestamp,
							author,
							comment,
							filesAffected);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return transaction;
	}

	/**
	 * Returns String with differences of fileName done by a shaCommit
	 * 
	 * @param shaCommit
	 * @param fileName
	 * @return
	 */
	public String getDiff(String shaCommit, String fileName, PrintWriter l) {
		String result = "";
		File localRepo1 = new File(this.workingDirectory + "");
		try {
			org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1);
			ObjectId oldId = git.getRepository().resolve(shaCommit + "^^{tree}");
			ObjectId headId = git.getRepository().resolve(shaCommit + "^{tree}");
			ObjectReader reader = git.getRepository().newObjectReader();
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);
			List<DiffEntry> diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			// df.setDiffComparator(RawTextComparator.WS_IGNORE_LEADING);
			df.setRepository(git.getRepository());

			for (DiffEntry diff : diffs) {
				if (diff.getNewPath().contains(fileName)) {
					// Print the contents of the DiffEntries
					// System.out.println(diff.getNewPath());
					df.format(diff);
					diff.getOldId();
					String diffText = out.toString("UTF-8");
					result = diffText;
					out.reset();
					df.close();
					break;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			l.println(e);
			return null;
		}
		return result;
	}

	/**
	 * It gets removed lines from a commit starting from the diffString
	 * 
	 * @param diffString
	 * @return
	 */
	public List<Integer> getLinesMinus(String diffString) {

		int actualInt = 1;
		boolean actualSet = false;
		List<Integer> listMinus = new LinkedList<Integer>();
		try {
			Scanner scanner = new Scanner(diffString);
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				switch (line.charAt(0)) {
					case '-':
						actualInt++;
						listMinus.add(actualInt);
						break;
					case '+':
						break;
					case '@':
						int stringMinus = line.indexOf('-');
						int stringCommma = line.indexOf(',');
						stringMinus++;
						String sM = line.substring(stringMinus, stringCommma);
						actualInt = Integer.parseInt(sM);
						line = scanner.nextLine();
						actualSet = true;
						break;
					default:
						if (actualSet)
							actualInt++;
						break;
				}
			}
			scanner.close();
		} catch (Exception e) {
			return null;
		}
		return listMinus;

	}

	/**
	 * It gets blame of a file at a specific commit time
	 * index 0 of array ==> line 0
	 * index 1 of array ==> line 1
	 * index 2 of array ==> line 2
	 * 
	 * @param commitSha
	 * @param file
	 * @param git
	 * @return
	 */
	// removed unused parameter PrintWriter l
	public String getBlameAt(String commitSha, String file, int lineNumber) {
		File localRepo1 = new File(this.workingDirectory);
		try {
			if (blame == null) {
				org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1);
				Repository repository = git.getRepository();
				BlameCommand blamer = new BlameCommand(repository);
				ObjectId commitID;
				commitID = repository.resolve(commitSha);
				blamer.setStartCommit(commitID);
				blamer.setFilePath(file);
				blame = blamer.call();
			}
			RevCommit commit = blame.getSourceCommit(lineNumber);
			return commit.getName();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * It gets commit object starting from a specific sha
	 *
	 */
	public RevCommit getCommit(String sha, PrintWriter l) {
		File localRepo1 = new File(this.workingDirectory);
		// Repository repository = git.getRepository();
		RevCommit commit = null;
		try {
			org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1);
			Repository repository = git.getRepository();
			RevWalk walk = new RevWalk(repository);
			ObjectId commitId = ObjectId.fromString(sha);
			commit = walk.parseCommit(commitId);
			walk.close();
		} catch (Exception e) {
			e.printStackTrace();
			l.println((e));
			return null;
		}
		return commit;
	}

	/**
	 * Get Commit that changed the file before the parameter commit
	 * 
	 * @param sha
	 * @param file
	 * @return
	 */
	public String getPreviousCommit(String sha, String file, PrintWriter l) {
		File localRepo1 = new File(this.workingDirectory);
		String finalSha = "";
		RevCommit latestCommit = null;
		String path = file;
		try {
			org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1);
			RevWalk revWalk = new RevWalk(git.getRepository());
			RevCommit revCommit = getCommit(sha, null);
			revWalk.markStart(revCommit);
			revWalk.sort(RevSort.COMMIT_TIME_DESC);
			revWalk.setTreeFilter(AndTreeFilter.create(PathFilter.create(path), TreeFilter.ANY_DIFF));
			latestCommit = revWalk.next();
			while (!latestCommit.getName().equals(sha))
				latestCommit = revWalk.next();
			latestCommit = revWalk.next();
			if (latestCommit == null)
				return null;
			finalSha = latestCommit.getName();

		} catch (Exception e) {
			l.println("No Predecessor-Commits found for " + sha + "for file " + file);
			return null;
		}
		return finalSha;
	}
}
