FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjfx && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get install -y sbt libxrender1 libxtst6 libxi6
EXPOSE 8080
#-agentlib:jdwp=transport=dt_socket,address=8080,server=y,suspend=y
ENV JAVA_TOOL_OPTIONS -Djava.awt.headless=true
WORKDIR /schach
ADD . /schach
CMD sbt run
#CMD sbt test


#If you want to run the tests you should comment out the RUN Command and its specifications and
#replace sbt run with sbt test

#To run the Game correctly via Docker and without any crashes regarding the GUI
#you will need to install (atleast on Windows) VcXsrv X Server for Windows (or something similiar)
    #->https://sourceforge.net/projects/vcxsrv/
    #open XLauncher select 'multiple windows' -> submit
    #select 'start no client' -> submit
    #enable 'disable access control' -> submit
    #pull image with 'docker pull schmidtjan0/schach:latest'
    #->after building the image run it like this "docker run --rm -it -e DISPLAY=[YOUR-IP]:0.0 schmidtjan0/schach" (without the brackets)
    #"-e" defines an environment variable (needed to connect to the Window Server)
    #"-it" allows an interactive session (if you need console input)
    #"--rm" will automatically remove the container after you exit

