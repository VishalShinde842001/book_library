package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;

import com.dao.ObjectDao;
import com.helper.AppConstants;
import com.model.CommonAppSetting;
import com.model.ProfilePicData;
import com.model.User;

public class FileUtility {

	@Autowired
	private ObjectDao objectDao;

	public void saveProfileImages(User user) {
		if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
			CommonAppSetting imageStoreBaseUrl = objectDao.getObjectByParam(CommonAppSetting.class, "settingName",
					AppConstants.IMAGE_BASE_PATH);
			CommonAppSetting profileFolder = objectDao.getObjectByParam(CommonAppSetting.class, "settingName",
					AppConstants.PROFILE_IMAGE_FOLDER);

			if (null != imageStoreBaseUrl && imageStoreBaseUrl.getSettingValue() != null && null != profileFolder
					&& profileFolder.getSettingValue() != null) {
				// String directory = "C:\\Users\\DELL\\Desktop\\Book Lib Project Personal
				// Frontend\\book_library_admin_panel\\src\\assets\\profileImages\\";

				File directoryFile = new File(imageStoreBaseUrl.getSettingValue() + profileFolder.getSettingValue());
				if (!directoryFile.exists()) {
					directoryFile.mkdirs();
				}
				String fileName = "profile_" + user.getUserId() + "_" + System.currentTimeMillis() + ".png";
				String filePath = imageStoreBaseUrl.getSettingValue() + profileFolder.getSettingValue() + fileName;
				File file = new File(filePath);
				try (FileOutputStream fos = new FileOutputStream(file)) {
					String base64Image = user.getProfileImage();
					if (base64Image.startsWith("data:image/png;base64,")) {
						base64Image = base64Image.replace("data:image/png;base64,", "");
					} else if (base64Image.startsWith("data:image/jpeg;base64,")) {
						base64Image = base64Image.replace("data:image/jpeg;base64,", "");
					}
					byte[] imageBytes = Base64.getDecoder().decode(base64Image);
					fos.write(imageBytes);
					fos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ProfilePicData profilePicData = new ProfilePicData();
				profilePicData.setProfilePicPath(fileName);
				profilePicData.setUser(user);
				objectDao.saveObject(profilePicData);
			}
		}
	}
}
