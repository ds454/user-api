#! /bin/sh

# Pull the latest submodule updates
git submodule update --remote --merge

# Resolve maven dependancies
mvn dependency:resolve

echo Development environment configured. Happy coding!