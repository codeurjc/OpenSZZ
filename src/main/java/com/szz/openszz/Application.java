package com.szz.openszz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.json.simple.JSONArray;

public class Application {

	private static final String DEFAULT_LOCATION = System.getProperty("user.dir");

	@Parameter(names={"--bug-fixing-commit", "-bfc"}, required = true)
    String bugFixingCommit;

    @Parameter(names={"--repository-url", "-r"}, required = true)
    String url;

	@Parameter(names={"--issue-creation-millis", "-i"}, required = true)
    long creationDateMillis;

	@Parameter(names={"--repository-directory", "-d"})
    String repositoryDirectory = DEFAULT_LOCATION;

	@Parameter(names={"--result-output-directory", "-o"})
    String resultOutputDirectory = DEFAULT_LOCATION;

    public static void main(String ... argv) throws Exception {
        Application app = new Application();
        JCommander.newBuilder()
            .addObject(app)
            .build()
            .parse(argv);
        app.run();
    }

	public void run() throws NumberFormatException, MalformedURLException {

		OpenSZZ a = new OpenSZZ();
		a.setUpRepository(url);
		List<String> suspects = a.calculateBugIntroductionCommits(bugFixingCommit, creationDateMillis);
		JSONArray suspectsJson = new JSONArray();
		suspectsJson.addAll(suspects);

		File directory = new File(resultOutputDirectory);
		if(!directory.exists()) directory.mkdirs();

		try (FileWriter file = new FileWriter(resultOutputDirectory+"suspects.json")) {
			file.write(suspectsJson.toJSONString()); 
			file.flush();
	
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
