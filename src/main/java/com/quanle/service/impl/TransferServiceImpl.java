package com.quanle.service.impl;

import com.quanle.annotation.AutowiredX;
import com.quanle.annotation.ServiceX;
import com.quanle.annotation.TransactionalX;
import com.quanle.dao.AccountDao;
import com.quanle.dao.impl.JdbcAccountDaoImpl;
import com.quanle.pojo.Account;
import com.quanle.service.TransferService;

/**
 * @author quanle
 */
@ServiceX
@TransactionalX
public class TransferServiceImpl implements TransferService {

    @AutowiredX
    private JdbcAccountDaoImpl accountDao;
//用注解的时候，value要为实现类的驼峰格式
//    @AutowiredX("jdbcAccountDaoImpl")
//    private AccountDao accountDao;

    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

        Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney() - money);
        to.setMoney(to.getMoney() + money);

        accountDao.updateAccountByCardNo(to);
        accountDao.updateAccountByCardNo(from);

    }
}
