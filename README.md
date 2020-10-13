# Squash It - JIRA reporting tool

[![Build Status](https://app.bitrise.io/app/d05c685963b4f009/status.svg?token=BcDiRXjSbF_95LiAmxH26w&branch=master)](https://app.bitrise.io/app/d05c685963b4f009)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.mehow.squashit/squashit/badge.svg)](https://search.maven.org/search?q=g:io.mehow.squashit)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

## How it works?

Squash It allows you to report JIRA issues or add comments to existing ones. You can open the tool by tapping on a notification in the system menu. This will capture an editable screenshot of the app's current window and let you provide the issue details.

![](images/sample-report.gif)

The final report will contain all the details you provided as well as some metadata about the device.

![](images/sample-new-issue.png)

Unfortunately, since JIRA API does not allow to set comment's author, when you add a comment to an issue it will be authored by a user who is integrated with the tool, but it will mention selected reporter.

![](images/sample-add-comment.png)

## Integration

Squash It requires Java 8 bytecode. To enable Java 8 desugaring configure it in your Gradle script.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
  // For Kotlin projects
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
```

To integrate the tool with your app you need to add the dependency to your project and configure it. Plugin should be configured before it attaches itself to any Activity. The best place for it is the Application object. Check [the sample](sample/) for more information.

```groovy
debugImplementation "io.mehow.squashit:squashit:0.7.6"
```

```kotlin
SquashItConfigurator
    .projectKey("ProjectKey")
    .jiraUrl("JiraUrl")
    .credentialsProvider(CredentialsProvider("UserId"))
    .configure(context)
```

The user needs to have read and write permissions to your project so you should make sure that the token is not accessible outside of your organization. One way to make it safe is to use an external app that can store credentials and expose it via `CredentialsProvider` interface during configuration. You can use your custom solution or leverage complementary [`Squash It`](squash-it/app) application and use `CredentialsProvider("UserId")` instead.

## Optional configuration

Also, you can configure some optional settings that might make your life easier.

### Logs

In order to attach logs to the reports you have to delegate them to `SquashItLogger`.

```kotlin
val myLogger: MyLogger
myLogger.log { priority, tag, message -> SquashItLogger.log(priority, tag, message) }
```

If you use [Timber](https://github.com/JakeWharton/timber) you can skip this step and just depend on the Timber artifact instead.

```groovy
debugImplementation "io.mehow.squashit:timber:0.7.6"
```

By default last 2000 logs are kept in a journal. You can configure the capacity of the log file. Keep in mind, however, that logs are kept in memory prior to writing them to a file, so you should be mindful of the capacity.

```kotlin
SquashItConfigurator
    .logsCapacity(5_000)
    .configure(context)
```

### User filter

If your JIRA has a lot of tools and users assigned to them have access to your project you might want to filter them out so they don't appear in the tool.

```kotlin
val whitelistedUsers = listOf("AccountId")
SquashItConfigurator
    .userFilter { user -> user.accountId in whitelistedUsers }
    .configure(context)
```

### Issue types

The same thing applies to issue types. You might want to restrict which types of issues reporters should be allowed to create.

```kotlin
val whitelistedIssueTypes = listOf("IssueTypeId")
SquashItConfigurator
    .issueTypeFilter { issueType -> issueType.id in whitelistedIssueTypes }
    .configure(context)
```

### Epic deserialization

JIRA uses custom fields for epics in its model. By default, they are configured to `customfield_10009` for reading epics from a JQL query and `customfield_10008` for assigning an epic to an issue while creating it.

```kotlin
SquashItConfigurator
    .epicReadFieldName("value_a")
    .epicWriteFieldName("value_b")
    .configure(context)
```

### Overwrite issue reporter

If you want or need to disable overwriting of the issue reporter you can configure it this way.

```kotlin
SquashItConfigurator
    .allowReporterOverride(false)
    .configure(context)
```

If you do this the the reporter will be added to the description like for comments.

### Overwrite sub task ID

By default JIRA uses ID `"5"` for sub tasks. If your configuration requires different value you can set it like so.

```kotlin
SquashItConfigurator
    .subTaskIssueId("10")
    .configure(context)
```

## Sample

You can check the integration with your JIRA by running [the sample](sample/) project. You'll need however to overwrite the [configuration file](sample/src/main/java/io/mehow/squashit/sample/SampleApplication.kt).

## License

    Copyright 2019 Michał Sikora

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
