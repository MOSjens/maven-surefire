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
 * Utils to change the command line to execute the integration tests inside a docker container.
 * This class provides methods to rewrite paths which point to the host system to the correct paths inside the
 * container. The order of the commands is important and must not be changed.
 *
 * @author Jens Reinhart
 */
public class DockerUtil
{
    private final String hostPathTrunk;
    private final String hostPathRepository;
    private final String projectName;
    private final String dockerImage;

    // The path to the maven repository inside the docker container.
    private final String dockerPathRepository = "/repository";
    // The path to the cadenza trunk inside the docker container.
    private final String dockerPathTrunk = "/workspace";


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

    // Rewrite classpaths so they match to the filesystem inside the docker container.
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
        return "--mount type=bind,source=\""
                + source
                + "\",target=\""
                + target
                + "\" ";
    }

    public String getGoToBaseDirCommand()
    {
        return "cd " + dockerPathTrunk + "/" + projectName + "; Xvfb :1 & export DISPLAY=:1;";
        //return "Xvfb :1 & export DISPLAY=:1;";
    }

    public String getShellInDocker()
    {
        return "bin/bash -c";
    }

    public String getDockerImage()
    {
        return dockerImage;
    }
}
