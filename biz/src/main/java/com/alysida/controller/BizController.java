package com.alysida.controller;

import cn.itrip.common.Dto;
import cn.itrip.common.DtoUtil;
import cn.itrip.dao.itripAreaDic.ItripAreaDicMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/api")
@Api(value = "api",description = "业务逻辑模块")
public class BizController {
	@Resource
	ItripAreaDicMapper dao;

	@RequestMapping("/hotel/queryhotcity/{type}")
	@ResponseBody
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path",required=true,value="type",name="type")
	})
	public Dto getList(@PathVariable("type")String type) throws Exception{
		return DtoUtil.returnDataSuccess(dao.isHot(type));
	}
}
