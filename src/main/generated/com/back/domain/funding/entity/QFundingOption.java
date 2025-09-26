package com.back.domain.funding.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFundingOption is a Querydsl query type for FundingOption
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFundingOption extends EntityPathBase<FundingOption> {

    private static final long serialVersionUID = 1769788008L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFundingOption fundingOption = new QFundingOption("fundingOption");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final QFunding funding;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath name = createString("name");

    public final NumberPath<Long> price = createNumber("price", Long.class);

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final NumberPath<Integer> stock = createNumber("stock", Integer.class);

    public QFundingOption(String variable) {
        this(FundingOption.class, forVariable(variable), INITS);
    }

    public QFundingOption(Path<? extends FundingOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFundingOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFundingOption(PathMetadata metadata, PathInits inits) {
        this(FundingOption.class, metadata, inits);
    }

    public QFundingOption(Class<? extends FundingOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.funding = inits.isInitialized("funding") ? new QFunding(forProperty("funding"), inits.get("funding")) : null;
    }

}

