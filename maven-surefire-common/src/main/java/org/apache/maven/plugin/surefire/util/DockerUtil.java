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

public class DockerUtil {

    private static final String SCRIPT_NAME = "DockerCommandLine.bat";

    static File file;
    static FileWriter writer;

    private static final String windowsPathRepository = "C:/Users/reinhart";
    private static final String dockerPathRepository = "/root";
    private static final String windowsPathTrunk = "C:/noscan";
    private static final String dockerPathTrunk = "/workspace";

    public DockerUtil ()
    {
    }

    public static String rewritePath( String originalPath )
    {
        if ( originalPath != null )
        {
            // Change each backslash to a forwardslash.
            originalPath = originalPath.replace( "\\", "/" );

            // Change uris to the docker path.
            if ( originalPath.contains( windowsPathRepository ) )
            {
                originalPath = originalPath.replace( windowsPathRepository, dockerPathRepository );
            }
            else if ( originalPath.contains( windowsPathTrunk ) )
            {
                originalPath = originalPath.replace( windowsPathTrunk, dockerPathTrunk );
            }

        }

        return originalPath;

    }

    public static Classpath rewriteClasspath( Classpath cp )
    {
        //TODO insert a kind of configuration to get the new paths automatically.
        Classpath newCp = Classpath.emptyClasspath();

        for (Iterator<String> it = cp.iterator(); it.hasNext(); )
        {
            File file = new File( it.next() );
            String uri = file.getAbsolutePath();

            String newUri = rewritePath( uri );

            newCp = newCp.addClassPathElementUrl( newUri );

        }

        return newCp;
    }

    public static void addStringToDockerCommandlineScript( String command )
    {
        try
        {
            if ( file == null || writer == null )
            {
                file = new File( SCRIPT_NAME );
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

    public static String getDockerCommandlineScriptPath()
    {
        return file.getAbsolutePath();
    }

    public static void closeDocekrCommandlineScript()
    {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
