package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

public interface CategoryService extends IService<Category> {

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void removeBI(Long id);
}
