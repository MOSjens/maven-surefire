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

    private final String dockerImage;

    private final String hostPathRepository;
    private final String dockerPathRepository = "/repository";
    private final String hostPathTrunk;
    private final String dockerPathTrunk = "/workspace";
    private final String projectName;


    private String dockerCommand = "";


    public DockerUtil ( String hostPathTrunk, String hostPathRepository, String projectName, String dockerImage )
    {
        this.hostPathTrunk = hostPathTrunk;
        this.hostPathRepository = hostPathRepository;
        this.projectName = projectName;
        this.dockerImage = dockerImage;
    }

    // Rewrite paths so they match to the filesystem inside the docker container.
    public String rewritePath( String originalPath )
    {
        if ( originalPath != null )
        {
            // Change uris to the docker path.
            if ( originalPath.contains( hostPathRepository ) )
            {
                originalPath = originalPath.replace( hostPathRepository, dockerPathRepository );
            }
            else if ( originalPath.contains( hostPathTrunk ) )
            {
                originalPath = originalPath.replace( hostPathTrunk, dockerPathTrunk );
            }

        }

        return originalPath;
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

    // Collect the whole command for the command Line here.
    public void addStringToDockerCommand( String command )
    {
        dockerCommand += command;
    }

    public String getDockerCommand()
    {
        return "docker run --rm ";
    }

    public String getDockerMountRepository()
    {
        return getDockerMount( hostPathRepository, dockerPathRepository );
    }

    public String getDockerMountBaseDir()
    {
        return getDockerMount( hostPathTrunk, dockerPathTrunk );
    }

    public String getDockerMount( String source, String target )
    {
        String command = "--mount type=bind,source=\""
                + source
                + "\",target=\""
                + target
                + "\" ";
        return command;
    }

    public String getGoToBaseDirCommand()
    {
        return "cd " + dockerPathTrunk + "/" + projectName + "; Xvfb :1 & export DISPLAY=:1;";
    }

    public String getShellInDocker()
    {
        return "bin/bash -c";
    }


    public String getDockerString()
    {
        return dockerCommand;
    }

    public String getHostPathRepository()
    {
        return hostPathRepository;
    }

    public String getHostPathTrunk()
    {
        return hostPathTrunk;
    }

    public String getProjectName()
    {
        return projectName;
    }

    // also needed for the command
    public String getDockerImage()
    {
        return dockerImage;
    }
}
