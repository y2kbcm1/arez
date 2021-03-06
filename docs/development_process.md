---
title: Development Process
---
<nav class="page-toc">

<!-- toc -->

- [Documentation](#documentation)
  * [Documenting changes in CHANGELOG.md](#documenting-changes-in-changelogmd)
- [Publishing](#publishing)
  * [Publishing the Website](#publishing-the-website)
  * [Publishing to Maven Central](#publishing-to-maven-central)
  * [Publishing to Downstream Projects](#publishing-to-downstream-projects)
  * [Publishing Coverage Reports to codecov](#publishing-coverage-reports-to-codecov)
  * [Encrypting Files for TravisCI](#encrypting-files-for-travisci)

<!-- tocstop -->

</nav>

## Documentation

### Documenting changes in CHANGELOG.md

The Changelog is where users go to see what has changed between versions so it is essential we keep it
up to date. A poor changelog should be considered a bug to be fixed.

The basic structure of the changelog is sourced from the [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
site but the format should be obvious from reviewing the file. The valid sections in each version and their
meanings are as follows:

  - ✨ **Added** for new features.
  - **Changed** for changes in existing functionality.
  - 💥 **Changed** for breaking changes in existing functionality.
  - **Deprecated** for soon-to-be removed features.
  - **Removed** for now removed features.
  - **Fixed** for any bug fixes.
  - **Security** in case of vulnerabilities.

## Publishing

### Publishing the Website

TravisCI regenerates the website every time a commit is pushed to the master branch. The initial setup was
derived from a [gist](https://gist.github.com/domenic/ec8b0fc8ab45f39403dd) and customized for our project.
The basic idea is to setup the GitHub project with a deploy key, encrypt the key and make it available to
TravisCI that will unencrypt it as part of the build.

Firstly you create the key via the following command.

    $ ssh-keygen -t rsa -b 4096 -C "peter@realityforge.org" -f ../deploy -P ""

This is a private key and should NOT be checked into source code repository. Instead an encrypted version
of the file is checked in. See the "Encrypting Files for TravisCI" section below for how to do this.

Then update the travis configuration file in the `before_install` section and after the encrypted file has been
decoded add the following.

```yaml
  - chmod 600 ../deploy
  - eval `ssh-agent -s` && ssh-add ../deploy
```

Finally you add the public part of the deploy key to the repository at
[https://github.com/arez/arez.github.io/settings/keys](https://github.com/arez/arez.github.io/settings/keys) and
make sure you give the key write access.

### Publishing to Maven Central

Arez releases are published to Maven Central. To simplify this process we have automated the release
process so that the last step of the TravisCI build is to run the task `publish_if_tagged`. If the
current git version is a tag, the artifacts produced by the build will be published to Maven Central.

To enable this we needed to provide encrypted credentials to TravisCI. The easiest way to do this is
to run the commands below and add the output under `env.global` key in the travis configuration.
This encrypts the password but makes it available when building on TravisCI.

    travis encrypt MAVEN_CENTRAL_PASSWORD=MyPassword --add
    travis encrypt GPG_USER=MyGpgUsername --add
    travis encrypt GPG_PASS=MyGpgPassword --add

You also need to provide a gpg key that can be used to sign the releases. To do this export a GPG that
can be used to sign releases to a file such as `../release.asc`. This should contain the private key and
should NOT be checked into source code repository. Instead an encrypted version of the file is checked.
See the "Encrypting Files for TravisCI" section below for how to do this.

Then update the travis configuration file in the `before_install` section and after the encrypted file has been
decoded add the following.

```yaml
  - chmod 600 ../release.asc
  - gpg --import ../release.asc
```

### Publishing to Downstream Projects

Arez updates several downstream projects during it's release process process. The goal of this process
is to ensure these downstream projects are always up to date with the latest version of Arez. However
there is a delay between publishing to Maven Central and the artifacts being available in Maven Central.
To combat this we deploy the required artifacts to a separate staging repository.

To enable this we needed to provide encrypted credentials to TravisCI. The easiest way to do this is
to run the commands below and add the output under `env.global` key in the travis configuration.
This encrypts the password but makes it available when building on TravisCI.

    travis encrypt STAGING_USERNAME=MyStagingUsername --add
    travis encrypt STAGING_PASSWORD=MyStagingPassword --add

### Publishing Coverage Reports to codecov

The project publishes the code coverage reports to codecov. This is to make it easier to review pull requests
and to get a quick overview on how we are doing test-coverage wise. It is not a goal of the project to get 100%
test coverage as that does not necessarily mean the tests are any good. Some parts of the codebase we do try and
keep reasonably high coverage as they are complex pieces of code. The coverage just helps asses the code and
may suggest parts that need more testing.

To get codecov reports, the project was signed up to codecov and then we enabled the buildr `jacoco` addon. Then
all that remains is to add the following snippet in out TravisCI configuration.

```yaml
after_success:
  - bash <(curl -s https://codecov.io/bash)
```

### Encrypting Files for TravisCI

TravisCI can only decrypt a single file in a build. As soon as you start requiring multiple secret files you need
to package them into an archive and encrypt the archive. As we need multiple keys to publish to Maven Central
and publish to GitHub we have been forced to use this strategy.

    $ (cd .. && tar cvf secrets.tar release.asc deploy)
    $ mkdir etc
    $ travis encrypt-file ../secrets.tar etc/secrets --add
    $ git add etc/secrets

Then update the travis configuration file as specified by `travis encrypt-file` command and unpack the archive.
The start of the `before_install` section should look something like:

```yaml
before_install:
  - openssl aes-256-cbc -K $encrypted_000000000000_key -iv $encrypted_000000000000_iv -in etc/secrets -out ../secrets.tar -d
  - (cd ../ && tar xvf secrets.tar)
```
