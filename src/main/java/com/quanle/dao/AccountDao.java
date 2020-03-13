package com.quanle.dao;

import com.quanle.annotation.RepositoryX;
import com.quanle.annotation.ServiceX;
import com.quanle.pojo.Account;

/**
 * @author quanle
 */
@RepositoryX
public interface AccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}
