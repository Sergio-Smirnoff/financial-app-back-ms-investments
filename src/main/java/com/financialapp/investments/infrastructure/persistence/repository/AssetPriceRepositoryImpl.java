package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.AssetPriceJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.AssetPricePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetPriceRepositoryImpl implements AssetPriceRepository {

    private final AssetPriceJpaRepository jpaRepository;
    private final AssetPricePersistenceMapper mapper;

    @Override
    public AssetPrice save(AssetPrice price) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(price)));
    }

    @Override
    public Optional<AssetPrice> findByTicker(Ticker ticker) {
        return jpaRepository.findByTicker(ticker.value()).map(mapper::toDomain);
    }

    @Override
    public List<AssetPrice> findAllByTickerIn(Collection<Ticker> tickers) {
        List<String> tickerValues = tickers.stream().map(Ticker::value).toList();
        return jpaRepository.findAllByTickerIn(tickerValues).stream()
                .map(mapper::toDomain).toList();
    }
}
