package com.szz.openszz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class SZZTest {

    @Test
    void testSZZ() throws NumberFormatException, MalformedURLException {

        String repository = "https://github.com/apache/commons-bcel.git";
		String bugFixingCommit = "f959849a37c8b08871cec6d6276ab152e6ed08ce";
		String creationDateMillis = "1591052424000";

        List<String> expected = Arrays.asList(
            "2c76cda4b5a6032129b27c6e4806c5c683e5e715", 
            "9ea57757d0379296a9a72da05437f9a9cbb4a96f", 
            "9df8518923e78395c77c5c6214ad87f1dd1ad967", 
            "31dcc10240df3b9d0c2abce5610aaaeb04d0b864", 
            "8dc6bbae4dff5434ae0a112f1a9ace6066074906"
        );

        OpenSZZ a = new OpenSZZ();
        a.setUpRepository(repository);
        List<String> suspects = a.calculateBugIntroductionCommits(bugFixingCommit, Long.valueOf(creationDateMillis));

        assertEquals(expected, suspects);
    }
}
