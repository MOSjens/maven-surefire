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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Changes the command line to execute the integration tests on a docker container. Instead of building the
 * command line with a cli object the command line for docker is saved in a script and the script is passed
 * to the cmd. The order of the commands is important and must not be changed.
 *
 * @author Jens Reinhart
 */
public class DockerUtil
{

    private final String scriptName;

    private final String dockerImage; // "openjdk:11"

    private final String windowsPathRepository; // "C:/Users/reinhart/.m2/Repository";
    private final String dockerPathRepository = "/repository";
    private final String windowsPathTrunk; // "C:\\noscan\\Cadenza\\GISterm_ArcGis_Rest_Client";
    private final String dockerPathTrunk = "/workspace";
    private final String projectName;

    File file;
    FileWriter writer;

    public DockerUtil ( String baseDir, String localRepository, String projectName, String dockerImage )
    {
        this.windowsPathTrunk = baseDir;
        this.windowsPathRepository = localRepository;
        this.projectName = projectName;
        this.dockerImage = dockerImage;
        scriptName = projectName + "DockerCommandLine.bat";
    }

    public String rewritePath( String originalPath )
    {
        if ( originalPath != null )
        {
            // Change each backslash to a forwardslash.
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

    public String rewriteJarPath( String originalPath )
    {
        return rewritePath( originalPath.replaceFirst( "/", "" ) );
    }

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

    public void addStringToDockerCommandlineScript( String command )
    {
        try
        {
            if ( file == null || writer == null )
            {
                file = new File( scriptName );
                file.setWritable( true );
                file.setReadable( true );
                writer = new FileWriter( file );
            }
            writer.write( command );
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

    public void closeDocekrCommandlineScript()
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
            closeDocekrCommandlineScript();
        }
        file.delete();
        file = null;

    }

    public String getWindowsPathRepository()
    {
        return windowsPathRepository;
    }

    public String getWindiowsPathTrunk()
    {
        return windowsPathTrunk;
    }
}
