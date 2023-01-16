package com.zcurd.common;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import javax.sql.DataSource;

public class _JFinalDemoGenerator {
	public static DataSource getDataSource() {
		PropKit.use("a_little_config.txt");
		C3p0Plugin c3p0Plugin = new C3p0Plugin(PropKit.get("base_jdbcUrl"), PropKit.get("base_user"),
				PropKit.get("base_password").trim());
		c3p0Plugin.start();
		return c3p0Plugin.getDataSource();
	}

	public static void main(String[] args) {
		String baseModelPackageName = "com.demo.common.model.base";
		String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/zcurd/model/base/test";
		String modelPackageName = "com.demo.common.model";
		String modelOutputDir = baseModelOutputDir + "/..";
		Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName,
				modelOutputDir);
		generator.setGenerateChainSetter(false);
		generator.addExcludedTable(new String[]{"adv"});
		generator.setGenerateDaoInModel(true);
		generator.setGenerateChainSetter(true);
		generator.setGenerateDataDictionary(false);
		generator.setRemovedTableNamePrefixes(new String[]{"t_"});
		generator.generate();
	}
}
