package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFundingContribution is a Querydsl query type for FundingContribution
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFundingContribution extends EntityPathBase<FundingContribution> {

    private static final long serialVersionUID = 1202023363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFundingContribution fundingContribution = new QFundingContribution("fundingContribution");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.user.entity.QUser buyer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final QFunding funding;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QFundingOption option;

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> totalAmount = createNumber("totalAmount", Long.class);

    public final NumberPath<Long> unitPrice = createNumber("unitPrice", Long.class);

    public QFundingContribution(String variable) {
        this(FundingContribution.class, forVariable(variable), INITS);
    }

    public QFundingContribution(Path<? extends FundingContribution> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFundingContribution(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFundingContribution(PathMetadata metadata, PathInits inits) {
        this(FundingContribution.class, metadata, inits);
    }

    public QFundingContribution(Class<? extends FundingContribution> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.buyer = inits.isInitialized("buyer") ? new com.back.domain.user.entity.QUser(forProperty("buyer")) : null;
        this.funding = inits.isInitialized("funding") ? new QFunding(forProperty("funding"), inits.get("funding")) : null;
        this.option = inits.isInitialized("option") ? new QFundingOption(forProperty("option"), inits.get("option")) : null;
    }

}

