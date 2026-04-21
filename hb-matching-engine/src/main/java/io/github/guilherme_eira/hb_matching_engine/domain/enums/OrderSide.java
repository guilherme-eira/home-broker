package io.github.guilherme_eira.hb_matching_engine.domain.enums;

public enum OrderSide {
    BID, ASK;

    public OrderSide opposite() {
        return this == BID ? ASK : BID;
    }
}

