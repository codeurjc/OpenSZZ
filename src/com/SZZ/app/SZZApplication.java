package com.SZZ.app;

import java.net.MalformedURLException;


import com.SZZ.jiraAnalyser.Application;

public class SZZApplication {

	public static void main(String[] args) {

		// EXAMPLE

		args = new String[5];
		args[0] = "-all";
		args[1] = "https://github.com/apache/commons-bcel.git";
		args[2] = "BCEL";
		args[3] = "f959849a37c8b08871cec6d6276ab152e6ed08ce";
		args[4] = "1591052424000";
				
		if (args.length == 0) {
			System.out.println("Welcome to the SZZ Calculation script.");
			System.out.println("Here a guide how to use the script");
			System.out.println("szz.jar -all githubUrl, jiraUrl, jiraKey => all steps together");
		} else {
			switch (args[0]) {
			case "-all":
				try {
					Application a = new Application();
					a.setUpRepository(args[1]);
					

					a.calculateBugIntroductionCommits(args[2], args[3],Long.parseLong(args[4]));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				System.out.println("Commands are not in the right form! Please retry!");
				break;

			}
		}

	}

}
