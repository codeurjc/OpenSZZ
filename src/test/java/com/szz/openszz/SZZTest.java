package com.szz.openszz;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

class SZZTest {

    @Test
    void testSZZ() throws NumberFormatException, MalformedURLException {

        String repository = "https://github.com/apache/commons-bcel.git";
		String bugFixingCommit = "f959849a37c8b08871cec6d6276ab152e6ed08ce";
		String creationDateMillis = "1591052424000";

        OpenSZZ a = new OpenSZZ();
        a.setUpRepository(repository);
        a.calculateBugIntroductionCommits(bugFixingCommit, Long.valueOf(creationDateMillis));
    }
}
