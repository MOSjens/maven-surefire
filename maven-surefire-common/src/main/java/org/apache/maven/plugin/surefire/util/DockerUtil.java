package org.apache.maven.plugin.surefire.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.booter.Classpath;

//import java.io.BufferedReader;
import java.io.File;
//import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Iterator;

/**
 * Changes the command line to execute the integration tests on a docker container. On a Windows OS the command for
 * docker is saved in a script and the script is passed to the cmd. On any other OS we build the docker command with
 * a cli object and start the command line directly.
 * The order of the commands is important and must not be changed.
 *
 * @author Jens Reinhart
 */
public class DockerUtil
{

    private final String scriptNameWindows;

    private final String dockerImage;

    // TODO change name from Windows to Host

    private final String windowsPathRepository; // "C:/Users/reinhart/.m2/Repository";
    private final String dockerPathRepository = "/repository";
    private final String windowsPathTrunk; // "C:\\noscan\\Cadenza\\GISterm_ArcGis_Rest_Client";
    private final String dockerPathTrunk = "/workspace";
    private final String projectName;

    private int forkNumber = 0;

    // Can be removed.
    private boolean isWindows = false;

    private String completeCommand = "";

    private File file;
    private FileWriter writer;

    public DockerUtil ( String windowsPathTrunk, String windowsPathRepository, String projectName, String dockerImage )
    {
        this.windowsPathTrunk = windowsPathTrunk;
        this.windowsPathRepository = windowsPathRepository;
        this.projectName = projectName;
        this.dockerImage = dockerImage;

        String os = System.getProperty( "os.name" ).toLowerCase();
        isWindows = os.contains( "win" );

        scriptNameWindows = projectName + forkNumber + "DockerCommandLine.bat";

    }

    public DockerUtil ( String windowsPathTrunk, String windowsPathRepository, String projectName, String dockerImage,
                        int forkNumber )
    {
        this.windowsPathTrunk = windowsPathTrunk;
        this.windowsPathRepository = windowsPathRepository;
        this.projectName = projectName;
        this.dockerImage = dockerImage;
        this.forkNumber = forkNumber;

        String os = System.getProperty( "os.name" ).toLowerCase();
        isWindows = os.contains( "win" );

        scriptNameWindows = projectName + forkNumber + "DockerCommandLine.bat";
    }

    // Rewrite paths so they match to the filesystem inside the docker container.
    public String rewritePath( String originalPath )
    {
        if ( originalPath != null )
        {
            // Change each backslash to a forwardslash.
            // TODO remove the replace
            originalPath = originalPath.replace( "\\", "/" );
            String repositoryPath = windowsPathRepository.replace( "\\", "/" );
            String trunkPath = windowsPathTrunk.replace( "\\", "/" );

            // Change uris to the docker path.
            if ( originalPath.contains( repositoryPath ) )
            {
                originalPath = originalPath.replace( repositoryPath, dockerPathRepository );
            }
            else if ( originalPath.contains( trunkPath ) )
            {
                originalPath = originalPath.replace( trunkPath, dockerPathTrunk );
            }

        }

        return originalPath;

    }

    // Rewrite paths so they match to the filesystem inside the docker container.
    public String rewriteJarPath( String originalPath )
    {
        return rewritePath( originalPath.replaceFirst( "/", "" ) );
    }

    // Rewrite paths so they match to the filesystem inside the docker container.
    public Classpath rewriteClasspath( Classpath cp )
    {
        Classpath newCp = Classpath.emptyClasspath();

        for ( Iterator<String> it = cp.iterator(); it.hasNext(); )
        {
            File file = new File( it.next() );
            String uri = file.getAbsolutePath();

            String newUri = rewritePath( uri );

            newCp = newCp.addClassPathElementUrl( newUri );

        }

        return newCp;
    }

    // On windows we use a batch file to start the docker command.
    public void addStringToDockerCommandlineScript( String command )
    {
        try
        {
            if ( file == null || writer == null )
            {
                file = new File( scriptNameWindows );
                file.setWritable( true );
                file.setReadable( true );
                writer = new FileWriter( file );
            }
            writer.write( command );
            completeCommand += command;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public void addDockerCommandToCommandLineScript()
    {
        addStringToDockerCommandlineScript( "docker run --rm " );
    }

    public void addDockerMountRepositoryToCommandLineScript()
    {
        addDockerMountToCommandLineScript( windowsPathRepository, dockerPathRepository );
    }

    public void addDockerMountBaseDirToCommandLineScript()
    {
        addDockerMountToCommandLineScript( windowsPathTrunk, dockerPathTrunk );
    }

    public void addDockerMountToCommandLineScript( String source, String target )
    {
        String command = "--mount type=bind,source=\""
                + source
                + "\",target=\""
                + target
                + "\" ";
        addStringToDockerCommandlineScript( command );
    }

    public void addDockerImageToCommandLineScript()
    {
        addStringToDockerCommandlineScript( dockerImage + " " );
    }

    public void addChangeToBaseDirToCommandLineScript()
    {
        addStringToDockerCommandlineScript( " bin/bash -c \"cd " + dockerPathTrunk
                + "/" + projectName + "; Xvfb :1 & export DISPLAY=:1; " );
    }

    public String getDockerCommandlineScriptPath()
    {
        return file.getAbsolutePath();
    }

    public void closeDockerCommandlineScript()
    {
        try
        {
            writer.close();
            writer = null;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public void deleteDockerCommandlineScript()
    {
        if ( writer != null )
        {
            closeDockerCommandlineScript();
        }
        file.delete();
        file = null;

    }

    public String getScriptContent()
    {
        return completeCommand;
    }

    public String getWindowsPathRepository()
    {
        return windowsPathRepository;
    }

    public String getWindowsPathTrunk()
    {
        return windowsPathTrunk;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getDockerImage()
    {
        return dockerImage;
    }
}
