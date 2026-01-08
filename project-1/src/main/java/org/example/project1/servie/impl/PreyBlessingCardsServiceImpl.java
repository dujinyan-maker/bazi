package org.example.project1.servie.impl;

import org.example.project1.mapper.PreyBlessingCardsMapper;
import org.example.project1.servie.PreyBlessingCardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/7 19:16
 */
@Service
public class PreyBlessingCardsServiceImpl implements PreyBlessingCardsService {
    @Autowired
    private PreyBlessingCardsMapper preyBlessingCardsMapper;
}
