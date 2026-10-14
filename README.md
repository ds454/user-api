# User API

Repo for the User API.

## Getting Started

Before doing anything, this repo needs to be cloned recursively. Make sure git can use your github username and token without prompts, and run the following command.
```
git clone --recurse-submodules https://github.com/ds454/user-api.git
```
This pulls the repository and the submodule `local-dev` which is needed to make sure everyone has the same versions of gRPC protos, the docker compose stack, and the Bruno project.

the git submodule is a reference to another repo inside a parent repo. In this situation, `user-api` is the parent repo, and `local-dev` is the submodule. By default submodules aren't pulled, so we need to specifically tell git to pull `local-dev` with the `--recurse-submodules` flag.


You have a couple different options for credentials.
- Enter username and token each time you open the container
- Store your credentials in git

If you want to do the second option, you will need to enter your username and token the first time the container is run. After that, it should cache your credentials so you won't need to enter them again. You should still save your token somewhere in case something weird happens.

#### Starting the Dev Container

This repo is designed to be used in a devcontainer. To use this repo effectively, docker and the VS Code devcontainer plugin must be installed. To open this repo in a devcontainer...

1) Start VS Code.
2) Open this folder in VS Code.
3) Open the VS Code command palette (`Ctrl + Shift + P` or View:Command Palette).
4) Search for "Reopen in Container" inside the command palette.
5) Hit `Enter`.

VS Code will start the devcontainer and make sure the right version of Java and Maven are installed. 

The first time this devcontainer is started, docker needs to pull the image (the files needed to actually run the container). On my laptop, it weighs about 1 gigabyte. Make sure you have at least 10 gigabytes of free storage. If your home internet isn't great, I would try to pull the images on campus. If your internet is too slow, pulling the images may not work.

## Software Versions
| Software | Version  |
|----------|----------|
| Java     | 25.0.4.1 |
| Maven    | 3.10.0   |

Note that gradle is _not_ installed.
