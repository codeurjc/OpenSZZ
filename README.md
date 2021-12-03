# OpenSZZ-Simple

This repository contains a modified version of the [OpenSZZ](https://github.com/clowee/OpenSZZ) algorithm.

Compared to the original project, the functionality of the algorithm has not been modified, but the way in which it is executed has.

The main change is that Jira's issue mining has been removed. Now it is the user who provides the following information to the algorithm:

- The hash commit in which the bug fix was introduced
- The date (in milliseconds) when the issue was created where the bug was reported
- The project's git repository (local path or repository url)

The executable prints the list of suspected commits in a JSON file (suspects.json) for integration with other tools.

Pre-requisites:
- Java 8+

## Build project

Pre-requisites:
- Java 8+
- Maven 3+

In order to generate de executable (jar file) just run:

```
     mvn clean package -DskipTests
```

## Usage

### With Git URL

```
     java -jar openszz.jar -bfc <bug_fix_hash> -r <repository_url> -i <issue_creation_timestamp>
```

Example:
```
     java -jar openszz.jar -bfc f959849a37c8b08871cec6d6276ab152e6ed08ce -r https://github.com/apache/commons-bcel.git -i 1591052424000
```

You can also specify the folder where the repository will be cloned (-d):

```
    java -jar openszz.jar -bfc <bug_fix_hash> -r <repository_url> -d <path_to_repo> -i <issue_creation_timestamp>
```

### With local repo

```
     java -jar openszz.jar -bfc <bug_fix_hash> -d <path_to_repo> -i <issue_creation_timestamp>
```

Example:
```
     java -jar openszz.jar -bfc f959849a37c8b08871cec6d6276ab152e6ed08ce -d path/to/repo/ -i 1591052424000
```

### All options

```
  * --bug-fixing-commit, -bfc
      Hash of bug fixing commit
  * --issue-creation-millis, -i
      Timestamp of issue creation (in milliseconds)
    --repository-directory, -d
      Path to directory where the repository is available or where the repository will be cloned
      Default: <current_work_directory>/tmp/
    --repository-url, -r
      Git URL of the project repository
    --result-output-directory, -o
      Path to directory where the result file 'suspects.json' will be stored
      Default: <current_work_directory>/suspects.json
```


