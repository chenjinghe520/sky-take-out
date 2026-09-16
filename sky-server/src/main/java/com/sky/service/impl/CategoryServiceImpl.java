package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.Collections;

@Service
public class CategoryServiceImpl
        extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SortArgumentResolver sortArgumentResolver;

    /**
     * 分类查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        Integer currentpage = categoryPageQueryDTO.getPage();
        Integer pagesize = categoryPageQueryDTO.getPageSize();
        Integer type = categoryPageQueryDTO.getType();
        String name = categoryPageQueryDTO.getName();
        //分页的页数和当前属于第几页
        Page<Category> page = new Page<>(currentpage, pagesize);

        //设置分页查询的条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(type != null,Category::getType,type)
                .eq(name != null,Category::getName, name)
                .orderByAsc(Category::getSort);

        categoryMapper.selectPage(page,wrapper);

        return new PageResult(
                page.getTotal(),
                page.getRecords()
        );
    }

    @Override
    public void removeBI(Long id) {
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
    }
}
