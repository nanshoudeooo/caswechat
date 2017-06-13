package com.caspar.caswechat.user.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.caspar.caswechat.start.service.AccessTokenService;
import com.caspar.caswechat.user.entity.UserOpenIdList;
import com.caspar.caswechat.user.entity.UserWechat;
import com.caspar.caswechat.user.service.UserWechatService;
import com.caspar.caswechat.util.general.PropertyUtil;
import com.caspar.caswechat.util.general.StringUtil;
import com.caspar.caswechat.util.general.http.HttpRequester;
import com.caspar.caswechat.util.general.http.Response;

/**
 * @author caspar.chen
 * @date 2017-5-15
 */
@Service
public class UserWechatServiceImpl implements UserWechatService {

	private static final Logger log = LoggerFactory
			.getLogger(UserWechatServiceImpl.class);

	@Autowired
	private AccessTokenService accessTokenService;

	/**
	 * 获取用户详细信息
	 */
	public static String GET_USER_INFO = PropertyUtil.get("url_user_info_get");

	/**
	 * 获取用户openid列表
	 */
	public static String GET_USER_OPENID_LIST = PropertyUtil.get("url_user_openid_list");

	@Override
	public UserWechat getUserInfo(String openid) {
		String token = accessTokenService.getAccessTokenStr();
		UserWechat user = null;
		if (token != null) {
			String url = GET_USER_INFO.replace("ACCESS_TOKEN", token).replace(
					"OPENID", openid);
			HttpRequester request = HttpRequester.createDefault();
			Response response = request.get(url);
			String responseStr = response.getContent();

			JSONObject jsonObject = JSONObject.parseObject(responseStr);

			if (null != jsonObject) {
				if (StringUtil.isNotEmpty(jsonObject.getString("errcode"))
						&& !"0".equals(jsonObject.get("errcode"))) {
					log.error("获取用户信息失败 : " + jsonObject.toString());
					System.out.println("获取用户信息失败 : " + jsonObject.toString());
				} else {
					user = JSONObject
							.parseObject(responseStr, UserWechat.class);
				}
			}

		}
		return user;
	}

	@Override
	public UserOpenIdList getUserOpenIdList() {
		UserOpenIdList userOpenIdList = null;
		String token = accessTokenService.getAccessTokenStr();
		if (token != null) {
			String url = GET_USER_OPENID_LIST.replace("ACCESS_TOKEN", token)
					.replace("NEXT_OPENID", "");

			HttpRequester request = HttpRequester.createDefault();
			Response response = request.get(url);
			String responseStr = response.getContent();

			JSONObject jsonObject = JSONObject.parseObject(responseStr);

			if (null != jsonObject) {
				if (StringUtil.isNotEmpty(jsonObject.getString("errcode"))
						&& jsonObject.get("errcode") != "0") {
					log.error("获取关注用户列表失败 errcode: : " + jsonObject.toString());
				} else {
					userOpenIdList = JSONObject.parseObject(responseStr,
							UserOpenIdList.class);
				}
			}

		}
		return userOpenIdList;
	}

	@Override
	public List<UserWechat> getUserList() {
		List<UserWechat> list = new ArrayList<UserWechat>();

		// 获取关注用户openid列表
		UserOpenIdList userOpenId = getUserOpenIdList();

		if (userOpenId == null || userOpenId.getData().length <= 0) {
			return null;
		}
		for (int i = 0; i < userOpenId.getData().length; i++) {
			// 根据openid查询用户信息
			UserWechat user = getUserInfo(userOpenId.getData()[i]);
			if (user != null) {
				list.add(user);
			}
		}
		return list;
	}

}