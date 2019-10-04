package com.dataeconomy.migration.app.controller;

import java.io.IOException;
import java.util.concurrent.Executors;

import com.dataeconomy.migration.app.demo.DmuStreamGobbler;

public class test {

	public static void main(String[] args) {

		String str = "ssh -i /Users/dataeconomy/dmu-user.pem dmu-user@18.216.202.239 /opt/cloudera/parcels/CDH-5.16.2-1.cdh5.16.2.p0.8/bin/hadoop distcp  \r\n"
				+ " -Dfs.s3a.access.key=\"AKIAV6TWR75UHBACDLTW\" \r\n"
				+ " -Dfs.s3a.secret.key=\"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\"  \r\n"
				+ " hdfs://ip-172-31-20-195.us-east-2.compute.internal:8020/user/testfolder/backkup/categories/* s3a://dmutestbucket/categories ";
		Process process;
		try {
			process = Runtime.getRuntime().exec(str);
			DmuStreamGobbler streamGobbler = new DmuStreamGobbler(process.getInputStream(), System.out::println);
			Executors.newSingleThreadExecutor().submit(streamGobbler);
			int exitCode = process.waitFor();
			assert exitCode == 0;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

}
