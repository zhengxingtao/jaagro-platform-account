package com.jaagro.account.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.jaagro.account.api.constant.AccountStatus;
import com.jaagro.account.api.constant.AccountType;
import com.jaagro.account.api.dto.request.UpdateAccountDto;
import com.jaagro.account.api.dto.request.CreateAccountDto;
import com.jaagro.account.api.dto.response.AccountDto;
import com.jaagro.account.api.service.AccountService;
import com.jaagro.account.biz.entity.Account;
import com.jaagro.account.biz.mapper.AccountMapperExt;
import com.jaagro.utils.ResponseStatusCode;
import com.jaagro.utils.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author yj
 * @date 2018/10/23
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountMapperExt accountMapperExt;
    @Autowired
    private CurrentUserService currentUserService;
    /**
     * 创建账户
     *
     * @param createAccountDto
     * @return
     */
    @Override
    public Integer createAccount(CreateAccountDto createAccountDto) {
        Account account = accountMapperExt.selectActiveAccount(AccountType.CASH,createAccountDto.getUserId(),createAccountDto.getUserType());
        if (account != null){
            log.info("account already created,{}", JSON.toJSONString(createAccountDto));
            return account.getId();
        }
        account = new Account();
        BeanUtils.copyProperties(createAccountDto,account);
        generateAccount(account);
        accountMapperExt.insert(account);
        return account.getId();
    }

    /**
     * 修改账户
     *
     * @param updateAccountDto
     * @return
     */
    @Override
    public boolean updateAccount(UpdateAccountDto updateAccountDto) {
        Account account = new Account();
        BeanUtils.copyProperties(updateAccountDto,account);
        account.setModifyTime(new Date());
        account.setModifyUserId(currentUserService.getCurrentUser() == null ? null : currentUserService.getCurrentUser().getId());
        accountMapperExt.updateByPrimaryKeySelective(account);
        return true;
    }

    /**
     * 查询账户
     *
     * @param id
     * @return
     */
    @Override
    public AccountDto getById(Integer id) {
        Account account = accountMapperExt.selectByPrimaryKey(id);
        if (account == null){
            return null;
        }
        AccountDto accountDto = new AccountDto();
        BeanUtils.copyProperties(account,accountDto);
        return accountDto;
    }

    /**
     * 删除账户(逻辑删除)
     *
     * @param id
     * @return
     */
    @Override
    public boolean disableAccount(Integer id) {
        Account account = accountMapperExt.selectByPrimaryKey(id);
        if (account == null){
            log.warn("id not exist id={}",id);
            return false;
        }
        account.setAccountStatus(AccountStatus.FREEZE);
        account.setModifyTime(new Date());
        account.setModifyUserId(currentUserService.getCurrentUser() == null ? null : currentUserService.getCurrentUser().getId());
        int result = accountMapperExt.updateByPrimaryKeySelective(account);
        if (result == 1){
            return true;
        }
        return false;
    }

    private void generateAccount(Account account) {
        account.setVersion(0);
        account.setAccountStatus(AccountStatus.NORMAL);
        account.setBalance(new BigDecimal("0"));
        account.setCreateTime(new Date());
        account.setCredit(new BigDecimal("0"));
        account.setDebit(new BigDecimal("0"));
    }
}