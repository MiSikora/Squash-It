# Squash It - JIRA report tool

[![Build Status](https://app.bitrise.io/app/d05c685963b4f009/status.svg?token=BcDiRXjSbF_95LiAmxH26w&branch=master)](https://app.bitrise.io/app/d05c685963b4f009)

[![Maven Central](https://img.shields.io/maven-central/v/io.mehow.squashit/squashit.svg)](https://search.maven.org/search?q=g:io.mehow.squashit)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

## How it works?

Squash It allows you to report JIRA issues or add comments to existing ones. You can open the report tool by holding two fingers on your screen. This will capture an editable screenshot of the app's current window and let you provide the issue details.

![](images/sample-screenshot.gif)
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

To integrate the tool with your app you need to add the dependency to your project.

```groovy
debugImplementation "io.mehow.squashit:squashit:0.3.2"
```

You should also delegate logs to `SquashItLogger` so they can be attached to the reports.

```kotlin
val myLogger: MyLogger
myLogger.log { priority, tag, message -> SquashItLogger.log(priority, tag, message) }
```

If you use [Timber](https://github.com/JakeWharton/timber) all you need to do is to depend on a Timber artifact instead.

```groovy
debugImplementation "io.mehow.squashit:timber:0.3.2"
```

Also, you need to configure the tool with your JIRA project. For that, you have to override some `string` resources.

```xml
<string name="squash_it_jira_server_url">https://squashit.atlassian.net</string>
<string name="squash_it_jira_user_email">jira-bot@squashit.com</string>
<string name="squash_it_jira_user_token">MXhDz3MlmwyH2v9aVASKx77a</string>
<string name="squash_it_jira_project_key">SQ</string>
```

The user needs to have read and write permissions to your project so you should make sure that the token is not accessible outside of your organization.

## Optional configuration

Also, you can configure some optional resources that might make your life easier.

### User filter

If your JIRA has a lot of tools and users assigned to them have access to your project you might want to filter them out so they don't appear in hints. To do this you need to provide their IDs in a `string-array` resource.

```xml
<string-array name="squash_it_jira_user_filter">
  <item>557058:8d5dc844-3ffe-4723-a067-1f5d8c6470e6</item>
  <item>5a8eb647d324b531c3d3f9cd</item>
  <item>5b69ff2b85ee4d3d958602b0</item>
  <item>5dd3eb2f58fc78100710bbe4</item>
  <item>557058:e66733f5-626e-4c74-b103-976e1eeb3abb</item>
  <item>5ac5326a95d30150501e5ff4</item>
</string-array>
```

By default users provided in the resource are blacklisted. If you want to whitelist them instead you need to override a `bool` resource.

```xml
<string name="squash_it_whitelist_jira_user">true</string>
```

### Issue types

The same thing applies to issue types. You might want to restrict which types of issues reporters should be allowed to create. For example, it might not make sense to let users create epics or graphical tasks. To do this you need to provide their IDs in a `string-array` resource.

```xml
<string-array name="squash_it_jira_issue_types_filter">
  <item>10000</item>
  <item>10400</item>
  <item>10646</item>
</string-array>
```

By default issue types provided in the resource are blacklisted. If you want to whitelist them instead you need to override a `bool` resource.

```xml
<string name="squash_it_whitelist_issue_types">true</string>
```

### Finger trigger

By default, the tool is initiated by holding two fingers on a screen. However, it might not be the best idea for some applications where using two fingers is part of the app's flow (like map applications). To change the finger count you need to override an `integer` resource.

```xml
<integer name="squash_it_report_pointer_count">3</integer>
```

### Epic deserialization

JIRA uses custom fields for epics in its model. By default, they are configured to `customfield_10009` for reading epics from a JQL query and `customfield_10008` for assigning an epic to an issue while creating it. If your JIRA uses different field names for epics you need to override `string` resources.

```xml
<string name="squash_it_jira_epic_read_field_name">value_a</string>
<string name="squash_it_jira_epic_write_field_name">value_b</string>
```

### Logs capacity

By default last 2000 logs are kept in a journal. To change this behaviour you need to override `integer` resource.

```xml
<integer name="squash_it_logs_capacity">5000</integer>
```

## Sample

You can check the integration with your JIRA by running [the sample](sample/) project. You'll need however to overwrite the [configuration file](sample/src/main/res/values/strings.xml).

## Attribution

* [Matthew Precious](https://github.com/mattprecious) and his awesome [Telescope library](https://github.com/mattprecious/telescope) which is used in this project.

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
