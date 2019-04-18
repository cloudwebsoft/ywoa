package com.redmoon.oa.upgrade.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.redmoon.oa.upgrade.service.UpgradeException;

public class DatabaseUpgradeUtil extends JdbcDaoSupport implements
		IDatabaseUpgradeUtil {
	private Logger logger = LoggerFactory.getLogger(DatabaseUpgradeUtil.class);

	public DatabaseUpgradeUtil() {
		super();
	}

	private void runScript(BufferedReader reader) throws IOException {
		StringBuffer buffer = new StringBuffer();
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				execute(buffer);
				return;
			} else if (line.matches("^\\s*$")) {
				execute(buffer);
				buffer.setLength(0);
			} else {
				buffer.append(line);
			}
		}
	}

	private void execute(StringBuffer buffer) {
		this.logger
				.info(">>> Upgrade database with sql: {}", buffer.toString());
		this.getJdbcTemplate().execute(buffer.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.redmoon.oa.upgrade.util.IDatabaseUpgradeUtil#upgrade(java.io.File)
	 */
	public void upgrade(File script) {
		if (!script.exists()) {
			return;
		}
		InputStreamReader  reader = null;
		try {
			reader = new InputStreamReader (new FileInputStream(script), "UTF-8");
			this.runScript(new BufferedReader(reader));
		} catch (FileNotFoundException e) {
			throw new UpgradeException("Upgrade database failed.", e);
		} catch (IOException e) {
			throw new UpgradeException("Upgrade database failed.", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
				reader = null;
			}

		}

	}
}
