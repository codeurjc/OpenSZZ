package com.szz.openszz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.json.simple.JSONArray;

public class Application {

	@Parameter(names={"--bug-fixing-commit", "-bfc"}, description="Hash of bug fixing commit", required = true)
    String bugFixingCommit;

    @Parameter(names={"--repository-url", "-r"}, description="Git URL of the project repository")
    String url;

	@Parameter(names={"--issue-creation-millis", "-i"}, description="Timestamp of issue creation (in milliseconds)", required = true)
    long creationDateMillis;

	@Parameter(names={"--repository-directory", "-d"}, description="Path to directory where the repository is available or where the repository will be cloned")
    String repositoryDirectory = OpenSZZ.DEFAULT_REPOSITORY_LOCATION;

	@Parameter(names={"--result-output-directory", "-o"}, description="Path to directory where the result file 'suspects.json' will be stored")
    String resultOutputDirectory = System.getProperty("user.dir");

    public static void main(String ... argv) throws Exception {
        Application app = new Application();
        
		try{
			JCommander.newBuilder()
            .addObject(app)
            .build()
            .parse(argv);
		} catch (ParameterException e) {
			e.usage();
			System.exit(1);
		}

		app.run();
    }

	public void run() throws Exception {

		OpenSZZ a = new OpenSZZ();
		a.setUpRepository(url, repositoryDirectory);
		List<String> suspects = a.calculateBugIntroductionCommits(bugFixingCommit, creationDateMillis);
		JSONArray suspectsJson = new JSONArray();
		suspectsJson.addAll(suspects);

		File directory = new File(resultOutputDirectory);
		if(!directory.exists()) directory.mkdirs();

		try (FileWriter file = new FileWriter(resultOutputDirectory+"/suspects.json")) {
			file.write(suspectsJson.toJSONString()); 
			file.flush();
	
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
