Change Log
==========

Version 0.7.2 *(2020-01-30)*
----------------------------

* Detect gesture based on gravity and magnetic field measurements. It makes detection more robust.

Version 0.7.1 *(2020-01-30)*
----------------------------

* Make gesture detector more relaxed and so specific.

Version 0.7.0 *(2020-01-30)*
----------------------------

* Remove Telescope dependency and use a twist motion for capturing screenshots.
* Fix bug where errors on auto complete texts did not disappear sometimes.

Version 0.6.3 *(2020-01-23)*
----------------------------

* Use RFC 822 time zone format to fix crashes on API below 24.

Version 0.6.2 *(2020-01-17)*
----------------------------

* Remove runtime permission for external app access.

Version 0.6.1 *(2020-01-17)*
----------------------------

* Remove unused File Provider that could easily cause conflicts in manifest mergers.

Version 0.6.0 *(2020-01-17)*
----------------------------

* Add support for submitting sub tasks.
* Fix a bug where canvas couldn't be cleared without redo history.

Version 0.5.0 *(2020-01-14)*
----------------------------

* Add support for custom credential providers.
* Add support for the complementary Squash It app.
* Fix layout inflation crashes on Android 21 and 22.

Version 0.4.3 *(2020-01-08)*
----------------------------

* Add an option to disable reporter overrides for issues creation.

Version 0.4.2 *(2020-01-05)*
----------------------------

* Remove 'ReportActivity' memory leaks on configuration changes.
* Add supported ABIs to the device info section.

Version 0.4.1 *(2020-01-05)*
----------------------------

* Add implicit Internet permission to the library manifest file.

Version 0.4.0 *(2020-01-05)*
----------------------------

* Configure the plugin from the source code and not from the resource files.
* Add a local date time to the report info.
* Fix an issue when screenshot and logs attachments would be added when their respective checkboxes were not checked.

Version 0.3.5 *(2020-01-04)*
----------------------------

* Fix missing public resource declaration.

Version 0.3.4 *(2020-01-03)*
----------------------------

* Show thumbnails for custom attachments whenever possible.
* Highlight attachments that are larger then 10 MB.
* Migrate to presentation to the 'ViewModel' library to ensure compatibility with newer versions of the 'AppCompat' library.

Version 0.3.3 *(2020-01-03)*
----------------------------

* Allow to redo drawing actions after clearing the whole canvas.
* Minor improvements to the screenshot creation.

Version 0.3.2 *(2020-01-02)*
----------------------------

* Enable users and issue types whitelisting.

Version 0.3.1 *(2019-12-31)*
----------------------------

* Improve threading for file generation.
* Use Snackbars instead of Toasts for error prompts.
* Allow to override log file entry count.

Version 0.3.0 *(2019-12-31)*
----------------------------

* Add a feature for screenshot preview and editing.
* Fix a bug with screenshots not being properly detected.
* Fix a bug with Telescope attaching to other activities.
* Bring back centered content.
* Restructure internal API packages.

Version 0.2.0 *(2019-12-26)*
----------------------------

* Merge 'backend' and 'core' artifacts into 'squashit' artifact.
* Plugin no longer distinguishes on the UI level between comment and new issue failures.
* Align screen content top.
* Internal API changes.

Version 0.1.2 *(2019-12-17)*
----------------------------

* Fix wrong misconfiguration info.

Version 0.1.1 *(2019-12-17)*
----------------------------

* Deploy missing 'backend' artifact.

Version 0.1.0 *(2019-12-17)*
----------------------------

* Initial release.
