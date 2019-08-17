package com.alysida.controller;

import cn.itrip.common.*;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripUser;
import com.alibaba.fastjson.JSONArray;
import com.alysida.biz.SentSSM;
import com.alysida.biz.TokenBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import jdk.nashorn.internal.parser.Token;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.management.Agent;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Random;

@Controller
@RequestMapping(value = "/api")
@Api(value = "api",description = "用户模块")
public class UserController {
	@Resource
	ItripUserMapper dao;

	@Resource
	JredisApi jredis=new JredisApi();

	@RequestMapping(value = "/login",produces = "application/json;charset=utf-8")
	@ResponseBody
	public Object getList() throws Exception {
		ItripUser user=
		dao.getItripUserById(new Long(12));
		return JSONArray.toJSONString(user);
	}

	@RequestMapping(value = "/login1",produces = "application/json;charset=utf-8")
	@ResponseBody
	public Object getList1() throws Exception {
		try {
			jredis.SetRedis("a1","testdata",60);
			return JSONArray.toJSONString("插入成功");
		}catch(Exception ex)
		{
			return JSONArray.toJSONString("插入失败");
		}

		//return "添加成功"; 不加produces 显示为????
	}

	@Resource
	TokenBiz tokenBiz=new TokenBiz();

	@RequestMapping(value = "/dologin",method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="form",required=true,value="用户名",name="name",defaultValue="itrip@163.com"),
			@ApiImplicitParam(paramType="form",required=true,value="密码",name="password",defaultValue="123456"),
	})
	//变量必须与接口文档匹配
	public Dto getList2(HttpServletRequest request, String name, String password) throws Exception {
		try {
			ItripUser user=dao.ifLogin(name,MD5.getMd5(password,32));
			if (user!=null){
				//生成一个token
				String token=tokenBiz.generateToken(request.getHeader("User-Agent"),user);

				jredis.SetRedis(token,JSONArray.toJSONString(user),60*60*2);

				//返回前台页面需要当前时间与过期时间  1s=1000ms
				ItripTokenVO tokenVO=new ItripTokenVO(token,Calendar.getInstance().getTimeInMillis()+3600*2*1000,Calendar.getInstance().getTimeInMillis());
				return DtoUtil.returnDataSuccess(tokenVO);
			}

		}catch(Exception ex)
		{

		}
		return DtoUtil.returnFail("登录失败","1000");
	}

	/*@RequestMapping(value = "/validatephone",method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query",required=false,value="手机号码",name="user",defaultValue="13811565189"),
			@ApiImplicitParam(paramType="query",required=false,value="验证码",name="code",defaultValue="8888")
	})
	public Dto validatephone(String user, String code) throws Exception {

	}

	@RequestMapping(value = "/registerbyphone",method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="body",required=true,value="用户实体",name="userVO")
	})
	public Dto registerbyphone(ItripUser itripUser) throws Exception {
		//为手机号发送验证码
	}*/

	@Resource
	SentSSM sentSSM;

	@RequestMapping(value = "/validatephone")
	@ResponseBody
	public Dto validatephone(String user,String code) throws Exception {
		//去redis中查找数据
		try {
			String result=jredis.getRedis("Code:"+user);
			if(result.equals(code))
			{
				//根据手机号查询实体，然后update
				//   ItripUser tt=dao.getUserE(user);
				dao.UpdateByUser(user);
				return DtoUtil.returnSuccess("激活成功！！");
			}
		}
		catch (Exception ex)
		{
			return DtoUtil.returnSuccess("激活失败！！");
		}

		return DtoUtil.returnSuccess("激活失败！！");
	}


	@RequestMapping(value = "/registerbyphone", method = RequestMethod.POST)
	@ResponseBody

	public Dto registerbyphone(@RequestBody ItripUser itripUser) throws Exception {
		try {
			//为手机号发送验证码
			Random random = new Random(7);
			int i = random.nextInt(9999);
			//把随机4位数字发送给手机，把这个四位验证码存入到redis 中去
			sentSSM.setPhone(itripUser.getUserCode(), "" + i);

			//存入到redis 中
			jredis.SetRedis("Code:" + itripUser.getUserCode(), "" + i, 60*60);

			//把实体类存入到数据库中去
			ItripUser user = new ItripUser();
			user.setUserCode(itripUser.getUserCode());
			user.setUserName(itripUser.getUserName());
			user.setUserPassword(MD5.getMd5(itripUser.getUserPassword(),32));
			user.setActivated(0);


			Integer result = dao.insertItripUser(user);
			if (result > 0) {
				return DtoUtil.returnDataSuccess(user);
			}
		} catch (Exception ex) {
			return DtoUtil.returnFail("注册失败", "10000");
		}
		return DtoUtil.returnFail("注册失败", "10000");
	}



	@RequestMapping("test")
	public void test(){
		SentSSM sentSSM=new SentSSM();
		sentSSM.setPhone("13933218129","1234");
	}

}
