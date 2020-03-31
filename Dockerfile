FROM ubuntu:bionic
MAINTAINER utsunomiya 20200226

RUN apt-get update
RUN apt-get install -y curl wget unzip language-pack-ja
RUN apt-get install -y openjdk-8-jdk

## ENV settings
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64
ENV SDK_TOOL_VERSION sdk-tools-linux-4333796
ENV ANDROID_HOME /usr/local/android-sdk-linux
ENV PATH $PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

# install android sdk tools
RUN mkdir $ANDROID_HOME && \
  wget "https://dl.google.com/android/repository/${SDK_TOOL_VERSION}.zip" && \
  unzip -d $ANDROID_HOME $SDK_TOOL_VERSION.zip && \
  rm -rf $SDK_TOOL_VERSION.zip

# agree sdkmanager licenses
RUN mkdir ~/.android && \
    touch ~/.android/repositories.cfg
RUN yes | sdkmanager --licenses

# install android tools and more
RUN sdkmanager "tools" 
RUN sdkmanager "build-tools;28.0.3"
RUN sdkmanager "platforms;android-22"
RUN sdkmanager "platform-tools" 
