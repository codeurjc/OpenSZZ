package com.SZZ.app;

import java.net.MalformedURLException;


import com.SZZ.jiraAnalyser.Application;

public class SZZApplication {

	public static void main(String[] args) {
		args = new String[4];
		args[0] = "-all";
		args[1] = "https://github.com/apache/commons-bcel.git";
		args[2] = "BCEL";
		
//		args[0] = "-all";
//		args[1] = "https://github.com/apache/archiva.git";
//		args[2] = "https://issues.apache.org/jira/projects/MRM";
//		args[3] = "MRM";
//		https://issues.apache.org/jira/projects/MRM
				
		if (args.length == 0) {
			System.out.println("Welcome to the SZZ Calculation script.");
			System.out.println("Here a guide how to use the script");
			System.out.println("szz.jar -all githubUrl, jiraUrl, jiraKey => all steps together");
		} else {
			switch (args[0]) {
			case "-all":
				try {
					Application a = new Application();
					a.mineData(args[1], args[2]);
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
