package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;


@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService{

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void savewithFlavor(DishDTO dishDTO) {
        //菜品的插入
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        //口味的插入
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insert(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        Integer currentpage = dishPageQueryDTO.getPage();
        Integer pageSize = dishPageQueryDTO.getPageSize();
        Page<DishVO> page = new Page<>(
                currentpage,
                pageSize
        );
        dishMapper.pageQuery(page,dishPageQueryDTO);
        return new PageResult(
                page.getTotal(),
                page.getRecords()
        );

    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否在起售中
        for(Long id : ids){
            Dish dish = dishMapper.selectById(id);
            if(dish == null) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_NOT_FOUND);
            }
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                //当前正在售卖中，无法删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否被套餐关联了
        LambdaQueryWrapper<SetmealDish> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.in(SetmealDish::getDishId, ids);
        List<SetmealDish> setmeals = setmealDishMapper.selectList(setmealWrapper);
        if(setmeals != null && !setmeals.isEmpty()){
            //当前菜品被关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id : ids){
            dishMapper.deleteById(id);

        }
        // 删除对应口味
        LambdaQueryWrapper<DishFlavor> flavorWrapper =
                new LambdaQueryWrapper<>();

        flavorWrapper.in(DishFlavor::getDishId, ids);

        dishFlavorMapper.delete(flavorWrapper);
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.selectById(id);

        //根据菜品id查询口味数据
        LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
        flavorWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(flavorWrapper);

        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.updateById(dish);

        // 删除对应口味
        LambdaQueryWrapper<DishFlavor> flavorWrapper =
                new LambdaQueryWrapper<>();

        flavorWrapper.in(DishFlavor::getDishId, dishDTO.getId());

        dishFlavorMapper.delete(flavorWrapper);

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                // 给每条口味设置所属菜品的 ID
                flavor.setDishId(dishDTO.getId());
            }

            dishFlavorMapper.insert(flavors);
        }

    }
}
