#! /bin/sh

# Pull the latest submodule updates
echo Refreshing local-dev...
git submodule update --remote --merge
echo Done!

# Update Maven 
echo Pulling maven dependancies...
mvn dependency:resolve
echo Done!

# Compile protoc files
echo Compiling protoc to Java...
mvn clean compile
echo Done!

echo Development environment configured.
echo Happy coding!