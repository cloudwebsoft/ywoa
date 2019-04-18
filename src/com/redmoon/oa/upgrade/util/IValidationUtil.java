package com.redmoon.oa.upgrade.util;

import java.io.File;

import com.redmoon.oa.upgrade.domain.VersionResponse;

public interface IValidationUtil {

	public abstract void validate(File file, VersionResponse versionResponse);

}