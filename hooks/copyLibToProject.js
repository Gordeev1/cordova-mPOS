function updateProjectProperty(ctx) {
	var changes = 'android.library.reference.1=mPOS';
	var fs = ctx.requireCordovaModule('fs');
	var path = ctx.requireCordovaModule('path');
	var os = ctx.requireCordovaModule('os');

	var propertiesFile = path.join(ctx.opts.projectRoot, 'platforms/android/project.properties');

	if (fs.existsSync(propertiesFile)) {
		fs.readFile(propertiesFile, 'utf8', function(error, data) {
			if (error) {
				throw new Error('[cordova-mPOS] Unable to find build.gradle: ' + error);
			}

			if (data.indexOf(changes) === -1) {
				var result = data + os.EOL + changes;

				return fs.writeFile(propertiesFile, result, 'utf8', function(error) {
					if (error) {
						throw new Error(
							'[cordova-mPOS] Unable to write into project.properties file: ' + error
						);
					}
				});
			}
		});
	}
}

function copyLibToProject(ctx) {
	var childProcess = ctx.requireCordovaModule('child_process');
	childProcess.exec(`cp -r ./libs/android/mPOS ./platforms/android/mPOS/`, function(error) {
		if (error) {
			throw new Error(
				`[cordova-mPOS] error while move lib to project: ${error.message || error}`
			);
		}
	});
}

module.exports = function(ctx) {
	if (ctx.opts.platforms && ctx.opts.platforms.indexOf('android') < 0) {
		return;
	}

	updateProjectProperty(ctx);
	copyLibToProject(ctx);
};
