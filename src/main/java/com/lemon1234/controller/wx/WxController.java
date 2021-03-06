package com.lemon1234.controller.wx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.lemon1234.entity.Grit;
import com.lemon1234.entity.Shout;
import com.lemon1234.entity.WxUserInfo;
import com.lemon1234.service.GritService;
import com.lemon1234.service.ShoutService;
import com.lemon1234.service.WxUserInfoService;
import com.lemon1234.util.HttpRequestUtil;
import com.lemon1234.util.StringUtil;
import com.lemon1234.util.WxUtil;

@Controller
@RequestMapping("/wx")
public class WxController {
	
	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Autowired
	private ShoutService shoutService;
	@Autowired
	private GritService gritService;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 获取微信 AccessToken
	 * 
	 * https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getAccessToken() throws Exception {
		
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx66855f8530eadefb&secret=cdfc92ca0c8fe1fb9f6920e305a040eb";
		JSONObject jsonObject = HttpRequestUtil.httpRequestGet(url);
		
		if(jsonObject != null && jsonObject.get("access_token") != null) {
			return (String) jsonObject.get("access_token");
		}
		return null;
	}
	
	/**
	 * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
	 */
	@ResponseBody
	@RequestMapping("/getOpenId")
	public Map<String, Object> wxLogin(@RequestParam(value = "code", required = false)String code) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		if(code == null) {
			result.put("success", false);
			return result;
		}
		
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wx66855f8530eadefb&secret=cdfc92ca0c8fe1fb9f6920e305a040eb" 
					+ "&js_code=" + code + "&grant_type=authorization_code";
		JSONObject jsonObject = HttpRequestUtil.httpRequestGet(url);
		
		if(jsonObject == null) {
			result.put("success", false);
			return result;
		} else {
			result.put("success", true);
			result.put("openId", jsonObject.get("openid"));
			return result;
		}
	}
	
	@ResponseBody
	@RequestMapping("/saveWxUserInfo")
	public Map<String, Object> wxUserInfo(WxUserInfo info) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		WxUserInfo wxUserInfo = wxUserInfoService.findWxUser(info.getOpenId());
		if(wxUserInfo != null && wxUserInfo.getNickName() != null) {
			wxUserInfo.setLastUpdateDt(new Date());
			wxUserInfoService.update(wxUserInfo);
		} else {
			wxUserInfoService.addWxUser(info);
		}
		result.put("success", true);
		return result;
	}
	
	@ResponseBody
	@RequestMapping("/addShout")
	public Map<String, Object> addShout(HttpServletRequest request, Shout shout) throws Exception {
		String text = shout.getText();
		Map<String, Object> result = new HashMap<String, Object>();
		
		ServletContext servletContext = request.getServletContext();
		String token = (String) servletContext.getAttribute("token");
		// 检测是不是有不良词汇
		boolean isFalse = WxUtil.checkText(text, token);
		
		if(isFalse) {
			result.put("errorInfo", "留言内容有不文明词汇");
			result.put("success", false);
			return result;
		}
		Grit grit = gritService.getRand();
		shout.setGrit(grit.getText());
		shoutService.addShout(shout);
		result.put("grit", grit);
		result.put("success", true);
		return result;
	}
	
	@ResponseBody
	@RequestMapping("/getShout")
	public Map<String, Object> getShout(String openId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Shout> shouts = shoutService.getShout(openId);
		for(Shout shout : shouts) {
			shout.setYyyymmdd(sdf.format(shout.getCreateDt()));
		}
		result.put("shouts", shouts);
		return result;
	}
	
	@ResponseBody
	@RequestMapping("/addGrit")
	public Map<String, Object> addGrit(
				HttpServletRequest request,
				@RequestParam(value = "text", required = false)String text, 
				@RequestParam(value = "openId", required = false)String openId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if(StringUtil.isEmpty(text) || StringUtil.isEmpty(openId)) {
			result.put("success", false);
		} else {
			// 先去查看是否有重复的
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("text", text);
			Integer count = gritService.getCount(param);
			if(count > 0) {
				result.put("success", false);
				result.put("errorInfo", "存在类似提示，请更换~");
			} else {
				// 检测是不是有不良词汇
				ServletContext servletContext = request.getServletContext();
				String token = (String) servletContext.getAttribute("token");
				// 检测是不是有不良词汇
				boolean isFalse = WxUtil.checkText(text, token);
				if(isFalse) {
					result.put("errorInfo", "留言内容有不文明词汇");
					result.put("success", false);
				} else {
					// 没有直接添加，有的话返回错误信息
					Grit grit = new Grit();
					grit.setCreateDt(new Date());
					grit.setOpenId(openId);
					grit.setText(text);
					grit.setStatus(Grit.NO);
					
					gritService.addGrit(grit);
					result.put("success", true);
				}
			}
		}
		return result;
	}
	
	@ResponseBody
	@RequestMapping("/getGrits")
	public Map<String, Object> getGrits(@RequestParam(value = "openId", required = false)String openId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if(StringUtil.isEmpty(openId)) {
			result.put("success", false);
		} else {
			List<Grit> grits = gritService.getGrits(openId);
			result.put("grits", grits);
			result.put("success", true);
		}
		return result;
	}
	
}
