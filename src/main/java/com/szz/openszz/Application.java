package com.szz.openszz;

import java.net.MalformedURLException;

public class Application {

	public static void main(String[] args) throws NumberFormatException, MalformedURLException {

		// EXAMPLE

		args = new String[3];
		args[0] = "https://github.com/apache/commons-bcel.git";
		args[1] = "f959849a37c8b08871cec6d6276ab152e6ed08ce";
		args[2] = "1591052424000";
				
		if (args.length != 3) {
			System.out.println("Welcome to the SZZ Calculation script.");
			System.out.println("Here a guide how to use the script");
			System.out.println("szz.jar <repo_url> <bug_fixing_commit> <issue_created_millis>");
		} else {
			OpenSZZ a = new OpenSZZ();
			a.setUpRepository(args[0]);
			a.calculateBugIntroductionCommits(args[1],Long.parseLong(args[2]));
		}

	}

}
