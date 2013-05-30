package com.mk.mongo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Goal starts mongo db
 *
 */
@Mojo( name = "start", defaultPhase = LifecyclePhase.INTEGRATION_TEST )
public class EmbeddedMongoMojo
    extends AbstractMojo
{
	
    @Parameter( defaultValue = "localhost", property = "host")
    private String MONGO_HOST;
	
    @Parameter( defaultValue = "27777", property = "port")
    private int MONGO_PORT;
	
    @Parameter( defaultValue = "script.js", property = "datapath")
    private static String MONGO_SCRIPT_FILE;
	
    @Parameter( defaultValue = "./target/mongodata", property = "datapath")
    private String MONGO_DATA_PATH;
	
    @Parameter( defaultValue = "mdb", property = "db", required = true )
    private String MONGO_DB;

	private DB db;
	private MongodExecutable mongodExe;
	private Mongo mongo;
	
	public static void main(String[] args){
		EmbeddedMongoMojo mojo = new EmbeddedMongoMojo();
		try {
			System.out.println(getScriptContent());
			mojo.execute();
		} catch (MojoExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public void execute()
        throws MojoExecutionException
    {
    	try {
			MongodStarter runtime = MongodStarter.getDefaultInstance();
			mongodExe = runtime.prepare(new MongodConfig(Version.V2_0_5, null,
					MONGO_PORT, Network.localhostIsIPv6(),
					MONGO_DATA_PATH, null, 0));
			mongodExe.start();
			mongo = new Mongo(MONGO_HOST, MONGO_PORT);
			db = mongo.getDB(MONGO_DB);
			db.doEval(getScriptContent(), new Object[0]);
			getLog().info("--In memory mongo db setup successful--");
		} catch (UnknownHostException e) {
			getLog().error(e.getMessage());
		} catch (IOException e) {
			getLog().error(e.getMessage());
		}
    }
    
	private static String getScriptContent() throws IOException {
		StringBuilder script = new StringBuilder();
		File file = new File(MONGO_SCRIPT_FILE);
		BufferedReader br = null;
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			br = new BufferedReader(reader);
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				script.append(strLine);
			}
		} finally {
			br.close();
			reader.close();
		}
		return script.toString();

	}
}
