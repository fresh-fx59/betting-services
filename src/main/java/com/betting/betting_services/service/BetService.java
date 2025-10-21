package com.betting.betting_services.service;

import com.betting.betting_services.model.BetRequest;
import com.betting.betting_services.model.BetResponse;

public interface BetService {

    BetResponse placeBet(BetRequest request);
}

