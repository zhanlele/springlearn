package com.quanle.service;

import com.quanle.annotation.ServiceX;

/**
 * @author quanle
 */
@ServiceX
public interface TransferService {

    void transfer(String fromCardNo, String toCardNo, int money) throws Exception;
}
